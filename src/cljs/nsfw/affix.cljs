(ns nsfw.affix
  (:require [reagent.core :as rea]
            [nsfw.util :as util]
            [dommy.core :as dommy
             :refer [listen! unlisten!]
             :refer-macros [sel1]]))

(defn page-height []
  (max
    (.. js/document -body -scrollHeight)
    (.. js/document -body -offsetHeight)
    (.. js/document -documentElement -scrollHeight)
    (.. js/document -documentElement -clientHeight)
    (.. js/document -documentElement -offsetHeight)))

(defn calc-el-top [$el $scroller margin offset-top client-rect]
  (let [{:keys [width height top left]} client-rect
        scroll-top (if (= js/window $scroller)
                     (.-scrollTop (sel1 :body))
                     (.-scrollTop $scroller))
        el-top (- (+ top scroll-top) offset-top margin)]
    {:el-top el-top
     :scroll-top scroll-top}))

(defn check-position [$el
                      $target
                      margin
                      {:keys [state
                              offset-top
                              offset-bottom
                              orig-top]
                       :or {offset-top 0
                            offset-bottom 0}
                       :as prev}]
  (let [{:keys [width height top left] :as client-rect}
        (dommy/bounding-client-rect $el)

        scroll-height (page-height)

        {:keys [el-top scroll-top]}
        (calc-el-top $el $target margin offset-top client-rect)

        state (or state :affix-top)]
    (cond
      (and (= state :affix-top)
           (<= scroll-top el-top))
      (merge
        prev
        {:state :affix-top
         :offset 0
         :orig-top el-top})

      (and (= state :affix-top)
           (> scroll-top el-top))
      (merge
        prev
        {:state :affix
         :offset 0
         :orig-top orig-top})

      (and (= state :affix)
           (> scroll-top orig-top))
      (merge
        prev
        {:offset (- scroll-top orig-top)})

      (and (= state :affix)
           (<= scroll-top orig-top))
      (merge
        prev
        {:state :affix-top
         :offset 0})

      :else prev)))

(defn throttle [f delta]
  (let [last (atom nil)
        to (atom nil)]
    (fn [& args]
      (cond
        (not @last) (do
                      (reset! last (util/now))
                      (reset! to nil)
                      (apply f args))
        (> @last 0) (let [now (util/now)]
                      (if (> (- now @last) delta)
                        (do
                          (reset! last now)
                          (apply f args))
                        (do
                          (js/clearTimeout @to)
                          (reset! to
                            (js/setTimeout
                              (fn []
                                (reset! last (+ delta @last))
                                (apply f args))
                              (- delta (- now @last)))))))))))

(defn $wrap [{:keys [scroller
                     margin
                     preserve-height?
                     on-offset] :as opts}
             child]
  (rea/create-class
    {:component-will-receive-props (fn [& args] true)
     :component-did-mount
     (fn [this]
       (let [$scroller (if scroller (sel1 scroller) js/window)
             margin (or margin 0)
             with-state (:with-state opts)
             $preserve (rea/dom-node this)
             $el (.item (.-children $preserve) 0)
             !prev (atom (merge
                           (select-keys opts [:offset-top])
                           {:orig-top
                            (:el-top
                             (calc-el-top $el $scroller margin 0
                               (dommy/bounding-client-rect $el)))}))
             handler (fn [e]
                       (let [prev @!prev
                             current (check-position $el $scroller margin prev)]
                         (when with-state
                           (with-state prev current))
                         (when (not= (:state prev) (:state current))
                           (let [state (or (:state current) :affix-top)]
                             (when (and
                                     preserve-height?
                                     (= :affix state))
                               (let [height (:height (dommy/bounding-client-rect $el))]
                                 (dommy/set-style!
                                   $preserve
                                   :height (str height "px"))))
                             (dommy/add-class! $el state)
                             (doseq [c (disj
                                         #{:affix-top :affix-bottom :affix}
                                         state)]
                               (dommy/remove-class! $el (name c)))))
                         (when (not= (:top prev) (:top current))
                           (dommy/set-style! $el
                             :top (if (:top current)
                                    (str (:top current) "px")
                                    (:top current))))
                         (when (and
                                 on-offset
                                 (not= (:offset prev) (:offset current)))
                           (on-offset (:offset current)))
                         (reset! !prev current)))
             handler (throttle handler 16)]
         (handler #js {:target $scroller})
         (listen! $scroller :scroll handler)
         (rea/set-state this
           {:$scroller $scroller
            :handler handler})))

     :component-will-unmount
     (fn [this]
       (let [{:keys [$scroller handler] :as state} (rea/state this)]
         (unlisten! $scroller :scroll handler)))

     :reagent-render
     (fn []
       [:div.affix-preserve
        [:div.affix-wrapper
         child]])}))
