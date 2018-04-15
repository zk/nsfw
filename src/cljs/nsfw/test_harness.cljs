(ns nsfw.test-harness
  (:require [dommy.core :as dommy]
            [reagent.core :as r]
            [nsfw.util :as nu]
            [nsfw.css2 :as nc]
            [nsfw.comps2 :as comps]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(nc/inject-css-defs
  {:sizes {:xs 5 :sm 10 :md 20 :lg 50 :xl 100}
   :fonts {:header "'Helvetica Neue', Helvetica, sans-serif"
           :copy "'Helvetica Neue', Helvetica, sans-serif"
           :impact "'Helvetica Neue', Helvetica, sans-serif"}})

(defonce !current (r/atom nil))


#_(when (:play-all? @!current)
    (swap! !current :play-plans (cycle (:plans @!current)))
    (go-loop []
      (when (:play-all? @!current)
        (swap! !current assoc
          :selected-plan (first (:play-plans @!current))
          :play-plans (reset (:play-plans @!current)))
        (reload!)
        (<! (timeout 2000))
        (recur))))

(defn on-keydown [e]
  (when (= 27 (.. e -keyCode))
    (swap! !current assoc :open? false)))

(defn reload! []
  (doseq [selector (:component-selectors @!current)]
    (doseq [$el (dommy/sel selector)]
      (r/unmount-component-at-node $el)))
  (dommy/unlisten! js/window :keydown on-keydown)
  (when-let [f (-> @!current :reload-fn)]
    (f)))

(defn set-reload-fn! [f]
  (swap! !current assoc :reload-fn f))

(defn set-component-selectors! [sels]
  (swap! !current assoc :component-selectors sels))

(defn set-plans! [plans]
  (swap! !current assoc :plans plans))

(defn plan->id [plan]
  (str (:section plan) (:title plan)))

(defn $state-editor [state-refs]
  (let [!state (:!state state-refs)]
    [:pre
     {:style {:width "100%"
              :height 400
              :color 'black}}
     (nu/pp-str
       @!state)]))

(defn $console-root []
  (let [{:keys [plans console-opts open?
                selected-plan-id]}
        @!current
        {:keys [position]} console-opts]
    [:div
     {:style (merge
               {:position 'fixed
                :z-index 10000
                :min-width 150
                :background-color 'black
                :color 'white}
               (condp = position
                 :top-right {:top 0
                             :right 0}
                 {:bottom 0
                  :left 0}))}
     [:div
      {:style (merge
                nc/flex-right
                {:cursor 'pointer})}
      [:div
       {:style {:padding (str xspx " " smpx)}
        :on-click (fn [e]
                    (let [same-key? (= :edit-state (:tab-key @!current))]
                      (.preventDefault e)
                      (swap! !current
                        (fn [current]
                          (-> current
                              (update-in [:open?] not)
                              (assoc-in [:tab-key] :edit-state))))
                      nil))}
       "State"]
      [:div
       {:style (merge pad-xs)
        :on-click (fn [e]
                    (let [same-key? (= :test-state-picker (:tab-key @!current))]
                      (.preventDefault e)
                      (swap! !current
                        (fn [current]
                          (-> current
                              (update-in [:open?] not)
                              (assoc-in [:tab-key] :test-state-picker))))
                      nil))}
       "Test States"]]
     (when open?
       [:div
        {:style {:border-top "solid white 1px"}}
        [comps/$cstack
         {:view (:tab-key @!current)
          :transition :quickfade}
         [{:key :edit-state
           :comp (fn [nav-to]
                   [$state-editor (:state-refs @!current)])}
          {:key :test-state-picker
           :comp (fn [nav-to]
                   [:div
                    #_[:a
                       {:href "#"
                        :style {:display 'block
                                :color 'white
                                :padding "5px 10px"}
                        :on-click (fn [e]
                                    (.preventDefault e)
                                    (swap! !current update :play-all? not)
                                    nil)}
                       "Play All"]
                    (->> plans
                         (map (fn [{:keys [title section] :as plan}]
                                [:a
                                 {:key (str section "-" title)
                                  :href "#"
                                  :style {:display 'block
                                          :color 'white
                                          :padding "5px 10px"
                                          :font-weight (if (= selected-plan-id (plan->id plan))
                                                         'bold
                                                         'normal)}
                                  :on-click (fn [e]
                                              (.preventDefault e)
                                              (swap! !current assoc :selected-plan-id (plan->id plan))
                                              (reload!)
                                              nil)}
                                 title])))])}]]])]))

(defn attach-console! [opts]
  (when-let [$console (dommy/sel1 "#test-harness-console")]
    (r/unmount-component-at-node (dommy/sel1 "#test-harness-console"))
    (dommy/remove! $console))
  (let [$test-harness-console
        (dommy/set-attr! (dommy/create-element :div) :id "test-harness-console")]
    (dommy/append! (dommy/sel1 "body") $test-harness-console)
    (swap! !current assoc :console-opts opts)
    (dommy/listen! js/window :keydown on-keydown)
    (r/render-component
      [$console-root]
      $test-harness-console)))

(defn selected-plan! []
  (->> @!current
       :plans
       (filter #(= (str (:section %) (:title %))
                   (:selected-plan-id @!current)))
       first))

(defn init! [{:keys [component-selectors console plans state-refs]}]
  (set-component-selectors! component-selectors)
  (attach-console! console)
  (set-plans! plans)
  (swap! !current assoc :state-refs state-refs))

;; Usage
#_ (th/init!
     {:component-selectors (keys components)
      :console {:position :top-right}
      :plans test/EMPORIUM_ACCOUNT_TEST_PLANS})
