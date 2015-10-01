(ns nsfw.css)

(def display-flex
  {:display ^:prefix #{"flex" "-webkit-flex"
                       "-moz-box" "-ms-flexbox"}})

(defn justify-content [v]
  {:justify-content v
   :-webkit-justify-content v
   :-moz-justify-content v
   :-ms-justify-content v})

(defn align-items [v]
  {:align-items v
   :-webkit-align-items v
   :-moz-align-items v
   :-ms-align-items v})
