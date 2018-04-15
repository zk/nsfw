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
                [nsfw.css :as css]
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
     ["0%" {:transform "scaleX(0) translate3d(0,0,0)"}]
     ["30%" {:transform "scaleX(0.6) translate3d(0,0,0)"}]
     ["55%" {:transform "scaleX(0.75) translate3d(0,0,0)"}]
     ["100%" {:transform "scaleX(1) translate3d(0,0,0)"}])

   [:.prog-bar-mock
    {:transform-origin "left center"}]

   [:.prog-bar-mock-bar
    {:transform-origin "left center"
     :transform "scaleX(0) translate3d(0,0,0)"}
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
        (fn [{:keys [loading? done? style stick-to height]
              :or {height 5}}]
          [:div.prog-bar-mock
           {:class (when done? "done")
            :style (merge
                     {:overflow 'hidden
                      :height height}
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
               {:style {:height height
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
        (let [[{:keys [stage disabled? style]} & children] (page/ensure-opts args)]
          (let [stage (if disabled?
                        nil
                        stage)]
            [:div.flipper-viewbox
             {:class (when stage "updating")
              :style (merge
                       {:overflow 'hidden}
                       style)}
             (vec
               (concat
                 [:div
                  {:style (merge
                            {:transform "translate3d(0,0,0)"}
                            (transition "transform 0.2s ease")
                            (when (= :post stage)
                              {:transform "translate3d(0,100%,0)"})
                            (when (= :pre stage)
                              (merge
                                {:transform "translate3d(0,-100%,0)"}
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
                   {:transform "rotate(45deg) translate3d(0,25%,0)"
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
                   {:transform "rotate(-45deg) translate3d(0,-25%,0)"
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


#?
(:cljs
 (defn $countdown [{:keys [end-ts]}]
   (let [!remaining (r/atom (- end-ts (nu/now)))
         !run? (atom true)]
     (r/create-class
       {:component-did-mount
        (fn []
          (go-loop []
            (reset! !remaining (- end-ts (nu/now)))
            (<! (timeout (if (and (< @!remaining (* 1000 60))
                                  (>= @!remaining 0))
                           16
                           250)))
            (when @!run?
              (recur))))
        :component-will-unmount
        (fn []
          (reset! !run? false))
        :reagent-render
        (fn [{:keys [size title gutter-size]
              :or {size :md
                   gutter-size 20}}]
          (let [font-size (condp = size
                            :sm 30
                            :md 50)]
            [:div
             {:style {:color 'black
                      :font-size font-size
                      :letter-spacing 1
                      :padding 10
                      :text-align 'center
                      :margin-left 'auto
                      :margin-right 'auto}}

             (when title
               [:h4 {:style {:font-weight '700
                             :text-transform 'upppercase
                             :font-size 12
                             :margin-bottom 10
                             :letter-spacing 1}}
                title])
             (let [{:keys [d h m s ms]}
                   (if (> @!remaining 0)
                     (nu/time-delta-parts @!remaining)
                     {:d 0 :h 0 :m 0 :s 0})]
               [page/$interpose-children
                {:separator [:div {:style {:width gutter-size}}]
                 :class "flex-center"}
                (->> [d "days"
                      h "hours"
                      m "minutes"
                      s "seconds"]
                     (partition-all 2)
                     (map-indexed
                       (fn [i [n units]]
                         (let [value (nu/pad n 2)]
                           [:div {:key i
                                  :style {:width "25%"
                                          :text-align 'center}}
                            [:div
                             {:style {:font-size font-size
                                      :line-height "100%"}}

                             value]
                            [:div
                             {:style {:font-size (if (= :sm size)
                                                   10
                                                   14)
                                      :color "rgba(0,0,0,0.6)"}}
                             units]]))))])]))}))))


#?
(:cljs
 (do
   (defn $abl [& args]
     (let [[opts & children] (page/ensure-opts args)]
       (page/elvc
         [:a (merge
               {:target "_blank"
                :rel "noopener"}
               opts)]
         children)))

   (defn $a [& args]
     (let [[{:keys [on-click] :as opts}
            & children]
           (page/ensure-opts args)]
       (page/elvc
         [:a (merge
               {:href "#"}
               opts
               {:on-click (fn [e]
                            (.preventDefault e)
                            (when on-click
                              (on-click))
                            nil)})]
         children)))))


#?
(:cljs
 (defn $popover [& args]
   (let [!ui (r/atom nil)
         !state (r/atom (assoc-in
                          (page/ensure-opts args)
                          [0 :visible?]
                          false))
         ch (chan 10)]
     (r/create-class
       {:component-did-mount
        (fn [_]
          (go-loop []
            (when-let [next-state (<! ch)]
              (let [[{ov :visible?
                      oc :content
                      :as oo}]
                    @!state

                    [{nv :visible?
                      nc :content
                      nmo? :enable-mouse-over?
                      animate-content-transition?
                      :animate-content-transition?

                      :as no}]
                    next-state

                    ;; When vis controlled by mouse / touch, ignore
                    ;; :visible? from args change
                    nv (if nmo?
                         ov
                         nv)

                    next-state (assoc-in next-state [0 :visible?] nv)]

                (cond
                  (and (not= ov nv)
                       (not nv))
                  (do
                    (swap! !state assoc-in [0 :visible?] nv)
                    (<! (timeout 100))
                    (reset! !state next-state))

                  (and (not= oc nc)
                       ov
                       nv
                       animate-content-transition?)
                  (do
                    (swap! !state assoc-in [0 :visible?] false)
                    (<! (timeout 300))
                    (swap! !state
                      (fn [state]
                        (-> state
                            (assoc-in [0 :content] nc)
                            (assoc-in [0 :visible?] true)))))

                  :else (reset! !state next-state))
                (recur))))
          (put! ch (page/ensure-opts args)))
        :component-will-unmount
        (fn [_]
          (close! ch))
        :component-did-update
        (page/cdu-diff
          (fn [[{ov :visible? :as oo} :as old] [{nv :visible? :as no} :as new]]
            (when (not= old new)
              (put! ch (page/ensure-opts new)))))
        :reagent-render
        (fn [_ & _]
          (let [[opts & body] @!state
                {:keys [position
                        style
                        width
                        border-color
                        pop-style
                        enable-mouse-over?
                        offset
                        visible?
                        border-color]
                 po-content :content
                 :or {position :bot-center
                      color 'white
                      width "100%"
                      offset 0}}
                opts

                {:keys [slide-axis
                        slide-dist
                        top
                        bottom
                        left
                        right
                        tx
                        ty
                        h-align
                        v-align
                        carat-side]}

                (condp = position
                  :top-left {:slide-axis "translateY"
                             :slide-dist 5
                             :top (+ -3 offset)
                             :left 0
                             :tx "0"
                             :ty "-100%"
                             :h-align 'left
                             :carat-side :bot}

                  :top-center {:slide-axis "translateY"
                               :slide-dist 5
                               :top (+ -3 offset)
                               :left "50%"
                               :tx "-50%"
                               :ty "-100%"
                               :h-align 'center
                               :carat-side :bot}

                  :top-right {:slide-axis "translateY"
                              :slide-dist 5
                              :top (+ -3 offset)
                              :right 0
                              :tx "0"
                              :ty "-100%"
                              :h-align 'right
                              :carat-side :bot}

                  :bot-left {:slide-axis "translateY"
                             :slide-dist 5
                             :bottom (+ -3 offset)
                             :left 0
                             :tx "0"
                             :ty "100%"
                             :h-align 'left
                             :carat-side :top}

                  :bot-center {:slide-axis "translateY"
                               :slide-dist 5
                               :bottom (+ -3 offset)
                               :left "50%"
                               :tx "-50%"
                               :ty "100%"
                               :h-align 'center
                               :carat-side :top}
                  :bot-right {:slide-axis "translateY"
                              :slide-dist 5
                              :bottom (+ -3 offset)
                              :right 0
                              :tx "0"
                              :ty "100%"
                              :h-align 'right
                              :carat-side :top}

                  :left-center {:slide-axis "translateX"
                                :slide-dist 5
                                :left (+ -3 offset)
                                :top "50%"
                                :tx "-100%"
                                :ty "-50%"
                                :carat-side :right
                                :v-align 'center}

                  :right-center {:slide-axis "translateX"
                                 :slide-dist 5
                                 :right (+ -3 offset)
                                 :top "50%"
                                 :tx "100%"
                                 :ty "-50%"
                                 :carat-side :left
                                 :v-align 'center})

                h-align-margin (merge (when (= h-align 'left)
                                        {:margin-left 0})
                                      (when (= h-align 'right)
                                        {:margin-right 0}))
                v-align-margin {}]
            [:div.popover-wrapper
             (merge
               {:style (merge
                         {:position 'relative}
                         style)}
               (when enable-mouse-over?
                 {:on-mouse-over
                  (fn [e]
                    (swap! !state assoc-in [0 :visible?] true)
                    nil)
                  :on-mouse-out
                  (fn [e]
                    (swap! !state assoc-in [0 :visible?] false)
                    nil)
                  :on-touch-end
                  (fn [e]
                    (swap! !state update-in [0 :visible?] not)
                    nil)}))
             (page/elvc
               [:div.popover-body]
               body)
             [:div
              {:style (merge
                        {:position 'absolute
                         :width width
                         :opacity (if visible?
                                    1
                                    0)
                         :z-index 1000
                         :pointer-events (if visible?
                                           'inherit
                                           'none)}
                        {:transform (str "translate("
                                         tx
                                         ","
                                         ty
                                         ")")}
                        (when top
                          {:top top})
                        (when bottom
                          {:bottom bottom})
                        (when left
                          {:left left})
                        (when right
                          {:right right})
                        (when h-align
                          {:text-align h-align})
                        (transition "opacity 0.1s ease"))}
              [:div
               {:style (merge
                         {:transform (if visible?
                                       "translateY(0)"
                                       "translateY(5px)")}
                         (transition "opacity 0.1s ease, transform 0.1s ease"))}
               (when (= :top carat-side)
                 [:svg {:width "45px"
                        :height "12px"
                        :viewBox "0 0 100 100"
                        :preserveAspectRatio "none"
                        :style (merge
                                 {:shape-rendering "geometricPrecision"
                                  :display 'block
                                  :margin-left 'auto
                                  :margin-right 'auto
                                  :padding 0}
                                 h-align-margin)}
                  [:polygon
                   {:points "0,100 50,0 100,100"
                    :style {:fill 'black}}]
                  (when border-color
                    [:polyline
                     {:fill 'none
                      :stroke border-color
                      :stroke-width 6

                      :points "0,100 50,8 100,100"}])])
               [:div
                {:style {:display "flex"
                         :justify-content 'center
                         :align-items 'center}}
                (when (= :left carat-side)
                  [:svg {:width "6px"
                         :height "16px"
                         :viewBox "0 0 100 100"
                         :preserveAspectRatio "none"
                         :style (merge
                                  {:shape-rendering "geometricPrecision"
                                   :display 'block
                                   :margin-left 'auto
                                   :margin-right 'auto
                                   :padding 0}
                                  v-align-margin)}
                   [:polygon
                    {:points "0,50 100,100 100,0"
                     :style {:fill 'black}}
                    (when border-color
                      [:polyline
                       {:fill 'none
                        :stroke border-color
                        :stroke-width 6

                        :points "0,100 50,8 100,100"}])]])
                [:div
                 {:on-touch-end (fn [e]
                                  (.stopPropagation e)
                                  nil)
                  :style (merge
                           {:flex 1
                            :border-radius 5
                            :overflow 'hidden
                            :background-color 'black
                            :color 'white
                            :padding "7px 10px"
                            :font-size 14
                            :line-height "130%"
                            :text-align 'center
                            :box-shadow "0 4px 8px 0 rgba(0,0,0,0.12), 0 2px 4px 0 rgba(0,0,0,0.08)"}

                           (when border-color
                             {:border (str "solid " border-color " 1px")}
                             )
                           (when (= :top carat-side)
                             {:margin-top -5})
                           (when (= :bot carat-side)
                             {:margin-bottom -5})
                           pop-style)}
                 po-content]
                (when (= :right carat-side)
                  [:svg {:width "6px"
                         :height "16px"
                         :viewBox "0 0 100 100"
                         :preserveAspectRatio "none"
                         :style (merge
                                  {:shape-rendering "geometricPrecision"
                                   :display 'block
                                   :margin-left 'auto
                                   :margin-right 'auto
                                   :padding 0}
                                  v-align-margin)}
                   [:polygon
                    {:points "0,0 0,100 100,50"
                     :style {:fill 'black}}
                    (when border-color
                      [:polyline
                       {:fill 'none
                        :stroke border-color
                        :stroke-width 6

                        :points "0,100 50,8 100,100"}])]])]
               (when (= :bot carat-side)
                 [:svg {:width "45px"
                        :height "12px"
                        :viewBox "0 0 100 100"
                        :preserveAspectRatio "none"
                        :style (merge
                                 {:shape-rendering "geometricPrecision"
                                  :display 'block
                                  :margin-left 'auto
                                  :margin-right 'auto
                                  :padding 0}
                                 h-align-margin)}
                  [:polygon
                   {:points "0,0 50,100 100,0"
                    :style {:fill 'black}}
                   (when border-color
                     [:polyline
                      {:fill 'none
                       :stroke border-color
                       :stroke-width 6

                       :points "0,100 50,8 100,100"}])]])]]]))}))))




(defn $video [{:keys [webm mp4 ogg
                      autoplay? loop? controls?
                      muted? playinline?
                      style]
               :as opts}]
  (let [props (dissoc opts
                :webm :mp4 :ogg
                :autoplay? :loop? :controls?
                :muted? :playinline?)]
    [:video
     (merge
       props
       (when autoplay?
         {:autoPlay "autoPlay"})
       (when loop?
         {:loop "loop"})
       (when controls?
         {:controls "controls"})
       (when muted?
         {:muted "muted"})
       (when playinline?
         {:playinline? playinline?}))
     "Your browser does not support HTML5 video. You should "
     [:a {:href "https://whatbrowser.org"} "consider updating"]
     "."
     (when webm
       [:source {:src webm :type "video/webm"}])
     (when ogg
       [:source {:src ogg :type "video/ogg"}])
     (when mp4
       [:source {:src mp4 :type "video/mp4"}])]))


#?
(:cljs
 (defn $vidbg [& args]
   (let [[{:keys []
           :as opts}
          & children] (page/ensure-opts args)]
     (let [props (dissoc opts
                   :webm :mp4 :ogg
                   :autoplay? :loop? :controls?
                   :muted? :playinline?
                   :buffered :crossorigin :height :width
                   :played :preload :poster
                   :src)]
       [:div (merge
               props
               {:style
                (merge
                  {:position 'relative}
                  (:style props))})
        [:div
         {:style {:position 'absolute
                  :width "100%"
                  :height "100%"
                  :overflow 'hidden}}
         [:div
          {:style {:position 'relative
                   :width "100%"
                   :height "100%"}}
          [$video
           (merge
             (select-keys opts
               [:webm :mp4 :ogg
                :autoplay? :loop? :controls?
                :muted? :playinline?
                :buffered :crossorigin :height :width
                :played :preload :poster
                :src])
             {:style {:display 'block
                      :min-width "100%"
                      :min-height "100%"
                      :position 'absolute
                      :left "50%"
                      :top "50%"
                      :transform "translate3d(-50%,-50%,0)"}})]]]
        (page/elvc
          [:div
           {:style {:z-index 100
                    :position 'absolute
                    :top 0
                    :left 0
                    :width "100%"
                    :height "100%"}}]
          children)]))))


#?
(:cljs
 (defn $cstack [{:keys [on-nav
                        view
                        initial-view
                        transition]
                 :as opts}
                views]
   (let [!ui (r/atom {:view-key (or initial-view
                                    view
                                    (-> views
                                        first
                                        :key))
                      :visible? true})
         change-view (fn [k]
                       (when on-nav
                         (on-nav k))
                       (if (or (= :fade transition)
                               (= :quickfade transition))
                         (go
                           (swap! !ui assoc :visible? false)
                           (<! (timeout (if (= :quickfade transition)
                                          100
                                          200)))
                           (swap! !ui assoc
                             :view-key k
                             :visible? true))
                         (swap! !ui assoc
                           :view-key k
                           :visible? true)))]
     (r/create-class
       {:component-did-update
        (page/cdu-diff
          (fn [[{ov :view}] [{nv :view}]]
            (when (not= ov nv)
              (change-view nv))))
        :reagent-render
        (fn [{:keys [transition]} views]
          (let [views-lookup (->> views
                                  (map (fn [o]
                                         [(:key o) o]))
                                  (into {}))
                {:keys [view-key
                        visible?]}
                @!ui

                slide (get views-lookup view-key)

                comp-or-fn (when slide
                             (:comp slide))
                comp (when slide
                       (if (vector? comp-or-fn)
                         comp-or-fn
                         (comp-or-fn
                           (fn [key]
                             (change-view key)))))]
            [:div
             {:style (merge
                       {:width "100%"
                        :height "100%"
                        :opacity (if visible? 1 0)}
                       (css/transition (str "opacity "
                                            (if (= :quickfade transition)
                                              "0.1s"
                                              "0.2s")
                                            " ease")))}
             comp]))}))))
