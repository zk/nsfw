(ns nsfw.mailgun
  (:require [clj-mailgun.core :as mg]
            [nsfw.util :as util]))

(defn send
  "creds: :api-key, :domain\n
   mail: :from, :to, :subject, :text"
  [creds mail]
  (try
    (let [res (mg/send-email
                creds
                mail)]
      (merge
        {:from (:from mail)
         :to (:to mail)}
        {:success? (= 200 (:status res))
         :result (util/from-json (:body res))}))
    (catch Exception e
      {:success? false
       :from (:from mail)
       :to (:to mail)})))
