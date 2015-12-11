(ns devguide.css
  (:require [nsfw.css :as nc]))

(def reset
  [[:body {:font-family "'Open Sans', 'Helvetica Neue', Arial, sans-serif"}]
   [:p {:font-size "18px"
        :line-height "150%"
        :font-weight 'normal}]
   [:pre {:border-radius 0
          :border 'none
          :background-color "rgba(0,0,0,0.02)"
          :padding "10px"
          :font-size "14px"}]
   [:.badge {:height 'auto
             :font-weight 'normal}]
   [:h1 :h2 :h3 :h4 :h5 {:margin-top 0}]])

(def layout
  [(->> [50 40 30 20 10]
        (map-indexed
          (fn [i n]
            [(str ".sec" (inc i))
             {:margin-bottom (str n "px")}])))
   [:.flex-row
    nc/display-flex
    (nc/prefix [:justify-content 'space-between])
    (nc/prefix [:align-items 'center])]])

(def page
  [[:.nsfw-logo
    nc/display-flex
    (nc/prefix [:justify-content 'flex-left])
    (nc/prefix [:align-items 'center])
    {:font-family "'', 'Helvetica Neue', Arial, sans-serif"}
    [:h1 :h2 :h3 {:padding 0 :margin 0}]
    [:h1 {:font-weight 'bold
          :font-size "70px"
          :margin-right "3px"
          :letter-spacing "2px"}]
    [:h2 {:font-weight 300
          :font-size "28px"}]
    [:h5 {:text-transform 'uppercase
          :font-weight 'normal
          :font-size "14px"}]
    [:.title {:margin-top "2px"}]]
   [:.affix-wrapper.affix
    {:top "10px"}]
   [:.sec-header
    {:border-bottom "solid #eee 1px"
     :margin-bottom "10px"}]
   [:.dg-section
    {:margin-bottom "50px"}
    [:.badge {:margin-left "3px"}]
    [:h1 :h2 :h3 :h4 :h5 :h6 {:margin-bottom "10px"}]
    [:h3
     nc/display-flex
     (nc/prefix [:justify-content 'space-between])]
    [:h5 {:text-transform 'uppercase
          :font-weight 'normal}]
    [:p :pre {:margin-bottom "20px"}]
    [:.options-table
     [:.row {:margin-bottom "10px"
             :padding-top "10px"
             :border-top "solid #eee 1px"}]
     [:.key {:text-align 'right}]]]
   [:.example
    (nc/checkerboard {})
    {:padding "20px"}]])

(def app
  [reset
   layout
   page])
