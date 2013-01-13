(ns nsfw-site.run
  (:require [nsfw.server :as server]
            [nsfw.env :as env]
            [ring.middleware.reload-modified :as reload]
            [nsfw-site.entry :as entry]))

(def root-entry
  (-> #'entry/routes
      (reload/wrap-reload-modified ["src/clj"])))

(defn -main [& args]
  (server/start :entry root-entry
                :port (env/int :port 8080)))