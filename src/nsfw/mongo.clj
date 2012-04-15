(ns nsfw.mongo
  (:use [somnium.congomongo])
  (:require [nsfw.util :as util]))

(defn bson-id [id-or-str]
  (org.bson.types.ObjectId/massageToObjectId id-or-str))
