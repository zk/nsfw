(ns nsfw.css
  (:require [clojure.string :as str]
            [garden.def :refer [defkeyframes]]
            [garden.units :refer [px px-]]
            [garden.stylesheet :refer [at-media]]))

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

(defn flex-box [opts]
  (merge
    display-flex
    (->> opts
         (map (fn [[k v]]
                (prefix [k v])))
         (reduce merge))))

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
    [[:.ellipsis-spinner
      {:text-align 'center
       :display 'inline-block}
      ["> div"
       {:width (str size "px")
        :height (str size "px")
        :margin-left (str (max (/ size 5) 1) "px")
        :margin-right (str (max (/ size 5) 1) "px")
        :margin-bottom 0
        :background-color color

        :border-radius "100%"
        :display 'inline-block
        :-webkit-animation "sk-bouncedelay 1.4s infinite ease-in-out both"
        :animation "sk-bouncedelay 1.4s infinite ease-in-out both"}]
      [:.bounce1 {:-webkit-animation-delay "-0.32s"
                  :animation-delay "-0.32s"}]
      [:.bounce2 {:-webkit-animation-delay "-0.16s"
                  :animation-delay "-0.16s"}]]]))

(def screen-lg-min (px 1200))

(def screen-md-min (px 992))
(def screen-md-max (px- screen-lg-min 1))

(def screen-sm-min (px 768))
(def screen-sm-max (px- screen-md-min 1))

(def screen-xs-min (px 480))
(def screen-xs-max (px- screen-sm-min 1))

(def key->breakpoint
  {:xs {:max-width screen-xs-max}
   :>sm {:min-width screen-sm-min}
   :<md {:max-width screen-md-min}
   :<lg {:max-width screen-lg-min}
   :sm {:min-width screen-sm-min
        :max-width screen-sm-max}
   :md {:min-width screen-md-min
        :max-width screen-md-max}
   :lg {:min-width screen-lg-min}})

(defn at-bp [breakpoint-key & rules]
  (let [rules (if (map? (first rules))
                [(vec (concat [:&] rules))]
                rules)]
    (apply
      at-media
      (key->breakpoint breakpoint-key)
      rules)))
