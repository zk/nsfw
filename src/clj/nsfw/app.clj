(ns nsfw.app
  (:use [nsfw.util]
        (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]
        [ring.middleware.session.cookie :only (cookie-store)]
        [clojure.pprint :only [pprint]])
  (:require [net.cgrand.moustache :as moustache]
            [nsfw.html :as html]
            [nsfw.middleware :as nm]
            [nsfw.util :as nu]
            [clojure.string :as str]
            [clout.core :as clout])
  (:import [java.io
            PushbackReader
            File
            BufferedReader
            FileReader]))

(defn debug-exceptions
  "Ring handler that will render exceptions to the client.

   Don't use in production."
  [h enabled? & [with-err]]
  (fn [r]
    (if-not enabled?
      (h r)
      (try
        (h r)
        (catch Exception e
          (if with-err
            (with-err e r)
            {:header {"Content-Type" "text/html"}
             :status 500
             :body (html/html5
                    [:head
                     [:title (str (type e) " | NSFW")]
                     [:style {:type "text/css"}
                      (html/css [:body {:padding "1em 3em"
                                        :font-family "'Helvetica Neue', Helvetica, Arial, sans-serif"}]
                                [:pre {:padding "1em"
                                       :border "solid #ccc 1px;"
                                       :border-radius "0.5em"
                                       :background-color "#fafafa"
                                       :overflow-x "scroll"
                                       :margin-bottom "3em"}])]]
                    [:body
                     [:h1 "Oh Snap!"]
                     [:pre (nu/stacktrace->str e)]
                     [:h2 "Request"]
                     [:pre (nu/pp-str r)]])}))))))

(defn clj->js-name [s]
  (-> s
      str
      (str/replace #"-" "_")))

(defn cs-route [opts]
  (fn [r]
    (let [{:keys [entry title data css google-maps js]} opts
          data (when data
                 (data r))
          css (if (coll? css)
                css
                [css])
          entry-ns (first (str/split (str entry) #"/"))
          entry-js (->> (str/split (str entry) #"/")
                        (interpose ".")
                        (apply str)
                        clj->js-name)]
      {:headers {"Content-Type" "text/html"}
       :body (html/html5
              [:head
               [:meta {:name "viewport" :content "width=device-width" :initial-scale "1"}]
               (when title [:title title])
               (if-not (empty? css)
                 (map #(html/stylesheet (str "/css/" (name %) ".css")) css)
                 (html/stylesheet (str "/css/app.css")))]
              [:body
               (when js
                 (map (fn [src]
                        [:script {:type "text/javascript"
                                  :src (if (keyword? src)
                                         (str "/js/" (name src) ".js")
                                         src)}])
                      js))
               (when google-maps
                 [:script {:type "text/javascript"
                           :src "http://maps.googleapis.com/maps/api/js?sensor=false"}])
               (when data
                 [:script {:type "text/javascript"}
                  (str
                   (->> data
                        (map #(str "window."
                                   (str/replace (name (key %)) #"-" "_")
                                   " = "
                                   (-> % val pr-str nu/to-json)))
                        (interpose ";")
                        (apply str))
                   ";")])
               (html/script (str "/js/app.js"))
               (when entry
                 [:script {:type "text/javascript"}
                  (str entry-js "()")])])})))

(defn session-store [kw-or-opts]
  (let [type (if (keyword? kw-or-opts)
               kw-or-opts
               (:type kw-or-opts))
        opts (if-not (keyword? kw-or-opts)
               (dissoc kw-or-opts :type))]
    (condp = type
      :atom (if opts
              (memory-store opts)
              (memory-store))

      ;; encrypted cookie
      (if opts
        (cookie-store opts)
        (cookie-store)))))

(defmacro route [& routes]
  `(moustache/app ~@routes))

(defmacro route-default [opts & routes]
  `(moustache/app
    (wrap-session ~opts)
    wrap-file-info
    (wrap-file "resources/public" {:allow-symlinks? true})
    wrap-params
    wrap-nested-params
    wrap-keyword-params
    ~@routes))

(defmacro clojurescript [& opts]
  (let [{:keys [routes] :as opts} (apply hash-map opts)]
    `(let [opts# ~opts
           store# (or (:session-store opts#) (memory-store (:session-atom opts#)))
           public-path# (or (:public-path opts#) "resources/public")]
       (moustache/app
        (wrap-session {:store store#})
        wrap-file-info
        (wrap-file public-path# {:allow-symlinks? true})
        wrap-params
        wrap-nested-params
        wrap-keyword-params
        ~@routes
        [""] (cs-route opts#)))))

(defn has-route? [m]
  (-> m :meta :nsfw/route))

(defn has-comp-tag? [m]
  (-> m :meta :nsfw/comp-tag))

(defn has-middleware-tag? [m]
  (-> m :meta :nsfw/middleware))

(defn comment?
  "Returns true if form is a (comment ...)"
  [form]
  (and (list? form) (= 'comment (first form))))

(defn ns-decl?
  "Returns true if form is a (ns ...) declaration."
  [form]
  (and (list? form) (= 'ns (first form))))

(defn read-ns-decl
  "Attempts to read a (ns ...) declaration from a
  java.io.PushbackReader, and returns the unevaluated form. Returns
  nil if read fails or if a ns declaration cannot be found. The ns
  declaration must be the first Clojure form in the file, except for
  (comment ...) forms."
  [rdr]
  (try
    (loop [] (let [form (doto (read rdr) str)]
               (cond
                (ns-decl? form) form
                (comment? form) (recur)
                :else nil)))
    (catch Exception e nil)))

(defn read-file-ns-decl
  "Attempts to read a (ns ...) declaration from file, and returns the
  unevaluated form.  Returns nil if read fails, or if the first form
  is not a ns declaration."
  [#^File file]
  (with-open [rdr (PushbackReader. (BufferedReader. (FileReader. file)))]
    (read-ns-decl rdr)))

(defn parse-ns-sym [file-path]
  (-> file-path
      (java.io.File.)
      read-file-ns-decl))


(defn namespaces-in [path]
  (->> path
       (java.io.File.)
       file-seq
       (map #(.getAbsolutePath %))
       (filter #(.endsWith % ".clj"))
       (map parse-ns-sym)
       (map second)
       (filter identity)))

(defn var-data [nss]
  (->> nss
       (map ns-publics)
       (map (fn [publics]
              (map (fn [[sym var]]
                     {:sym sym
                      :var var
                      :meta (meta var)})
                   publics)))
       flatten))

(defn parse-route [{:keys [path method middleware] :as route}]
  (if (string? route)
    {:path route}
    route))

(defn routes-in-nss [nss]
  (->> nss
       var-data
       (filter has-route?)
       (map (fn [{:keys [meta var]}]
              (let [route (parse-route (:nsfw/route meta))
                    res (assoc route :handler var :skip-middleware (:nsfw/skip-middleware meta))]
                res)))))

(defn comps-in-nss [nss]
  (->> nss
       var-data
       (filter has-comp-tag?)
       (map (fn [{:keys [meta var]}]
              {:tag (:nsfw/comp-tag meta)
               :var var}))))

(defn middleware-in-nss [nss]
  (->> nss
       var-data
       (filter has-middleware-tag?)
       (map (fn [{:keys [meta var]}]
              (merge {:var var :ns (-> meta :ns str symbol)}
                     @var)))
       #_(reduce #(assoc %1 (:ns %2) (:var %2)) {})))

(defn apply-middleware [mws handler]
  ((apply comp mws) handler))

(defn load-routes [path !components]
  (let [route-nss (namespaces-in path)]
    (doseq [ns route-nss]
      (require ns))
    (let [comps (->> route-nss
                     comps-in-nss
                     (group-by :tag)
                     (map (fn [[k vs]]
                            [k (-> vs first :var deref)]))
                     (into {}))
          routes (routes-in-nss route-nss)
          middlewares (middleware-in-nss route-nss)]
      (swap! !components merge comps)
      (fn [req]
        (let [match (->> routes
                         (map (fn [{:keys [method path handler skip-middleware] :as route}]
                                (let [route-params (clout/route-matches path req)
                                      method-match (or (nil? method)
                                                       (= :any method)
                                                       (= method (:request-method req)))]
                                  {:match (and route-params method-match)
                                   :route-params route-params
                                   :handler handler})))
                         (filter :match)
                         first)
              route-ns (-> match :handler meta :ns str symbol)
              middleware (->> middlewares
                              (filter #((:pred %) req))
                              (mapcat :middleware)
                              reverse)
              handler (:handler match)
              handler (if (and middleware
                               (not (-> match :skip-middleware)))
                        (apply-middleware middleware handler)
                        handler)]
          (when match
            (handler (assoc req
                       :params (merge (:route-params match)
                                      (:params req))
                       :route (assoc match :middleware middleware)))))))))