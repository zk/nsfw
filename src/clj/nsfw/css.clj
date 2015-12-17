(ns nsfw.css
  (:require [clojure.string :as str]
            [garden.def :refer [defkeyframes]]))

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

(defn transition [v]
  {:transition v
   :-webkit-transition (if (string? v)
                         (str/replace v #"transform" "-webkit-transform")
                         v)
   :-moz-transition v
   :-ms-transition v
   :-o-transition v})

(defn checkerboard [{:keys [light-bg dark-bg]}]
  (let [light-bg "#fafafa"
        dark-bg "#f3f3f3"]
    {:background-color light-bg
     :background-image (str "linear-gradient(45deg, "
                            dark-bg
                            " 25%, transparent 25%, transparent 75%, "
                            dark-bg
                            " 75%, "
                            dark-bg
                            "),
linear-gradient(45deg, "
                            dark-bg
                            " 25%, transparent 25%, transparent 75%, "
                            dark-bg
                            " 75%, "
                            dark-bg
                            ")")
     :background-size "60px 60px"
     :background-position "0 0, 30px 30px"}))



;; Spinners

(defkeyframes sk-bouncedelay
  ["0%" "80%" "100%" {:transform "scale(0)"
                      :-webkit-transform "scale(0)"}]
  ["40%" {:transform "scale(1.0)"
          :-webkit-transform "scale(1.0)"}])

(defn ellipsis-spinner [{:keys [size color]}]
  (let [color (or color "rgba(0,0,0,0.4)")
        size (or size 14)]
    [sk-bouncedelay
     [:.ellipsis-spinner
      {:text-align 'center
       :display 'inline-block}
      ["> div"
       {:width (str size "px")
        :height (str size "px")
        :margin (str (max (/ size 10) 1) "px")
        :background-color color

        :border-radius "100%"
        :display 'inline-block
        :-webkit-animation "sk-bouncedelay 1.4s infinite ease-in-out both"
        :animation "sk-bouncedelay 1.4s infinite ease-in-out both"}]
      [:.bounce1 {:-webkit-animation-delay "-0.32s"
                  :animation-delay "-0.32s"}]
      [:.bounce2 {:-webkit-animation-delay "-0.16s"
                  :animation-delay "-0.16s"}]]]))
