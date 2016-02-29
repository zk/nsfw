(ns nsfw.s3
  (:require [aws.sdk.s3 :as s3]))

(defn put-global-readable [creds
                           {:keys [file bucket path content-type content-length]}]
  (s3/put-object creds bucket path file
    {:content-type content-type
     :content-length content-length}
    (s3/grant :all-users :read)))

#_(put-global-readble
    {:access-key "AK"
     :secret-key "SK"}
    {:file (java.io.File. "/Users/zk/Dropbox/zk-avatar.png")
     :path "avatars/zk-avatar.png"
     :bucket "letterlovely-dev"})
