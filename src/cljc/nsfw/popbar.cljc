(ns nsfw.popbar
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
   (def !popbar (r/atom nil))

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

   (def show (gen-show !popbar))

   (defn gen-hide [!state]
     (fn []
       (swap! !state assoc
         :visible? false)
       (go
         (<! (timeout 300))
         (swap! !state assoc
           :current-view-key nil))))

   (def hide (gen-hide !popbar))

   (def gen-current-view-key
     (fn [!state]
       (:current-view-key @!state)))

   (defn current-view-key []
     (gen-current-view-key !popbar))

   (defn gen-visible? [!state]
     (fn []
       (:visible? @!state)))

   (def visible? (gen-visible? !popbar))

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
                    on-visible-change
                    !state]}]
           (page/ensure-opts args)

           !state (or !state !popbar)]

       (swap! !state assoc
         :visible? visible?
         :current-view-key initial-view
         :current-args initial-args)
       (let [!ui (r/atom {:anim-state (if visible?
                                        :post-in
                                        :post-out)})
             on-keydown (fn [e]
                          (when (= 27 (.. e -keyCode))
                            (swap! !state assoc :visible? false)))]
         (r/create-class
           {:component-did-mount
            (fn [_]
              (dommy/listen!
                js/window
                :keydown
                on-keydown)
              (add-watch !state :anims
                (fn [_ _ {ov? :visible?} {nv? :visible?}]
                  (when (not= ov? nv?)
                    (when on-visible-change
                      (on-visible-change nv?))
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
              (remove-watch !state :anims))
            :reagent-render
            (fn [& args]
              (let [[{:keys [style
                             stick-to]
                      :or {stick-to :bot}}
                     views]
                    (page/ensure-opts args)

                    {:keys [anim-state]} @!ui
                    view-lookup (nu/lookup-map
                                  :key
                                  views)
                    current-view-key (or (:current-view-key @!state)
                                         initial-view)

                    current-view (if (keyword? current-view-key)
                                   (get view-lookup current-view-key)
                                   {:comp current-view-key})

                    comp (:comp current-view)

                    comp (if (fn? comp)
                           (apply comp (:current-args @!state))
                           comp)

                    visible? (get @!state :visible?)

                    stick-to (or
                               (:stick-to current-view)
                               stick-to)

                    wrap-style
                    (condp = stick-to
                      :bot (merge
                             {:left 0
                              :right 0
                              :bottom 0}
                             (if visible?
                               (nc/transform "translate3d(0,0,0)")
                               (nc/transform "translate3d(0,15px,0)")))
                      :right (merge
                               {:top 0
                                :right 0
                                :bottom 0}
                               (if visible?
                                 (nc/transform "translate3d(0,0,0)")
                                 (nc/transform "translate3d(15px,0,0)")))

                      :left (merge
                              {:top 0
                               :left 0
                               :bottom 0}
                              (if visible?
                                (nc/transform "translate3d(0,0,0)")
                                (nc/transform "translate3d(-15px,0,0)")))
                      :top (merge
                             {:left 0
                              :right 0
                              :top 0}
                             (if visible?
                               (nc/transform "translate3d(0,0,0)")
                               (nc/transform "translate3d(0,-15px,0)"))))]
                (when (and visible?
                           (not comp))
                  (throw (js/Error.
                           (str "No view for key: "
                                (pr-str current-view)))))

                [:div.bottombar-wrapper
                 {:style (merge
                           {:position 'fixed
                            :opacity (if visible? 1 0)
                            :pointer-events (if visible? "auto" "none")}
                           wrap-style
                           (nc/transition "opacity 0.15s ease, transform 0.15s ease")
                           style)
                  :on-scroll (fn [e]
                               (.stopPropagation e))}
                 comp]))}))))

   (defn $standalone [!state & args]
     (let [[opts & children] (page/ensure-opts args)]
       (vec
         (concat
           [$container
            (merge
              {:!state !state}
              opts)]
           children))))))
