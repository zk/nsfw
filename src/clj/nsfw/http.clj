(ns nsfw.http
  "Request / response utilities"
  ;;(:use [plumbing.core])
  (:require [clojure.edn :as edn]
            [nsfw.util :as util]
            [hiccup.page]
            [hiccup.core]
            [clojure.string :as str]
            [bidi.ring :as bidi-ring]
            [bidi.bidi :as bidi]
            [ring.middleware
             file
             file-info
             session
             params
             nested-params
             multipart-params
             keyword-params
             resource
             content-type]
            [ring.middleware.session.cookie :only (cookie-store)]
            [ring.util.response :only (response content-type)])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defn html-resp [body]
  {:headers {"content-type" "text/html;charset=utf-8"}
   :body body})

(defn edn [body]
  {:headers {"content-type" "application/edn;charset=utf-8"}
   :body (pr-str body)})

(def header-for {:html {"Content-Type" "text/html; charset=utf-8"}
                 :js {"Content-Type" "text/javascript; charset=utf-8"}})

(defn headers [& opts]
  (apply merge (map header-for opts)))

(def html-header (headers :html))

(defn temp-redirect [loc opts]
  (merge
    {:headers {"Location" loc}
     :status 302}
    opts))

(defn perm-redirect [loc opts]
  (merge
    {:headers {"Location" loc}
     :status 301}
    opts))

(def redirect temp-redirect)

(defn temp-redirect [loc opts]
  (merge
    {:headers {"Location" loc}
     :status 302}
    opts))

(defn decode-body [content-length body]
  (when (and content-length
             (> content-length 0))
    (let [buf (byte-array content-length)]
      (.read body buf 0 content-length)
      (.close body)
      (String. buf))))

(defn response-body
  "Turn a HttpInputStream into a string."
  [{:keys [content-length body]}]
  (if (string? body)
    body
    (decode-body content-length body)))

(defn decode-edn-body [r]
  (-> r response-body edn/read-string))

(defn wrap-edn-request [h]
  (fn [r]
    (let [content-type-raw (-> r :headers (get "content-type"))]
      (if (and content-type-raw
               (.contains content-type-raw "application/edn"))
        (h (assoc r :edn-body (decode-edn-body r)))
        (h r)))))

(defn render-edn [body]
  {:headers {"Content-Type" "application/edn; encoding=utf-8"}
   :status 200
   :body (pr-str body)})

(defn decode-content-type [cts]
  (let [parts (str/split cts #";")
        media-type (some->
                     (first parts)
                     str/trim)]
    {:media-type media-type
     :params (->> parts
                  rest
                  (map str/trim)
                  (remove empty?)
                  (map #(str/split % #"="))
                  (map (fn [[k v]]
                         [(keyword k) v]))
                  (into {}))}))

(defn content-type [request]
  (when-let [cts (or (get-in request [:headers "Content-Type"])
                     (get-in request [:headers "content-type"]))]
    (decode-content-type cts)))

(defn json-content? [req]
  (= "application/json"
     (:media-type (content-type req))))

(defn render-json
  ([opts body]
     (merge {:headers {"Content-Type" "application/json;encoding=utf-8"}
             :status 200
             :body (util/to-json body)}
            opts))
  ([body]
     (render-json {} body)))

(defn wrap-decode-json-body [h]
  (fn [r]
    (if (json-content? r)
      (try
        (let [json-body (-> r util/response-body util/from-json)]
          (h (assoc r :json-body json-body)))
        (catch JsonParseException e
          (render-json
            {:status 400}
            {:message "Problems parsing JSON"})))
      (h r))))

(defn html-response? [r]
  (= "text/html"
     (:media-type (content-type r))))

(defn hiccup->html-string [body]
  (if-not (vector? body)
    body
    (if (= :html5 (first body))
      (hiccup.page/html5 (rest body))
      (hiccup.core/html body))))

(defn wrap-html-response
  "Render hiccup vector into an HTML string when the content type is
  text/html."
  [h]
  (fn [req]
    (let [resp (h req)]
      (if (and (html-response? resp)
               (vector? (:body resp)))
        (update-in resp [:body] hiccup->html-string)
        resp))))

(defn wrap-json-response [h]
  (fn [r]
    (let [res (h r)]
      (-> res
          (update-in [:body] util/to-json)
          (assoc-in [:headers "Content-Type"]
            "application/json;charset=utf-8")))))

(defn wrap-json-request [h]
  (fn [r]
    (if (= "application/json"
           (:media-type (content-type r)))
      (h (update-in r [:body] util/from-json))
      (h r))))

(defn wrap-transit-response [h & [handlers]]
  (fn [r]
    (let [res (h r)]
      (-> res
          (update-in [:body] #(util/to-transit % handlers))
          (assoc-in [:headers "Content-Type"]
            "application/transit+json;charset=utf-8")))))

(defn wrap-transit-request [h & [handlers]]
  (fn [r]
    (if (= "application/transit+json"
           (:media-type (content-type r)))
      (h (update-in r [:body] #(util/from-transit % handlers)))
      (h r))))

(defn routes->bidi [routes]
  ["" (->> routes
           (group-by :path)
           (map (fn [[path route-fragments]]
                  [path (->> route-fragments
                             (map (fn [{:keys [method handler]}]
                                    [method handler]))
                             (into {}))]))
           (into {}))])

(defn apply-middleware-to-handler [{:keys [handler middleware] :as route}]
  (assoc route
    :handler
    (reduce
      (fn [h mw]
        (mw h))
      handler
      middleware)))

(defn routes->handler [routes]
  (->> routes
       (map apply-middleware-to-handler)
       routes->bidi
       bidi-ring/make-handler))

(defn wrap-404 [h handler]
  (fn [r]
    (let [res (h r)]
      (if-not (nil? res)
        res
        (if handler
          (handler res)
          {:status 404
           :body "not found!!"})))))

(defn wrap-params [h & [opts]]
  (-> h
      ring.middleware.keyword-params/wrap-keyword-params
      (ring.middleware.nested-params/wrap-nested-params opts)
      (ring.middleware.multipart-params/wrap-multipart-params opts)
      (ring.middleware.params/wrap-params opts)))

(defn wrap-file [h dir]
  (-> h
      (ring.middleware.file/wrap-file dir {:allow-symlinks? true})
      (ring.middleware.file-info/wrap-file-info)))

(defn wrap-resource [h dir]
  (-> h
      (ring.middleware.resource/wrap-resource dir)
      ring.middleware.file-info/wrap-file-info
      ring.middleware.content-type/wrap-content-type))

(defn wrap-cookie-session [h domain key]
  (-> h
      (ring.middleware.session/wrap-session
        {:store (ring.middleware.session.cookie/cookie-store
                  {:domain domain
                   :key key})})))

(defn wrap-exception [h handler]
  (fn [r]
    (try
      (h r)
      (catch Exception e
        (handler (assoc r :exception e))))))

(defn wrap-context [h k v]
  (fn [r]
    (h (assoc r k v))))

(defn cljs-page-template [{:keys [js css env data
                                  body-class head
                                  meta-named
                                  title]}]
  (html-resp
    [:html5
     (vec
       (concat
         [:head
          (when title
            [:title title])]
         head
         (->> meta-named
              (map (fn [[k v]]
                     [:meta {:name k :content v}])))
         (for [css css]
           (if (string? css)
             [:link {:rel "stylesheet" :href css}]
             css))
         [(vec
            (concat
              [:body
               {:class body-class}]
              (when env
                [[:script {:type "text/javascript"}
                  (util/write-page-data :env env)]])
              (for [js js]
                (cond
                  (string? js) [:script {:type "text/javascript" :src js}]
                  (map? js) [:script js]
                  :else js))))]))]))

(def bootstrap3
  {:css [{:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
          :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
          :crossorigin "anonymous"}]
   :js []
   :head []
   :body []})

(defn apply-middleware [mw routes-map]
  (->> routes-map
       (map (fn [[k v]]
              [k (mw v)]))
       (into {})))

(defn compile-spec [specs]
  (apply
    merge-with
    (fn [v1 v2]
      (if (and (map? v1) (map? v2))
        (compile-spec [v1 v2])
        (concat v1 v2)))
    specs))

(defn css-html [csss]
  (->> csss
       (remove nil?)
       (map (fn [css]
              [:link
               (merge
                 {:rel "stylesheet"}
                 (if (string? css)
                   {:href css}
                   css))]))))

(defn js-html [jss]
  (->> jss
       (remove nil?)
       (map (fn [js]
              (cond
                (string? js) [:script {:type "text/javascript" :src js}]
                (map? js) [:script js]
                :else js)))))

(defn csp-str->map [s]
  (when s
    (->> (str/split (str/trim s) #";")
         (map (fn [csp-entry]
                (let [parts (str/split (str/trim csp-entry) #"\s+")
                      directive (first parts)
                      args (rest parts)]
                  [directive args])))
         (into {}))))

(defn csp-map->str [m]
  (->> m
       (map (fn [[k vs]]
              (str
                k
                " "
                (->> vs
                     (interpose " ")
                     (apply str)))))
       (interpose "; ")
       (apply str)))

(defn add-to-content-security-policy [resp addition]
  (let [csp-str (-> resp
                    :headers
                    (get "Content-Security-Policy"))
        csp-map (csp-str->map csp-str)
        new-csp-str (csp-map->str
                      (reduce
                        (fn [csp-map [directive to-add-args]]
                          (update
                            csp-map
                            directive
                            (fn [args]
                              (distinct
                                (concat
                                  args
                                  to-add-args)))))
                        csp-map
                        addition))]
    (assoc-in
      resp
      [:headers "Content-Security-Policy"]
      new-csp-str)))

(defn render-spec [specs]
  (let [{:keys [css js head body env body-attrs
                content-security-policy]
         :as compiled-spec} (compile-spec specs)
         env-src (when env
                   (util/write-page-data :env env))
         env-sha512 (when env
                      (util/to-base64 (util/sha512-bytes env-src)))
         resp (html-resp
                [:html5
                 (vec
                   (concat
                     [:head]
                     (css-html css)
                     head))
                 (vec
                   (concat
                     [:body
                      body-attrs]
                     body
                     (when env
                       [[:script {:type "text/javascript"}
                         (util/write-page-data :env env)]])
                     (js-html js)))])
         resp (add-to-content-security-policy
                resp
                content-security-policy)]
    (if (and env (not (str/includes?
                        (-> resp
                            :headers
                            (get "Content-Security-Policy"))
                        "unsafe-inline")))
      (add-to-content-security-policy
        resp
        {"script-src" [(str "'sha512-" env-sha512 "'")]})
      resp)))

(defn cljs-page-template
  [{:keys [js css env data
           body-class head meta-named
           title]}]
  (html-resp
    [:html5
     (vec
       (concat
         [:head
          (when title
            [:title title])]
         head
         (->> meta-named
              (map (fn [[k v]]
                     [:meta {:name k :content v}])))
         (for [css css]
           (if (string? css)
             [:link {:rel "stylesheet" :href css}]
             css))
         [(vec
            (concat
              [:body
               {:class body-class}]
              (when env
                [[:script {:type "text/javascript"}
                  (util/write-page-data :env env)]])
              (for [js js]
                (if (string? js)
                  [:script {:type "text/javascript" :src js}]))))]))]))

(defn compile-handlers [handlers]
  (->> handlers
       (map (fn [[k fn-or-map]]
              [k (if (map? fn-or-map)
                   (let [{:keys [middleware data render]} fn-or-map
                         middleware (or middleware identity)
                         data (or data identity)]
                     (middleware
                       (fn [req]
                         (render (data req)))))
                   fn-or-map)]))
       (into {})))

(defn gen-handler [routes handlers]
  (let [compiled-handlers (compile-handlers handlers)]
    (fn [{:keys [uri path-info] :as req}]
      (let [path (or path-info uri)
            {:keys [handler route-params] :as match-context}
            (bidi/match-route* routes path req)
            handler-fn (get compiled-handlers handler)]
        (when handler-fn
          (handler-fn
            (-> req
                (update-in [:params] merge route-params)
                (update-in [:route-params] merge route-params))))))))
