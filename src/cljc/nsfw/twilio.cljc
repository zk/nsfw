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
                         {:form-params params
                          :throw-exceptions false}
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

            (not (<= 200 status 299))
            [nil
             {:error "Tunnel response status not 200"
              :response res}
             res]

            #? (:cljs (not success))
            #? (:cljs [nil
                       {:error "Couldn't contact server"}
                       res])

            :else [(nu/from-json body) nil res]))))
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


#_[nil
   {:error "Tunnel response status not 200",
    :response
    {:request-time 842,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :chunked? false,
     :reason-phrase "CREATED",
     :headers
     {"Access-Control-Expose-Headers" "ETag",
      "Access-Control-Allow-Headers"
      "Accept, Authorization, Content-Type, If-Match, If-Modified-Since, If-None-Match, If-Unmodified-Since",
      "Twilio-Request-Id" "RQ16f0e18d5f8445e899fb7e30e1e77c63",
      "Content-Type" "application/json",
      "Access-Control-Allow-Origin" "*",
      "Content-Length" "809",
      "Strict-Transport-Security" "max-age=15768000",
      "Connection" "Close",
      "Twilio-Concurrent-Requests" "1",
      "Twilio-Request-Duration" "0.099",
      "Access-Control-Allow-Methods" "GET, POST, DELETE, OPTIONS",
      "Date" "Tue, 15 May 2018 06:26:36 GMT",
      "Access-Control-Allow-Credentials" "true",
      "X-Powered-By" "AT-5000",
      "X-Shenanigans" "none"},
     :orig-content-encoding nil,
     :status 201,
     :length
     809,
     :body
     "{\"sid\": \"SMcc677b1f959d480cabf67290a98fca9e\", \"date_created\": \"Tue, 15 May 2018 06:26:36 +0000\", \"date_updated\": \"Tue, 15 May 2018 06:26:36 +0000\", \"date_sent\": null, \"account_sid\": \"ACbb3c91e2e1bd759c2f555a72f865d89b\", \"to\": \"+14157580667\", \"from\": \"+14154232364\", \"messaging_service_sid\": null, \"body\": \"Sent from your Twilio trial account - testing\", \"status\": \"queued\", \"num_segments\": \"1\", \"num_media\": \"0\", \"direction\": \"outbound-api\", \"api_version\": \"2010-04-01\", \"price\": null, \"price_unit\": \"USD\", \"error_code\": null, \"error_message\": null, \"uri\": \"/2010-04-01/Accounts/ACbb3c91e2e1bd759c2f555a72f865d89b/Messages/SMcc677b1f959d480cabf67290a98fca9e.json\", \"subresource_uris\": {\"media\": \"/2010-04-01/Accounts/ACbb3c91e2e1bd759c2f555a72f865d89b/Messages/SMcc677b1f959d480cabf67290a98fca9e/Media.json\"}}",
     :trace-redirects []}}
   {:request-time 842,
    :repeatable? false,
    :protocol-version {:name "HTTP", :major 1, :minor 1
                       },
    :streaming? true,
    :chunked? false,
    :reason-phrase "CREATED",
    :headers
    {"Access-Control-Expose-Headers" "ETag",
     "Access-Control-Allow-Headers"
     "Accept, Authorization, Content-Type, If-Match, If-Modified-Since, If-None-Match, If-Unmodified-Since",
     "Twilio-Request-Id" "RQ16f0e18d5f8445e899fb7e30e1e77c63",
     "Content-Type" "application/json",
     "Access-Control-Allow-Origin" "*",
     "Content-Length" "809",
     "Strict-Transport-Security" "max-age=15768000",
     "Connection" "Close",
     "Twilio-Concurrent-Requests" "1",
     "Twilio-Request-Duration" "0.099",
     "Access-Control-Allow-Methods" "GET, POST, DELETE, OPTIONS",
     "Date" "Tue, 15 May 2018 06:26:36 GMT",
     "Access-Control-Allow-Credentials" "true",
     "X-Powered-By" "AT-5000",
     "X-Shenanigans" "none"},
    :orig-content-encoding nil,
    :status 201,
    :length 809,
    :body
    "{\"sid\": \"SMcc677b1f959d480cabf67290a98fca9e\", \"date_created\": \"Tue, 15 May 2018 06:26:36 +0000\", \"date_updated\": \"Tue, 15 May 2018 06:26:36 +0000\", \"d
ate_sent\": null, \"account_sid\": \"ACbb3c91e2e1bd759c2f555a72f865d89b\", \"to\": \"+14157580667\", \"from\": \"+14154232364\", \"messaging_service_sid\": null, \"body\": \"Sent from your Twilio trial account - testing\", \"status\": \"queued\", \"num_segments\": \"1\", \"num_media\": \"0\", \"direction\": \"outbound-api\", \"api_version\": \"2010-04-01\", \"price\": null, \"price_unit\": \"USD\", \"error_code\": null, \"error_message\": null, \"uri\": \"/2010-04-01/Accounts/ACbb3c91e2e1bd759c2f555a72f865d89b/Messages/SMcc677b1f959d480cabf67290a98fca9e.json\", \"subresource_uris\": {\"media\": \"/2010-04-01/Accounts/ACbb3c91e2e1bd759c2f555a72f865d89b/Messages/SMcc677b1f959d480cabf67290a98fca9e/Media.json\"}}",
    :trace-redirects []}]
