(ns nsfw.middleware
  (:use [nsfw.util]
        (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]))

(defn wrap-log-request [handler]
  (fn [r]
    (when (not= "/favicon.ico" (:uri r))
      (let [start (System/nanoTime)
            res (handler r)
            diff (/ (- (System/nanoTime) start) 1000000.0)]
        (println "[" (:uri r) "] - " (java.util.Date.))
        #_(println (let [writer (java.io.StringWriter. )]
                   (pprint r writer)
                   (str writer)))
        (println "^^^ took" diff "ms")
        res))))

(defn wrap-always-session [handler]
  (fn [r]
    (merge {:session (:session r)} (handler r))))

(defn wrap-stacktrace [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (web-stacktrace e req)}))))


(defn wrap-web-defaults
  "Wraps a good default set of middleware for webapps.  Includes
  session, file handling, and params."
  [handler & opts]
  (let [opts (apply hash-map opts)
        session-store (get opts :session-store (memory-store
                                                (get opts :session-atom (atom {}))))
        public-path (get opts :public-path "resources/public")]
    (-> handler
        wrap-keyword-params
        wrap-nested-params
        wrap-params
        (wrap-file public-path)
        wrap-file-info
        (wrap-session {:store session-store})
        wrap-stacktrace)))
