(ns nsfw.modal
  #? (:cljs
      (:require [reagent.core :as r]
                [nsfw.util :as nu]
                [nsfw.css :as nc]
                [nsfw.page :as page]
                [rx.css :as css]
                [dommy.core :as dommy]
                [cljs.core.async :as async
                 :refer [<! >! chan close! put! take! timeout]]))
  #? (:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]])))

#?
(:cljs
 (do
   (def !modal (r/atom nil))

   (defn gen-show [!state]
     (fn [view-key & args]
       (if (:visible? @!state)
         (go
           (swap! !state assoc :visible? false)
           (<! (timeout 250))
           (swap! !state assoc
             :visible? true
             :current-view-key view-key
             :current-args args))
         (swap! !state assoc
           :visible? true
           :current-view-key view-key
           :current-args args))))

   (def show (gen-show !modal))

   (defn gen-hide [!state]
     (fn []
       (swap! !state assoc
         :visible? false)
       (go
         (<! (timeout 300))
         (swap! !state assoc
           :current-view-key nil))))

   (def hide (gen-hide !modal))

   (def gen-current-view-key
     (fn [!state]
       (:current-view-key @!state)))

   (defn current-view-key []
     (gen-current-view-key !modal))

   (defn cdu-diff [f]
     (fn [this [_ & old-args]]
       (let [new-args (rest (r/argv this))]
         (f old-args new-args))))

   (defn inject-args [view args]
     (if args
       (if (vector? view)
         (let [[el & tail] view]
           (if (map? (first tail))
             (vec
               (concat
                 [el (merge (first tail) args)]
                 (rest tail)))
             view))
         view)
       view))

   (defn $container [& args]
     (let [[{:keys [visible?
                    initial-view
                    initial-args
                    !override-modal]}]
           (page/ensure-opts args)
           !modal (or !override-modal !modal)]
       (swap! !modal assoc
         :visible? visible?
         :current-view-key initial-view
         :current-args initial-args)
       (let [!ui (r/atom {:anim-state (if visible?
                                        :post-in
                                        :post-out)})
             on-keydown (fn [e]
                          (when (= 27 (.. e -keyCode))
                            (swap! !modal assoc :visible? false)))]
         (r/create-class
           {:component-did-mount
            (fn [_]
              (dommy/listen!
                js/window
                :keydown
                on-keydown)
              (add-watch !modal :anims
                (fn [_ _ {ov? :visible?} {nv? :visible?}]
                  (when (not= ov? nv?)
                    (if nv?
                      (go
                        (swap! !ui assoc :anim-state :pre-in)
                        (<! (timeout 17))
                        (swap! !ui assoc :anim-state :post-in))
                      (go
                        (swap! !ui assoc :anim-state :pre-out)
                        (<! (timeout 300))
                        (swap! !ui assoc :anim-state :post-out)))))))
            :component-will-unmount
            (fn [_]
              (dommy/unlisten!
                js/window
                :keydown
                on-keydown)
              (remove-watch !modal :anims))
            :reagent-render
            (fn [& args]
              (let [[{:keys [style]} views] (page/ensure-opts args)
                    {:keys [anim-state]} @!ui
                    view-lookup (nu/lookup-map
                                  :key
                                  views)
                    current-view-key (or (:current-view-key @!modal)
                                         initial-view)
                    current-view (if (keyword? current-view-key)
                                   (:comp (get view-lookup current-view-key))
                                   current-view-key)

                    current-view (if (fn? current-view)
                                   (current-view (:current-args @!modal))
                                   current-view)
                    visible? (get @!modal :visible?)]
                (when (and visible?
                           (not current-view))
                  (throw (js/Error.
                           (str "No modal view for key: "
                                (pr-str current-view-key)))))
                [:div.modal-wrapper
                 {:style (merge
                           {:position 'fixed
                            :top 0
                            :left 0
                            :right 0
                            :bottom 0
                            :-webkit-backface-visibility "hidden"
                            :-webkit-perspective "1000"
                            :background-color
                            (if (= :post-in anim-state)
                              "rgba(255,255,255,0.95)"
                              "rgba(255,255,255,0.0)")}
                           (if (= :post-out anim-state)
                             (nc/transform "translate3d(-100%,0,0)")
                             (nc/transform "translate3d(0,0,0)"))
                           (nc/transition "background-color 0.15s ease")
                           style)
                  :on-click (fn [e]
                              (.preventDefault e)
                              (hide)
                              nil)}

                 [:div.container
                  {:style {:height "100%"
                           :width "100%"}}
                  [:div
                   {:style {:width "100%"
                            :height "100%"
                            :position 'relative}}
                   [:div.flex-vcenter
                    {:style (merge
                              {:position 'absolute
                               :top 10
                               :left 10
                               :right 10
                               :bottom 10}
                              (if (= :post-in anim-state)
                                (merge
                                  {:opacity 1})
                                (merge
                                  {:opacity 0}))
                              (nc/transition "opacity 0.2s ease"))}
                    [:div
                     {:style (merge
                               {:max-width "100%"
                                :max-height "100%"
                                :background-color 'white
                                :overflow-y 'scroll
                                :box-shadow "0 0 4px 0 rgba(168,167,164,0.4)"
                                :margin 20
                                :-webkit-backface-visibility "hidden"
                                :-webkit-perspective "1000"}

                               (if (= :post-in anim-state)
                                 (merge
                                   (nc/transform "translate3d(0,0,0)"))
                                 (merge
                                   (nc/transform "translate3d(0,15px,0)")))
                               (nc/transition "transform 0.15s ease"))
                      :on-click (fn [e]
                                  (.stopPropagation e)
                                  nil)}
                     current-view]
                    [:div.text-center
                     {:style {:margin-top 10
                              :margin-bottom 20}}
                     [:a {:href "#"
                          :style {:text-decoration 'none
                                  :font-weight '500
                                  :font-size 14}
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (swap! !modal assoc :visible? false)
                                      nil)}
                      "Close"]]]]]]))}))))))
