(ns {{name}}.run
  (:require [nsfw.server :as server]
            [nsfw.env :as env]
            [nsfw.app :as app]
            [ring.middleware.reload-modified :as reload]
            [{{name}}.entry :as entry]
            [clojure.tools.nrepl.server :as repl]))

;; Config from environment variables

(def debug-exceptions? (env/bool :debug-exceptions false))
(def repl-port (env/int :repl-port))
(def server-port (env/int :port 8080))


(defn start-repl [port]
  (when port
    (repl/start-server :port port)))

;; Request handling
(def root-entry
  (-> #'entry/app
      (reload/wrap-reload-modified ["src/clj"])
      (app/debug-exceptions debug-exceptions?)))

;; Main
(defn -main [& args]
  (start-repl repl-port)
  (server/start :entry root-entry
                :port server-port))