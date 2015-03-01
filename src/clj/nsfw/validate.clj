(ns nsfw.validate
  (:require [schema.core :as s])
  (:refer-clojure :exclude [key]))

(defn run [payload vs]
  (->> vs
       (map (fn [f]
              (f payload)))
       (apply merge-with concat)))

(defn key [k & opts]
  (let [msg (last opts)
        fns (butlast opts)]
    (fn [pl]
      (let [pass? (->> fns
                       (map (fn [f]
                              (f (get pl k))))
                       (reduce #(and %1 %2)))]
        (if-not pass?
          {k [msg]})))))

(defn response [payload vs]
  (when-let [errors (run payload vs)]
    {:status 422 :errors errors}))

(defn schema [sch & [error]]
  (fn [pl]
    (when-let [res (s/check sch pl)]
      (or error res))))

(def not-empty? #(not (empty? %)))
