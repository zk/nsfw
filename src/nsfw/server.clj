(ns nsfw.server
  "Easily manage jetty instances.

   Example:
   (def server (atom nil))
   (start-server server (fn [req] {:body \"hi\"}) {:port 4321})

   ;; Change to port 4000 and join. Will shut down server bound to
   ;; 4321.
   (start-server server (fn [req] {:body \"lo\"}) {:port 4000 :join? true})"
  (:require [ring.adapter.jetty :as jetty]))


;; # Server

(def server-defaults {:port 8080 :join? false :max-threads 100})

(defn start-server
  "Start a jetty server. Opts include:
   {:port        8080
    :join?       false  ; Join server thread?
    :max-threads 100    ; Request thredpool cap.
    :min-threads 10}   
"
  [server-atom entry-point & [opts]]
  (let [opts (merge server-defaults opts)]
    (when @server-atom
      (.stop @server-atom))
    (swap! server-atom
           (fn [& _] (jetty/run-jetty entry-point opts)))
    (doto (.getThreadPool @server-atom)
      (.setMaxThreads (:max-threads opts))
      (.setMinThreads (:min-threads opts)))
    server-atom))

(def restart-server start-server)
