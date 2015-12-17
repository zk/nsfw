(ns nsfw.image-upload
  (:require [nsfw.util :as util]
            [nsfw.s3 :as s3]))


;; Assumptions * Files are stored with their hash as the filename,
;; theoretically you can have files with the same hashes but different
;; mime types, adding extension would fix this.

;; How to configure AWS for upload-only credentials:
;;
;; 1. Create bucket in S3, set perms to global read
;;    (everyone -> view permissions *consider adding
;;    an ACL call to this thing*
;; 2. Open IAM
;; 3. Create user, save credentials
;; 4. Create policy looking like:

;; {
;;  "Version": "2012-10-17",
;;  "Statement": [
;;                {
;;                 "Sid": "Stmt1443647793000",
;;                 "Effect": "Allow",
;;                 "Action": [
;;                            "s3:PutObject",
;;                            "s3:PutObjectAcl",
;;                            "s3:PutObjectVersionAcl",
;;                            "s3:GetObjectAcl",
;;                            "s3:GetObjectVersionAcl"
;;                            ],
;;                 "Resource": [
;;                              "arn:aws:s3:::bucket.name.here/*"
;;                              ]
;;                 }
;;                ]
;;  }

;; 5. Attach new policy to new user.

;; Voila! You've got a bucket and creds to upload to that bucket.

(defn gen-handler [{:keys [access-key
                           secret-key
                           bucket
                           root-path]}]

  (fn [{:keys [params] :as req}]
    (let [file (-> params :file :tempfile)
          content-type (-> params :file :content-type)
          id (util/md5 file)
          path (if root-path
                 (str root-path "/" id)
                 id)]
      (s3/put-global-readable
        {:access-key access-key
         :secret-key secret-key}
        {:file file
         :path path
         :bucket bucket
         :content-type content-type})
      {:body {:success? true
              :id id
              :url (str "https://s3.amazonaws.com/"
                        bucket
                        "/"
                        path)}})))

;; Override filename generator
