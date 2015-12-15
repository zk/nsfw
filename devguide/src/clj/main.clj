(ns main
  (:require [nsfw.server :as server]
            [devguide.config :as config]
            [devguide.entry :as entry]))


(def start-app
  (server/gen-start-app
    {:create-handler entry/handler
     :create-ctx entry/create-ctx
     :destroy-ctx entry/destroy-ctx
     :server-opts {:port config/port}}))

(def stop-app (server/gen-stop-app))
