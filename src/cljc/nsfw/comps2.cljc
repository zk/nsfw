(ns nsfw.comps2
  #? (:clj
      (:require [clojure.string :as str]
                [nsfw.util :as nu]
                [garden.units :as u]
                [garden.color :as co]
                [garden.core :as garden]
                [garden.stylesheet :as gs])
      :cljs
      (:require [clojure.string :as str]
                [nsfw.util :as nu]
                [nsfw.page :as page]
                [garden.units :as u]
                [garden.color :as co]
                [garden.core :as garden]
                [garden.stylesheet :as gs]
                [reagent.core :as r]
                [dommy.core :as dommy]
                [cljs.reader :as reader]
                [cljs.core.async :as async
                 :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]]))

  #? (:cljs
      (:require-macros [cljs.core.async.macros :refer [go go-loop]])))

(defn transition [v]
  {:transition v
   :-webkit-transition (if (string? v)
                         (str/replace v #"transform" "-webkit-transform")
                         v)
   :-moz-transition v
   :-ms-transition v
   :-o-transition v})

(def css
  [(gs/at-keyframes
     :indprogress
     ["0%" {:transform "scaleX(0)"}]
     ["30%" {:transform "scaleX(0.6)"}]
     ["55%" {:transform "scaleX(0.75)"}]
     ["100%" {:transform "scaleX(1)"}])

   [:.prog-bar-mock
    {:transform-origin "left center"}]

   [:.prog-bar-mock-bar
    {:transform-origin "left center"
     :transform "scaleX(0)"}
    {:animation "indprogress 20s ease infinite"}]
   [:.prog-bar-mock-done-bar
    {:transform "scaleX(0)"
     :transform-origin "left center"
     :background-color 'green}
    (transition "transform 0.2s ease-in")
    [:&.done
     {:transform "scaleX(1)"}]]])

#?
(:cljs
 (defn $prog-bar-mock [_]
   (let [!ui (r/atom nil)
         !run (atom true)]
     (r/create-class
       {:reagent-render
        (fn [{:keys [loading? done? style stick-to]}]
          [:div.prog-bar-mock
           {:class (when done? "done")
            :style (merge
                     {:overflow 'hidden
                      :height 5}
                     (cond
                       (not stick-to)
                       {:width "100%"}

                       (= :top stick-to)
                       {:position 'absolute
                        :top 0
                        :left 0
                        :right 0}

                       (= :bot stick-to)
                       {:position 'absolute
                        :bottom 0
                        :left 0
                        :right 0})
                     style)}
           (when loading?
             [:div {:style {:position 'relative
                            :width "100%"
                            :height "100%"}}
              [:div.prog-bar-mock-bar
               {:style {:height 5
                        :width "100%"
                        :background-color 'green}}]
              [:div.prog-bar-mock-done-bar
               {:class (when done? "done")
                :style {:position 'absolute
                        :top 0
                        :left 0
                        :right 0
                        :bottom 0}}]])])}))))


#?
(:cljs
 (defn $flipper [& args]
   (page/async-class
     (page/ensure-opts args)
     {:delay-fn
      (fn [[_ & old-c :as old] [_ & new-c :as new]]
        (when (not= old-c new-c)
          [[16
            (update
              old
              0
              merge
              {:stage :post})]
           [200
            (update
              old
              0
              merge
              {:stage :pre})]
           [50
            (update
              new
              0
              merge
              {:stage :vis})
            200]]))
      :reagent-render
      (fn [& args]
        (let [[{:keys [stage disabled?]} & children] (page/ensure-opts args)]
          (let [stage (if disabled?
                        nil
                        stage)]
            [:div.flipper-viewbox
             {:class (when stage "updating")
              :style {:overflow 'hidden}}
             (vec
               (concat
                 [:div
                  {:style (merge
                            {:transform "translateY(0)"}
                            (transition "transform 0.2s ease")
                            (when (= :post stage)
                              {:transform "translateY(100%)"})
                            (when (= :pre stage)
                              (merge
                                {:transform "translateY(-100%)"}
                                (transition 'none))))}]
                 children))])))})))


(defn $hamburger-menu [{:keys [open?
                               color
                               on-toggle
                               line-width
                               line-cap
                               style
                               size]
                        :or {line-width 9
                             line-cap :square
                             color 'black
                             size 25}}]
  (let [stroke-width line-width
        stroke-linecap (name line-cap)]
    [:div.hamburger.visible-xs
     {:class (when open? "open")
      :style (merge
               {:width size
                :height size}
               style)
      :on-click (fn [e]
                  (.preventDefault e)
                  (on-toggle)
                  nil)}
     [:svg {:width "100%"
            :height "100%"
            :viewBox "0 0 100 100"
            :style {:display 'block}}
      [:line.line.top-line
       {:x1 10 :y1 24
        :x2 90 :y2 24
        :style (merge
                 (transition "transform 0.2s ease")
                 {:stroke-width stroke-width
                  :stroke-linecap stroke-linecap}
                 (when color
                   {:stroke color})
                 (when open?
                   {:transform ""})
                 (when open?
                   {:transform "rotate(45deg) translateY(25%)"
                    :transform-origin "center"}))}]

      [:line.line.mid-line
       {:x1 10 :y1 50
        :x2 90 :y2 50
        :style (merge
                 (transition "opacity 0.2s ease")
                 {:stroke-width (+ stroke-width 0.5)
                  :stroke-linecap stroke-linecap}
                 (when color
                   {:stroke color})
                 (when open?
                   {:opacity 0}))}]

      [:line.line.bot-line
       {:x1 10 :y1 75
        :x2 90 :y2 75
        :style (merge
                 (transition "transform 0.2s ease")
                 {:stroke-width stroke-width
                  :stroke-linecap stroke-linecap}
                 (when color
                   {:stroke color})
                 (when open?
                   {:transform "rotate(-45deg) translateY(-25%)"
                    :transform-origin "center"}))}]]]))


(defn copy-button-css
  [& [{:keys [rest-bg
              rest-fg

              active-bg
              active-fg

              hover-bg
              hover-fg]
       :or {rest-bg 'white
            rest-fg 'black

            hover-bg "#eee"
            hover-fg "#777"

            active-bg 'black
            active-fg 'white}}]]
  [:.copy-button
   (transition "background-color 1s ease")
   [:a
    {:padding "6px"}
    [:i
     (transition "color 0.1s ease")
     {:color rest-fg}]]
   [:&:hover [:i
              (transition "color 0.5s ease")
              {:color hover-fg}]]
   [:&.highlighted
    (transition "none")
    {:background-color active-bg}
    [:a [:i
         {:color active-fg}
         (transition "none")]]]])

#?
(:cljs
 (defn $copy-button [_]
   (let [!copied? (r/atom false)
         !hover? (r/atom nil)
         !ct (atom 0)]
     (r/create-class
       {:component-did-update
        (page/cdu-diff
          (fn [[{ot :text}] [{nt :text}]]
            (when (not= ot nt)
              (reset! !copied? false))))
        :reagent-render
        (fn [{:keys [text
                     style

                     rest-bg
                     rest-fg

                     active-bg
                     active-fg

                     hover-bg
                     hover-fg]}]
          [:div.copy-button.text-center
           {:class (when @!copied?
                     "highlighted")
            :style (merge
                     (transition "background-color 1s ease")
                     (if @!copied?
                       (merge
                         {:background-color active-bg}
                         (transition "none"))
                       {:background-color rest-bg})
                     style)
            :on-mouse-over
            (fn [])}
           [:a {:style {:display 'block
                        :padding "0 6px"}
                :href "#"
                :on-mouse-over
                (fn [e]
                  (.preventDefault e)
                  (reset! !copied? false)
                  #_(reset! !hover? true)
                  nil)
                :on-mouse-out
                (fn [e]
                  (.preventDefault e)
                  #_(reset! !hover? false)
                  nil)
                :on-click
                (fn [e]
                  (.preventDefault e)
                  (.stopPropagation e)
                  (let [ta (.createElement js/document "textarea")
                        x (.-scrollX js/window)
                        y (.-scrollY js/window)]
                    (dommy/set-style! ta :position "absolute")
                    (dommy/set-style! ta :bottom 0)
                    (dommy/set-style! ta :opacity 0)
                    (set! (.-value ta) text)
                    (.appendChild
                      (.-body js/document)
                      ta)
                    (.focus ta)
                    (.scrollTo js/window x y)
                    (.select ta)
                    (.execCommand js/document "copy")
                    (.removeChild
                      (.-body js/document)
                      ta)
                    (reset! !copied? true)
                    (go
                      (let [ct-val (nu/now)]
                        (reset! !ct ct-val)
                        (<! (timeout 16))
                        (when (= @!ct ct-val)
                          (reset! !copied? false)))))
                  nil)}
            [:i.ion-ios-copy
             {:style (merge
                       (transition "color 0.5s ease")
                       (if @!copied?
                         (merge
                           {:color active-fg}
                           (transition "none"))
                         {:color rest-fg})
                       {:font-size 22
                        :margin 5})}]]])}))))
