(ns nsfw.popover
  (:require [nsfw.css :as css]
            [nsfw.util :as nu]
            [nsfw.page :as page]
            [reagent.core :as r]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn cdu-diff-multi [f]
  (fn [this [_ & old-args]]
    (let [new-args (rest (r/argv this))]
      (f old-args new-args))))

(defn $wrap [& args]
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
                     animate-content-transition?
                     :animate-content-transition?

                     :as no}] next-state]
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
       (cdu-diff-multi
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
                       color
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
                   (swap! !state update-in [0 :visible?] not))}))
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
                       (css/transition "opacity 0.1s ease"))}
             [:div
              {:style (merge
                        {:transform (if visible?
                                      "translateY(0)"
                                      "translateY(5px)")}
                        (css/transition "opacity 0.1s ease, transform 0.1s ease"))}
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
                           :padding "5px 10px"
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

                      :points "0,100 50,8 100,100"}])]])]]]))})))
