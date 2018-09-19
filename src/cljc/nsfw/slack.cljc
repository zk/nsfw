(ns nsfw.slack
  #?(:clj
     (:require [clj-http.client :as client]
               [clojure.string :as str]
               [nsfw.util :as nu])
     :cljs
     (:require [cljs-http.client :as client]
               [clojure.string :as str]
               [nsfw.util :as nu])))

(defn post-to-channel [webhook-url {:keys [message
                                           channel
                                           icon
                                           attachments]}]
  (try
    (client/post
      webhook-url
      {:form-params
       {:payload
        (nu/to-json
          (merge
            {:channel channel
             :text message
             :icon_url icon
             :username "btvbot"}
            (when attachments
              {:attachments attachments})))}})
    #?(:clj (catch Exception e
              (prn e))
       :cljs (catch js/Error e
               (prn e)))))
