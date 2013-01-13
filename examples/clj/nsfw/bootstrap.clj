(ns nsfw.bootstrap
  (:require [nsfw.server :as server]
            [nsfw.refresher :as refresher]
            [nsfw.bootstrap-entry :as entry]
            [ring.middleware.reload-modified :as reload]))

(def root-entry
  (-> #'entry/routes
      (reload/wrap-reload-modified ["examples" "src"])))

(defn -main [& args]
  (server/start :entry root-entry))

(-main)