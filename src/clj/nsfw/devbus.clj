(ns nsfw.devbus
  (:require [aleph.http :as http]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [manifold.bus :as bus]))

(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(def subscriptions (bus/event-bus))

(defn send-heartbeat []
  (bus/publish! subscriptions "devbus" "[\"~:heartbeat\"]"))

(defn handler
  [req]
  (d/let-flow [conn (d/catch
                        (http/websocket-connection req)
                        (fn [e] nil))]
    (if-not conn
      non-websocket-request
      (do
        (s/connect
          (bus/subscribe subscriptions "devbus")
          conn)
        (s/consume
          #(bus/publish! subscriptions "devbus" %)
          (->> conn
               (s/buffer 100)))

        {:status 200
         :body "ok"}))))
