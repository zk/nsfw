(ns nsfw.gqi
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn mutate [uri payload]
  (async/map
    (fn [{:keys [success body] :as resp}]
      (if success
        body
        {:success? false
         :error "Error contacting server"
         :resp resp}))
    [(http/post
       uri
       {:transit-params payload})]))


(defn query [uri payload]
  (async/map
    (fn [{:keys [success body] :as resp}]
      (if success
        body
        {:success? false
         :error "Error contacting server"
         :resp resp}))
    [(http/post
       uri
       {:transit-params payload})]))
