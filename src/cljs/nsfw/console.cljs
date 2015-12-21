(ns nsfw.console
  (:require [dommy.core :as dommy :refer-macros [sel1]]
            [reagent.core :as rea]
            [nsfw.util :as util]))

(defn $page [!console !state test-states]
  (let [{:keys [open?]} @!console]
    [:div
     {:style {:position 'fixed
              :top 0
              :bottom 0
              :left 0
              :right 0
              :background-color "white"
              :opacity 0.8
              ;;:display (if open? 'block 'none)
              :transform (if open? "translateY(0)" "translateY(100%)")
              :transition "transform 0.3s ease"
              :overflow-y 'scroll
              :padding "30px"}}
     [:div.container-fluid
      [:div.row
       [:div.col-sm-2
        (for [{:keys [title state]} test-states]
          ^{:key title}
          [:div
           [:a {:href "#"
                :on-click (fn [e]
                            (.preventDefault e)
                            (reset! !state state)
                            nil)}
            title]])]
       [:div.col-sm-10
        [:pre
         {:style {:background-color 'transparent
                  :border 'none}}
         (util/pp-str @!state)]]]]]))

(defn attach [!state test-states]
  (let [body (sel1 :body)
        $el (dommy/create-element :div)
        !console (rea/atom {:open? false})
        on-key (fn [e]
                 (let [key? (= 15 (.-keyCode e))
                       ctrl-key? (.-ctrlKey e)]
                   (when (and key? ctrl-key?)
                     (.preventDefault e)
                     (swap! !console update-in [:open?] not))))]
    (dommy/append! body $el)
    (dommy/listen! js/window :keypress on-key)
    (rea/render-component
      [$page !console !state test-states]
      $el)
    {:$el $el
     :on-key on-key}))

(defn detach [{:keys [$el on-key]}]
  (dommy/remove! $el)
  (dommy/unlisten! js/window :onkeypress on-key))
