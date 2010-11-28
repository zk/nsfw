(ns <(str project-name)>.boot
    (:use [ring.middleware file file-info params nested-params keyword-params session]
          [ring.middleware.session.memory]
          [clojure.stacktrace]
          [clojure.pprint :only (pprint)]
          [nsfw.util :only (web-stacktrace)]
          [nsfw.middleware])
    (:require [<(str project-name)>.routes]
              [nsfw.server]))

(def sessions (atom {}))

(defn entry-handler [req]
  (try
    ((-> <(str project-name)>.routes/routes
         (wrap-keyword-params)
         (wrap-nested-params)
         (wrap-params)
         (wrap-always-session)
         (wrap-session {:store (memory-store sessions)})
         (wrap-file-info)
         (wrap-file "resources/public")
         (wrap-log-request)) req)
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "text/html"}
       :body (web-stacktrace e req)})))

(defn start-server [& [port]]
  (nsfw.server/start (var entry-handler) (if port port 8080)))