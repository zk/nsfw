(ns nsfw.affix
  (:require [reagent.core :as rea]
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

(defn calc-el-top [$el $scroller margin offset-top]
  (let [{:keys [width height top left]} (dommy/bounding-client-rect $el)
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
  (let [{:keys [width height top left]} (dommy/bounding-client-rect $el)
        scroll-height (page-height) #_(:height (bounding-client-rect (sel1 :body)))
        {:keys [el-top scroll-top]} (calc-el-top $el $target margin offset-top)
        state (or state :affix-top)]
    (cond
      (and (= state :affix-top)
           (<= scroll-top el-top))
      (merge
        prev
        {:state :affix-top
         :orig-top el-top})

      (and (= state :affix-top)
           (> scroll-top el-top))
      (merge
        prev
        {:state :affix
         :orig-top orig-top})

      (and (= state :affix)
           (> scroll-top orig-top))
      prev

      (and (= state :affix)
           (<= scroll-top orig-top))
      (merge
        prev
        {:state :affix-top})

      :else prev)))

(defn $wrap [{:keys [scroller margin] :as opts} & children]
  (rea/create-class
    {:component-will-receive-props (fn [& args] true)
     :component-did-mount
     (fn [this]
       (let [$scroller (if scroller (sel1 scroller) js/window)
             margin (or margin 0)
             with-state (:with-state opts)
             $el (rea/dom-node this)
             !prev (atom (merge
                           (select-keys opts [:offset-top])
                           {:orig-top (:el-top (calc-el-top $el $scroller margin 0))}))
             handler (fn [e]
                       (let [prev @!prev
                             current (check-position $el $scroller margin prev)]
                         (when with-state
                           (with-state prev current))
                         (when (not= (:state prev) (:state current))
                           (let [state (or (:state current) :affix-top)]
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
                         (reset! !prev current)))]
         (handler #js {:target $scroller})
         (listen! $scroller :scroll handler)
         (rea/set-state this
           {:$scroller $scroller
            :handler handler})))

     :component-will-unmount
     (fn [this]
       (prn "WILL UNMOUNT")
       (let [{:keys [$scroller handler] :as state} (rea/state this)]
         (unlisten! $scroller :scroll handler)))

     :reagent-render
     (fn []
       [:div.affix-wrapper
        (first children)
        #_(doall
            (map-indexed
              (fn [i c]
                (with-meta c {:key i}))
              children))])}))
