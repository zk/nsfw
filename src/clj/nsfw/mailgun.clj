(ns nsfw.mailgun
  (:require [clj-mailgun.core :as mg]
            [nsfw.util :as util]))

(defn send
  "creds: :api-key, :domain\n
   mail: :from, :to, :subject, :text"
  [creds mail]
  (let [res (mg/send-email
              creds
              mail)]
    {:success? (= 200 (:status res))
     :result (util/from-json (:body res))}))
