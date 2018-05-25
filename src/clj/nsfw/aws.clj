(ns nsfw.aws
  (:import [com.amazonaws.auth BasicAWSCredentials]
           [com.amazonaws.regions Region Regions]))

(defn basic-credentials [access-key secret-key]
  (BasicAWSCredentials. access-key secret-key))

(defn region-key->region-obj [region-key]
  (Region/getRegion (Regions/fromName (name region-key))))
