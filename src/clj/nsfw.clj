(ns nsfw
  (:use (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]
        [ring.middleware.session.cookie :only (cookie-store)]
        [ring.middleware.reload :only (wrap-reload)])
  (:require [nsfw.env :as env]
            [nsfw.app :as app]
            [nsfw.server :as server]
            [clojure.tools.nrepl.server :as repl]
            [ring.middleware.reload-modified :as reload]
            [net.cgrand.moustache :as moustache]))

(def !reload-nss (atom []))

(defn start-repl [port]
  (repl/start-server :port port))

(defn load-nss [ns-syms]
  (reset! !reload-nss ns-syms))

(defn serve-routes [h !nss]
  (fn [r]
    (if-let [res ((app/load-routes @!nss) r)]
      res
      (h r))))

(defn catch-all [r]
  {:body "NO HANDLER"})

(defn app [& opts]
  (let [{:keys [repl-port
                server-port
                entry-point
                app-nss
                session]}
        (apply hash-map opts)]
    (when repl-port
      (start-repl repl-port))
    (server/start :entry (moustache/app
                          (app/debug-exceptions true)
                          (wrap-reload :dirs ["src/clj"])
                          wrap-file-info
                          (wrap-file "resources/public" {:allow-symlinks? true})
                          wrap-params
                          wrap-nested-params
                          wrap-keyword-params
                          (wrap-session (or session {}))
                          (serve-routes !reload-nss)
                          catch-all)
                  :port server-port)))