(ns nsfw.server
  (:require [ring.adapter.jetty :as jetty]))

(def server nil)

(defn stop []
  (when server
    (.stop server)))

(defn start [entry-handler & [port]]
  (stop)
  (alter-var-root
   (var server)
   (fn [val]
     (jetty/run-jetty entry-handler {:port (if port port 8080) :join? false}))))



