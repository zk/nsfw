(ns nsfw.s3
  (:require [cljsjs.aws-sdk-js]
            [cljs.core.async
             :refer [chan put!]]))

(def AWS js/AWS)

(defn <aws [creds service-class method & args]
  (let [ch (chan)]
    (apply js-invoke
      (service-class.
        (clj->js
          {:accessKeyId (:access-id creds)
           :secretAccessKey (:secret-key creds)
           :region (:region creds)}))
      method
      (concat
        (map clj->js args)
        [(fn [err data]
           (put! ch
             (if err
               [nil err]
               [(-> data
                    (js->clj :keywordize-keys true))])))]))
    ch))


(defn <call [creds fn-name payload]
  (<aws
    creds
    (.-S3 AWS)
    fn-name
    payload))
