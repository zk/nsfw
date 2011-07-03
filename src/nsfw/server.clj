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
  (:require [ring.adapter.jetty :as jetty])
  (:import [org.mortbay.thread QueuedThreadPool]))

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

(defn set-threads [server-atom]
  (let [server (:server @server-atom)
        {:keys [max-threads min-threads]}
        (:opts @server-atom)]
    (when server
      (let [tp (.getThreadPool server)]
        (when min-threads (.setMinThreads tp min-threads))
        (when max-threads (.setMaxThreads tp max-threads))))))

(defn stop [server-atom]
  (when-let [server (:server @server-atom)]
    (.stop server))
  (swap! server-atom assoc :server nil))

(defn restart [server-atom & override-opts]
  (let [override-opts (apply hash-map override-opts)
        opts (merge (:opts @server-atom)
                    override-opts)]
    (stop server-atom)
    (let [handler (:handler @server-atom)
          res (swap! server-atom
                     assoc
                     :opts opts
                     :server (jetty/run-jetty handler opts))]
      (set-threads server-atom)
      res)))

(def start restart)
