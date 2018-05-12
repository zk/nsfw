(ns nsfw.slack
  #?(:clj
     (:require [clj-http.client :as client]
               [clojure.string :as str])
     :cljs
     (:require [clj-http.client :as client]
               [clojure.string :as str])))

(defn post-to-channel [webhook-url {:keys [message
                                           channel
                                           icon
                                           attachments]}]
  (try
    (client/post
      webhook-url
      {:form-params (merge
                      {:channel channel
                       :text message
                       :icon_url icon
                       :username "btvbot"}
                      (when attachments
                        {:attachments attachments}))
       :content-type :json})
    (catch Exception e
      (prn e))))
