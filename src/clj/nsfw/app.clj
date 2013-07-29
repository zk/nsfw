(ns nsfw.app
  (:use [nsfw.util]
        (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]
        [ring.middleware.session.cookie :only (cookie-store)])
  (:require [net.cgrand.moustache :as moustache]
            [nsfw.html :as html]
            [nsfw.middleware :as nm]
            [nsfw.util :as nu]
            [clojure.string :as str]
            [clout.core :as clout]))

(defn debug-exceptions [h enabled?]
  (fn [r]
    (if-not enabled?
      (h r)
      (try
        (h r)
        (catch Exception e
          {:header {"Content-Type" "text/html"}
           :status 200
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
                   [:pre (nu/pp-str r)]])})))))

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

(defn session-store
  [type & rest]
  (condp = type
    :atom (if-not (empty? rest)
            (memory-store (first rest))
            (memory-store))

    ;; encrypted cookie
    (if-not (empty? rest)
      (cookie-store (first rest))
      (cookie-store))))

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
  (-> m :meta :route))

(defn load-routes [route-nss]
  (doseq [ns route-nss]
    (require ns))
  (let [routes (->> route-nss
                    (map ns-publics)
                    (map (fn [publics]
                           (map (fn [[sym var]]
                                  {:sym sym
                                   :var var
                                   :meta (meta var)})
                                publics)))
                    flatten
                    (filter has-route?)
                    (map (fn [{:keys [meta var]}]
                           {:route (:route meta)
                            :handler var})))]
    (fn [req]
      (let [match (->> routes
                       (map (fn [{:keys [route handler]}]
                              {:route-params (clout/route-matches route req)
                               :handler handler}))
                       (remove #(-> % :route-params nil?))
                       first)]
        (when match
          ((:handler match)
           (assoc req :route (:route-params match))))))))