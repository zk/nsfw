(ns nsfw.sns-push
  (:require [nsfw.aws :as aws]
            [nsfw.util :as nu])
  (:import [com.amazonaws.services.sns AmazonSNSClient]
           [com.amazonaws.services.sns.model
            CreatePlatformApplicationRequest
            CreatePlatformEndpointRequest
            PublishRequest
            PublishResult]
           [com.amazonaws.services.sns.model
            InvalidParameterException]))

(defn sns-client [access-key secret-key region-key]
  (doto (AmazonSNSClient.
          (aws/basic-credentials access-key secret-key))
    (.setRegion (aws/region-key->region-obj region-key))))

(defn publish-request-obj [{:keys [message-structure
                                   target-arn
                                   payload]
                            :or {message-structure :json}}]

  (doto (PublishRequest.)
    (.setMessageStructure (name message-structure))
    (.setTargetArn target-arn)
    (.setMessage (nu/to-json payload))))

(defn create-ios-endpoint-arn
  [{:keys [access-key
           secret-key
           region
           application-arn]}
   platform-token
   & [{:keys [custom-data]}]]
  (let [client (sns-client access-key secret-key region)
        custom-data (or custom-data platform-token)
        req (doto (CreatePlatformEndpointRequest.)
              (.setCustomUserData custom-data)
              (.setToken platform-token)
              (.setPlatformApplicationArn application-arn))]
    (try
      [(.getEndpointArn
         (.createPlatformEndpoint
           client
           req))]
      (catch InvalidParameterException e
        [nil {:message (.getMessage e)} e])
      (catch Exception e
        [nil {:message (.getMessage e)} e]))))

(defn format-push-payload [platform data]
  {(name platform)
   (nu/to-json data)})

(defn send-ios-push
  [{:keys [access-key
           secret-key
           region
           platform]}
   endpoint-arn
   push-payload]
  (let [client (sns-client access-key secret-key region)
        req (publish-request-obj
              {:target-arn endpoint-arn
               :payload
               (format-push-payload
                 platform
                 {"aps" push-payload})})]
    (try
      [{:message-id (.getMessageId (.publish client req))
        :endpoint-arn endpoint-arn}]
      (catch InvalidParameterException e
        [nil {:message (.getMessage e)} e])
      (catch Exception e
        [nil {:message (.getMessage e)} e]))))

(comment
  (def push-creds
    {:access-key ""
     :secret-key ""
     :region :us-west-2
     :platform "APNS_SANDBOX"
     :application-arn ""})

  (prn
    (create-ios-endpoint-arn push-creds
      "push-key"))

  (prn
    (send-ios-push
      push-creds
      "endpoint-arn"
      {:alert {:title "hello world"
               :subtitle "subtitle"
               :body "the quick brown fox"}}))

  (prn
    (send-ios-push
      push-creds
      "error-arn"
      {:alert {:title "hello world"
               :subtitle "subtitle"
               :body "the quick brown fox"}}))

  (prn
    (send-ios-push
      push-creds
      "endpoint-arn"
      nil)))
