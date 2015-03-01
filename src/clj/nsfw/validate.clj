(ns nsfw.validate
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

(def not-empty? #(not (empty? %)))
