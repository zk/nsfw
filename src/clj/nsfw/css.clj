(ns nsfw.css)

(def display-flex
  {:display ^:prefix #{"flex" "-webkit-flex"
                       "-moz-box" "-ms-flexbox"}})

(defn prefix [[k v]]
  (->> ["-webkit-"
        "-moz-"
        "-ms-"
        ""]
       (map (fn [prefix]
              [(->> k
                    name
                    (str prefix)
                    keyword)
               v]))
       (into {})))

(defn justify-content [v]
  (prefix [:justify-content v]))

(defn align-items [v]
  (prefix [:align-items v]))

(defn flex-wrap [v]
  (prefix [:flex-wrap v]))

(defn align-content [v]
  (prefix [:align-content v]))

(defn align-self [v]
  (prefix [:align-self v]))

(defn flex-grow [v]
  (prefix [:flex-grow v]))
