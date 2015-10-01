(ns nsfw.s3
  (:require [aws.sdk.s3 :as s3]))

(defn put-global-readable [creds
                           {:keys [file bucket path content-type]}]
  (s3/put-object creds bucket path file
    {:content-type content-type})
  (s3/update-object-acl
    creds
    bucket
    path
    (s3/grant :all-users :read)))

#_(put-global-readble
    {:access-key "AKIAI2STHNMJGS4HQPHQ"
     :secret-key "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"}
    {:file (java.io.File. "/Users/zk/Dropbox/zk-avatar.png")
     :path "avatars/zk-avatar.png"
     :bucket "letterlovely-dev"})
