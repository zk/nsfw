(ns nsfw.log
  (:require [clojure.string :as str]))

(defn clean-val [val]
  (cond
   (string? val) (str "\"" (str/replace val #"\"" "\\\\\"") "\"")
   :else val))

(defn format-log-entry [key-vals]
  (->> (partition 2 key-vals)
       (map #(str (name (first %)) "=" (clean-val (second %))))
       (interpose " ")
       (reduce str)
       (str (System/currentTimeMillis) " ")))

(defn make-logger [app-id ns]
  (fn [& key-vals]
    (println (format-log-entry (concat [:app-id app-id :ns ns] key-vals)))))
