(ns nsfw.storage
  (:require [cljs.reader :as reader]))

(extend-type js/Storage
  ILookup
  (-lookup [s k default]
    (if-let [res (.getItem s k)]
      (reader/read-string res)
      default)))

(def local (.-localStorage js/window))

(defn lset! [k v]
  (.setItem local k (pr-str v)))

(defn lget! [k & [default]]
  (get local k default))
