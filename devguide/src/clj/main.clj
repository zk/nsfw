(ns main
  (:require [nsfw.server :as server]
            [devguide.config :as config]
            [devguide.entry :as entry]
            [garden.core :as garden]
            [devguide.css :as css]))


(defn compile-css []
  (garden/css
    {:output-to "resources/public/css/app.css"
     :pretty-print? false
     :vendors ["webkit" "moz" "ms"]
     :auto-prefix #{:justify-content
                    :align-items
                    :flex-direction
                    :flex-wrap
                    :align-self
                    :transition
                    :transform}}
    css/app))

(defn start-app []
  (compile-css)
  (let [res (server/start-aleph
              (entry/handler)
              {:port config/port})]
    (prn "*** Server Up ***")
    res))

(defn stop-app [app]
  (server/stop-aleph app))
