(ns nsfw.devbus
  (:require [aleph.http :as http]
            [nsfw.http :as nhttp]
            [nsfw.devbus.css :as dcss]
            [garden.core :as garden]
            [nsfw.server :as server]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [taoensso.timbre :as log]
            [manifold.bus :as bus]
            [nsfw.env :as env]
            [nsfw.util :as nu]))

(def MAX_FRAME_PAYLOAD (* 1024 1024 1024)) ; 1G
(def MAX_FRAME_SIZE (* 1024 1024 1024))

(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(def subscriptions (bus/event-bus))

(defn send-heartbeat []
  (bus/publish! subscriptions "devbus" "[\"~:heartbeat\"]"))

(defn handler
  [req]
  (d/let-flow [conn (d/catch
                        (http/websocket-connection req
                          {:max-frame-payload MAX_FRAME_PAYLOAD
                           :max-frame-size MAX_FRAME_SIZE})
                        (fn [e] nil))]
    (if-not conn
      non-websocket-request
      (do
        (s/connect
          (bus/subscribe subscriptions "devbus")
          conn)
        (s/consume
          #(bus/publish! subscriptions "devbus" %)
          (->> conn
               (s/buffer 100)))

        {:status 200
         :body "ok"}))))

(def gui-handler
  (-> (fn [req]
        (nhttp/render-spec
          [{:js ["cljs/devbus.js"]
            :css ["https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
                  "https://fonts.googleapis.com/css?family=Source+Code+Pro:400,700"]
            :head [[:style {:type "text/css"} dcss/rules-string]
                   [:script {:style "text/javascript"}
                    "var ENV = " (nu/to-json
                                   {:aws-access-id (env/str :aws-access-id)
                                    :aws-secret-key (env/str :aws-secret-key)})]]
            :body [[:div#entry]]}]))
      nhttp/wrap-html-response))

(defonce !server (atom nil))

(defn start-server! [{:keys [port] :as opts}]
  (if @!server
    (println "Existing server instance, stop first.")
    (let [server (server/start-aleph
                   (->
                     (fn [{:keys [uri] :as req}]
                       (condp = uri
                         "/" (gui-handler req)
                         "/devbus" (handler req)
                         {:status 404
                          :body "not found"}))
                     (nhttp/wrap-resource "public"))
                   opts)]
      (println "Server started" opts)
      (reset! !server server))))

(defn stop-server! []
  (if-not @!server
    (println "No server found to stop")
    (do
      (server/stop-aleph @!server)
      (println "Server stopped")
      (reset! !server nil))))
