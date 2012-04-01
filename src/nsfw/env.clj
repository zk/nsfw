(ns nsfw.env "Shell environment helpers."
    (:require [clojure.string :as str]))

(defn clj->env [sym-or-str]
  (-> sym-or-str
      name
      (str/replace #"-" "_")
      (str/upper-case)))

(defn env
  "Retrieve environment variables by clojure keyword style.
   ex. (env :user) ;=> \"zkim\""
  [sym & [default]]
  (or (System/getenv (clj->env sym))
      default))
