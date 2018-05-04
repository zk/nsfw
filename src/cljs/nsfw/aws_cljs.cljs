(ns nsfw.aws_cljs
  (:require [cljsjs.aws-sdk-js]
            [nsfw.util :as nu]))

(def AWS
  (try
    (.-AWS js/window)
    (catch js/Error e
      (nu/throw-str "Couldn't load AWS sdk"))))

(def DynamoDB
  (when AWS
    (.-DynamoDB AWS)))

(defn check-deps! []
  (when-not AWS
    (nu/throw-str "AWS JS SDK is not present in JS environment")))

(defn configure! [{:keys [access-key
                          secret-key
                          region]}]
  (when-not (and access-key
                 secret-key
                 region)
    (.update
     (.-config AWS)
     (clj->js {:region region}))
    (set! (.-accessKeyId (.-config AWS)) access-key)
    (set! (.-secretAccessKey (.-config AWS)) secret-key)))

(defn dynamo-db-client [opts]
  (check-deps!)
  (DynamoDB.
   (clj->js
    (merge
      {:apiVersion "2012-10-08"}
      opts))))

(defn map-entry->attr-key [override k v]
  (or (override k v)
      (cond
        (string? v) :S
        (number? v) :N
        (boolean? v) :BOOL
        (nil? v) :NULL
        (sequential? v) :L
        (map? v) :M
        :else :B)))

(defn ddb-put-item [ddb table-name item
                    & [{:keys [key->attr]}]]
  (.putItem
   ddb
   (clj->js
    {:TableName (name table-name)
     :Item (->> item
                (map (fn [[k v]]
                       {(item-key->attr-key key->attr k) (clj->js v)}))
                (into {}))})
   (fn [err data]
     (put! ch [data err]))))
