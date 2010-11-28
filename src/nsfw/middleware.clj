(ns nsfw.middleware)

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