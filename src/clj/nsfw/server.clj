(ns nsfw.server
  (:require [aleph.http :as http]))

(defn start-aleph [handler opts]
  (http/start-server handler opts))

(defn stop-aleph [server]
  (when server
    (.close server)))

(defn gen-start-app
  "Return a function that, when called, will start your web app."
  [{:keys [create-ctx
           destroy-ctx
           create-handler
           server-opts]}]
  (fn []
    (let [context (if create-ctx
                    (create-ctx)
                    {})
          server (start-aleph
                   ((or create-handler
                        (fn [ctx]
                          (fn [r] {:body "Default nsfw.server/gen-app handler. Specify a :create-handler function."})))
                    context)
                   (or server-opts {:port 8080}))]
      (prn "*** Server Up ***")
      (fn []
        (stop-aleph server)
        (when destroy-ctx
          (destroy-ctx context))))))

(defn gen-stop-app []
  (fn [f]
    (when f (f))))
