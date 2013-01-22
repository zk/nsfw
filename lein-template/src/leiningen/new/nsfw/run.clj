(ns {{name}}.run
  (:require [nsfw.server :as server]
            [nsfw.env :as env]
            [ring.middleware.reload-modified :as reload]
            [{{name}}.entry :as entry]
            [clojure.tools.nrepl.server :as repl]))

(defn start-repl [port]
  (repl/start-server :port port))

(def root-entry
  (-> #'entry/routes
      (reload/wrap-reload-modified ["src/clj"])))

(defn -main [& args]
  (when-let [repl-port (env/int :repl-port)]
    (start-repl repl-port))
  (server/start :entry root-entry
                :port (env/int :port 8080)))