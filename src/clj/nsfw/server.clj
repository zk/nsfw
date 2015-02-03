(ns nsfw.server
  (:require [aleph.http :as http]))

(defn start-aleph [handler opts]
  (http/start-server handler opts))

(defn stop-aleph [server]
  (when server
    (.close server)))
