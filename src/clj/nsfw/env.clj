(ns nsfw.env
  "Shell environment helpers."
  (:require [clojure.string :as str])
  (:refer-clojure :exclude (int str)))

(def ^:dynamic *env* (System/getenv))

(defn clj->env [sym-or-str]
  (-> sym-or-str
      name
      (str/replace #"-" "_")
      (str/upper-case)))

(defn env
  "Retrieve environment variables by clojure keyword style.
   ex. (env :user) ;=> \"zk\"
   Returns nil if environment variable not set."
  [sym]
  (let [res (get *env* (clj->env sym))]
    (when-not (nil? res)
      res)))

(defn int
  "Retrieve and parse int env var."
  [sym]
  (when-let [v (env sym)]
    (Integer/parseInt v)))

(defn str
  "Retrieve and parse string env var."
  [sym]
  (env sym))

(defn bool
  [sym]
  (when-let [v (env sym)]
    (Boolean/parseBoolean v)))
