(ns nsfw
  (:use (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]
        [ring.middleware.session.cookie :only (cookie-store)]
        [ring.middleware.reload :only (wrap-reload)]
        [clojure.pprint])
  (:require [nsfw.env :as env]
            [nsfw.app :as app]
            [nsfw.server :as server]
            [nsfw.html :as html]
            [nsfw.http :as http]
            [clojure.tools.nrepl.server :as repl]
            [ring.middleware.reload-modified :as reload]
            [net.cgrand.moustache :as moustache]))

(defn start-repl [port]
  (repl/start-server :port port))

(defn serve-routes [h path !components]
  (fn [r]
    (if path
      (if-let [res ((app/load-routes path !components) r)]
        res
        (h r))
      (h r))))

(defn catch-all [r]
  {:body "NO HANDLER"})

(defonce !components (atom {}))

(defn app [& opts]
  (let [{:keys [repl-port
                server-port
                entry-point
                app-nss
                session
                on-err
                autoload]}
        (apply hash-map opts)]
    (try (when repl-port
           (start-repl repl-port))
         (catch Exception e
           (println "Can't start REPL on port" repl-port)
           (println e)))
    (server/start :entry (moustache/app
                          (app/debug-exceptions true on-err)
                          (wrap-reload :dirs ["src/clj"])
                          wrap-file-info
                          (wrap-file "resources/public" {:allow-symlinks? true})
                          wrap-params
                          wrap-nested-params
                          wrap-keyword-params
                          (wrap-session (or session {}))
                          (serve-routes autoload !components)
                          catch-all)
                  :port server-port)))

(def transform-components (html/mk-transformer !components))

(defn render [& body]
  (-> body
      transform-components
      http/html))