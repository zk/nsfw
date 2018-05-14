(ns nsfw.twilio
  (:require
   [nsfw.util :as nu]
   #? (:cljs
       [cljs-http.client :as http]
       :clj
       [clj-http.client :as http])
   #? (:clj
       [clojure.core.async :as async
        :refer [go <! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]]
       :cljs
       [cljs.core.async :as async
        :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]]))
  #? (:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]])))

(defn <post [uri params & [config]]
  (let [ch (chan)]
    (go
      (put! ch
        (let [res #?(:clj
                     (http/post
                       uri
                       (merge
                         {:form-params params}
                         config))
                     :cljs
                     (<! (http/post
                           uri
                           (merge
                             {:form-params params}
                             config))))
              {:keys [success
                      body
                      headers
                      error-code
                      error-text
                      status] :as resp}
              res]
          (cond
            (= :timeout error-code)
            [nil
             {:timeout? true
              :error error-text}
             res]

            (not= 200 status)
            [nil
             {:error "Tunnel response status not 200"
              :response res}
             res]

            #? (:cljs (not success))
            #? (:cljs [nil
                       {:error "Couldn't contact server"}
                       res])

            :else [body nil res]))))
    ch))

(defn <send-sms [{:keys [account-id
                         auth-token]}
                 {:keys [from to body]}]
  (<post
    (str "https://api.twilio.com/2010-04-01/Accounts/" account-id "/Messages.json")
    {:To to
     :From from
     :Body body}
    {:basic-auth [account-id auth-token]}))
