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
            [clojure.string :as str]))

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

(defmacro route-default [store & routes]
  `(moustache/app
    (wrap-session {:store ~store})
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
