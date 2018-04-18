(ns nsfw.css2
  (:require [clojure.string :as str]
            [garden.units :as u]
            [garden.color :as co]
            [garden.stylesheet :refer [at-media]]
            [garden.core :as garden]
            [garden.stylesheet :as gs])
  #? (:cljs (:require-macros [nsfw.css2 :refer [inject-css-defs]])))

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

(defn px [n]
  (if (and (string? n) (str/includes? n "px"))
    n
    (str n "px")))

(defn transition [v]
  {:transition v
   :-webkit-transition (if (string? v)
                         (str/replace v #"transform" "-webkit-transform")
                         v)
   :-moz-transition v
   :-ms-transition v
   :-o-transition v})

(def screen-lg-min (u/px 1200))

(def screen-md-min (u/px 992))
(def screen-md-max (u/px- screen-lg-min 1))

(def screen-sm-min (u/px 768))
(def screen-sm-max (u/px- screen-md-min 1))

(def screen-xs-min (u/px 480))
(def screen-xs-max (u/px- screen-sm-min 1))

(def key->breakpoint
  {:xs {:max-width screen-xs-max}
   :>sm {:min-width screen-sm-min}
   :<sm {:max-width screen-sm-min}
   :>md {:min-width screen-md-min}
   :<md {:max-width screen-md-min}
   :>lg {:min-width screen-lg-min}
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

(defn checkerboard-gen [{:keys [light-bg dark-bg]}]
  (let [light-bg (or light-bg "#fafafa")
        dark-bg (or dark-bg "#f3f3f3")]
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

(def shadows
  {:sm {:box-shadow "0 2px 4px 0 rgba(0,0,0,0.10)"}
   :md {:box-shadow "0 4px 8px 0 rgba(0,0,0,0.12), 0 2px 4px 0 rgba(0,0,0,0.08)"}
   :lg {:box-shadow "0 15px 30px 0 rgba(0,0,0,0.11), 0 5px 15px 0 rgba(0,0,0,0.08)"}
   :inner {:box-shadow "inset 0 2px 4px 0 rgba(0,0,0,0.06)"}})

(def shadow-sm (:sm shadows))
(def shadow-md (:md shadows))
(def shadow-lg (:lg shadows))
(def shadow-inner (:inner shadows))

(defn margin [n] {:margin (px n)})
(defn margin-top [n] {:margin-top (px n)})
(defn margin-bot [n] {:margin-bottom (px n)})

(defn padding [n] {:padding (px n)})
(defn padding-top [n] {:padding-top (px n)})
(defn padding-bot [n] {:padding-bot (px n)})

(defmacro inject-css-defs [{:keys [sizes fonts] :as spec}]
  (let [{:keys [xs sm md lg xl]} sizes
        {:keys [header copy impact monospace]} fonts]
    `(do
       (def ~'css-spec ~spec)

       (def ~'xs ~xs)
       (def ~'sm ~sm)
       (def ~'md ~md)
       (def ~'lg ~lg)
       (def ~'xl ~xl)

       (def ~'xspx (px ~xs))
       (def ~'smpx (px ~sm))
       (def ~'mdpx (px ~md))
       (def ~'lgpx (px ~lg))
       (def ~'xlpx (px ~xl))

       (def ~'pad-none (padding 0))
       (def ~'pad-xs (padding ~xs))
       (def ~'pad-sm (padding ~sm))
       (def ~'pad-md (padding ~md))
       (def ~'pad-lg (padding ~lg))
       (def ~'pad-xl (padding ~xl))

       (def ~'mg-none (margin 0))
       (def ~'mg-xs (margin ~xs))
       (def ~'mg-sm (margin ~sm))
       (def ~'mg-md (margin ~md))
       (def ~'mg-lg (margin ~lg))
       (def ~'mg-xl (margin ~xl))

       (def ~'mg-top-xs (margin-top ~xs))
       (def ~'mg-top-sm (margin-top ~sm))
       (def ~'mg-top-md (margin-top ~md))
       (def ~'mg-top-lg (margin-top ~lg))
       (def ~'mg-top-xl (margin-top ~xl))

       (def ~'mg-bot-xs (margin-bot ~xs))
       (def ~'mg-bot-sm (margin-bot ~sm))
       (def ~'mg-bot-md (margin-bot ~md))
       (def ~'mg-bot-lg (margin-bot ~lg))
       (def ~'mg-bot-xl (margin-bot ~xl))

       (def ~'header-font ~header)
       (def ~'copy-font ~copy)
       (def ~'impact-font ~impact)
       (def ~'monospace-font ~monospace)

       (def ~'shadow-sm (:sm shadows))
       (def ~'shadow-md (:md shadows))
       (def ~'shadow-lg (:lg shadows))
       (def ~'shadow-inner (:inner shadows))

       (def ~'flex-center {:display "flex"
                           :justify-content "center"
                           :align-items "center"})

       (def ~'flex-apart {:display "flex"
                          :flex-direction "row"
                          :justify-content "space-between"
                          :align-items "center"})

       (def ~'flex-left {:display "flex"
                         :justify-content "flex-start"
                         :align-items "center"
                         :flex-wrap "wrap"}))))


;;;

(def text-center {:text-align 'center})
(def text-right {:text-align 'right})
(def text-left {:text-align 'left})

(def lh100 {:line-height "100%"})
(def lh120 {:line-height "120%"})
(def lh150 {:line-height "150%"})

(def ellipsis
  {:flex 1
   :overflow 'hidden
   :white-space 'nowrap
   :text-overflow 'ellipsis})

(def border-radius-sm
  {:border-radius (px 2)})

(def border-radius-md
  {:border-radius (px 3)})

(def border-radius-lg
  {:border-radius (px 5)})

(def border-radius-round
  {:border-radius "999px"})

(defn utilities [css-spec]
  (let [{:keys [xs sm md lg xl]} (:sizes css-spec)
        {:keys [header copy impact monospace]} (:fonts css-spec)]
    [[:.text-center text-center]
     [:.text-right text-right]
     [:.text-left text-left]

     [:.pad-none (padding 0)]
     [:.pad-xs (padding xs)]
     [:.pad-sm (padding sm)]
     [:.pad-md (padding md)]
     [:.pad-lg (padding lg)]
     [:.pad-xl (padding xl)]

     [:.mg-none (margin 0)]
     [:.mg-xs (margin xs)]
     [:.mg-sm (margin sm)]
     [:.mg-md (margin md)]
     [:.mg-lg (margin lg)]
     [:.mg-xl (margin xl)]

     [:.mg-bot-xs (margin-bot xs)]
     [:.mg-bot-sm (margin-bot sm)]
     [:.mg-bot-md (margin-bot md)]
     [:.mg-bot-lg (margin-bot lg)]
     [:.mg-bot-xl (margin-bot xl)]

     [:.mg-top-xs (margin-top xs)]
     [:.mg-top-sm (margin-top sm)]
     [:.mg-top-md (margin-top md)]
     [:.mg-top-lg (margin-top lg)]
     [:.mg-top-xl (margin-top xl)]

     [:.lh100 lh100]
     [:.lh120 lh120]
     [:.lh150 lh150]

     [:.bold {:font-weight 'bold}]
     [:.ellipsis ellipsis]
     [:.spacer-xs {:height (px xs)}]
     [:.spacer-sm {:height (px sm)}]
     [:.spacer-md {:height (px md)}]
     [:.spacer-lg {:height (px lg)}]

     [:.br-sm border-radius-sm]
     [:.br-md border-radius-md]
     [:.br-lg border-radius-lg]

     [:.shadow-sm shadow-sm]
     [:.shadow-md shadow-md]
     [:.shadow-lg shadow-lg]
     [:.shadow-inner shadow-inner]

     [:.header-font {:font-family header}]
     [:.copy-font {:font-family copy}]
     [:.impact-font {:font-family impact}]
     [:.monospace-font {:font-family monospace}]


     [:.full-size {:width "100%"
                   :height "100%"}]
     [:.abs-full-size {:position 'absolute
                       :width "100%"
                       :height "100%"
                       :top 0
                       :left 0}]


     [:.scroll-y
      {:overflow-y 'scroll
       :-webkit-overflow-scrolling 'touch}]]))

(defn defaults [css-spec]
  (let [{:keys [sm md]} (:sizes css-spec)
        {:keys [monospace]} (:fonts css-spec)]
    [[:body {:-webkit-font-smoothing 'antialiased
             :-moz-osx-font-smoothing 'grayscale}]
     [:ul :ol
      {:padding-left (px 40)}
      [:li
       {:padding 0
        :margin 0}
       (margin-bot sm)]]

     [:ul :ol
      {:padding 0}
      [:&.bare
       [:li {:list-style-type 'none
             :padding 0
             :margin 0}]]]
     [:pre
      {:background-color 'white
       :font-family monospace}
      (padding sm)]

     [:.code
      {:font-family monospace
       :white-space 'pre-wrap
       :font-weight 'bold}]
     [:p (margin-bot md)]]))

(defn progress [css-spec]
  (let [{:keys [sm md lg]} (:sizes css-spec)]
    [(gs/at-keyframes
       :sk-rotatePlane
       ["0%" {:transform "perspective(120px) rotateX(0deg) rotateY(0deg)"}]
       ["50%" {:transform "perspective(120px) rotateX(-180.1deg) rotateY(0deg)"}]
       ["100%" {:transform "perspective(120px) rotateX(-180deg) rotateY(-179.9deg)"}])
     [:.prog-rot-sm
      :.prog-rot-md
      :.prog-rot-lg
      {:opacity 0}
      (transition "opacity 0.1s ease")
      [::&.loading
       {:opacity 1}]
      [:.box
       {:background-color 'black
        :animation "sk-rotatePlane 1.2s infinite ease-in-out"}]
      [:&.slow
       [:.box
        {:animation "sk-rotatePlane 2.4s infinite ease-in-out"}]]]

     [:.prog-rot-sm
      [:.box
       {:width (px sm)
        :height (px sm)}]]
     [:.prog-rot-md
      [:.box
       {:width (px md)
        :height (px md)}]]
     [:.prog-rot-lg
      [:.box
       {:width (px lg)
        :height (px lg)}]]]))


(defn button
  [k {:keys [base-color
             base-size
             border-color
             text-color
             border-radius
             border-radius-alt]
      :or {text-color (co/rgb 255 255 255)
           border-radius 4
           border-radius-alt 999}
      :as opts}]
  (let [s (name k)
        selector (str ".btn-" s)

        props (dissoc opts
                :base-color
                :base-size
                :text-color)
        border-color (or border-color base-color)

        hover-amount 6
        active-amount 14

        root-styles (merge
                      {:background-color 'transparent
                       :outline 0
                       :border-style 'solid
                       :border-width (px 1)
                       :font-size (px 15)
                       :font-weight 'bold
                       :padding "5px 20px"
                       ;;:width "100%"
                       :cursor 'pointer}
                      {:background-color base-color
                       :border-color (or border-color base-color)
                       :color text-color
                       :letter-spacing (px 1)}
                      props
                      (transition "background-color 0.2s ease, border-color 0.2s ease"))

        hover-styles {:background-color (-> base-color
                                            (co/rotate-hue hover-amount)
                                            (co/darken 7))
                      :border-color (co/rotate-hue border-color (* 1 hover-amount))
                      :color (co/rotate-hue text-color hover-amount)}

        active-styles {:background-color (-> base-color
                                             (co/rotate-hue active-amount)
                                             (co/darken 12))
                       :border-color (co/rotate-hue border-color (* 1 active-amount))
                       :color (co/rotate-hue text-color active-amount)}]
    [[selector
      {:border-radius (px border-radius-alt)}
      root-styles
      [:&:hover hover-styles]
      [:&:active active-styles]
      [:&:focus
       {:outline 0}]]
     [(str selector "-alt")
      {:border-radius (px border-radius-alt)}
      root-styles
      [:&:hover hover-styles]
      [:&:active active-styles]
      [:&:focus
       {:outline 0}]]]))

(defn buttons [css-spec]
  (->> css-spec
       :buttons
       (map #(apply button %))
       vec))

(defn headers [css-spec & style-overrides]
  (let [sm (-> css-spec :sizes :sm)
        header-font (-> css-spec :fonts :header)]
    [:h1 :h2 :h3 :h4 :h5 :h6
     (merge
       (margin-bot sm)
       lh100
       {:font-family header-font
        :font-weight 'normal}
       (reduce merge style-overrides))]))

(def flex-center {:display 'flex
                  :justify-content 'center
                  :align-items 'center})

(def flex-apart {:display 'flex
                 :flex-direction 'row
                 :justify-content 'space-between
                 :align-items 'center})

(def flex-left {:display 'flex
                :justify-content 'flex-start
                :align-items 'center
                :flex-wrap 'wrap})

(def flex-right {:display 'flex
                :justify-content 'flex-end
                :align-items 'center
                :flex-wrap 'wrap})

(def flexbox
  [[:.flex-apart flex-apart]

   [:.flex-apart-top {:display 'flex
                      :flex-direction 'row
                      :justify-content 'space-between
                      :align-items 'flex-start}]

   [:.flex-around {:display 'flex
                   :justify-content 'space-around
                   :align-items 'center}]

   [:.flex-center flex-center]

   [:.flex-vcenter {:display 'flex
                    :flex-direction 'column
                    :justify-content 'center
                    :align-items 'center}]


   [:.flex-center-top {:display 'flex
                       :justify-content 'center
                       :align-items 'flex-start}]
   [:.flex-right flex-right]

   [:.flex-right-stretch {:display 'flex
                          :align-itmes 'stretch
                          :justify-content 'flex-end}]


   [:.flex-left flex-left]

   [:.flex-left-top {:display 'flex
                     :flex-direction 'row
                     :justify-content 'flex-start
                     :align-items 'flex-start
                     :flex-wrap 'wrap}]

   [:.flex-left-bot {:flex-direction 'row
                     :justify-content 'flex-start
                     :align-items 'flex-end
                     :flex-wrap 'wrap}]

   [:.flex-top {:display 'flex
                :justify-content 'flex-start
                :align-items 'flex-start}]

   [:.flex-masonry {:display 'flex
                    :flex-direction 'column
                    :flex-wrap 'wrap
                    :align-content 'stretch}]

   [:.flex-column {:display 'flex
                   :flex-direction 'column
                   :justify-content 'flex-start}]

   [:.flex-column-center {:display 'flex
                          :flex-direction 'column
                          :align-items 'center}]

   [:.flex-column-top {:display 'flex
                       :flex-direction 'column
                       :align-items 'flex-start}]

   [:.flex-column-vcenter {:display 'flex
                           :flex-direction 'column
                           :justify-content 'center}]

   [:.flex-column-right {:display 'flex
                         :flex-direction 'column
                         :align-items 'flex-end}]

   [:.flex-column-right-center {:display 'flex
                                :flex-direction 'column
                                :align-items 'flex-end
                                :justify-content 'center}]

   [:.flex-column-center-both {:display 'flex
                               :flex-direction 'column
                               :align-items 'center
                               :justify-content 'center}]])
