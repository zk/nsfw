(ns nsfw.server
  "Easily manage jetty instances.

   Usage:
   ;; Create a server
   (def s (make my-entry-handler :port 9090))

   ;; Start server
   (start s)

   ;; Stop server
   (stop s)

   ;; Restart a server
   (restart s)"
  (:require [ring.adapter.jetty :as jetty]))

(def opts-defaults
  {:port 8080
   :join? false})

(defn make [entry-handler & opts]
  (let [opts (->> opts
                  (apply hash-map)
                  (merge opts-defaults))]
    (atom {:opts opts
           :handler entry-handler
           :server nil})))

(defn stop [server-atom]
  (when-let [server (:server @server-atom)]
    (.stop server))
  (swap! server-atom assoc :server nil))


(defn restart [server-atom]
  (stop server-atom)
  (let [handler (:handler @server-atom)
        opts (:opts @server-atom)]
    (swap! server-atom
           assoc :server (jetty/run-jetty handler opts))))

(def start restart)
