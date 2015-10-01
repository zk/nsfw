(ns nsfw.mailgun
  (:require [clj-mailgun.core :as mg]))

(defn send
  "creds: :api-key, :domain\n
   mail: :from, :to, :subject, :text"
  [creds mail]
  (mg/send-email creds mail))
