(ns nsfw.workout
  (:require [nsfw.dom :as dom]))

(def $body (dom/$ "body"))


(defn log-in [e]
  (js/alert "log in"))

(defn twitter-button []
  (-> (dom/$ [:a.button {:href "#"}
              "Log in with Twitter"])
      (dom/click log-in)))

(defn main []
  (-> $body
      (dom/apd [:div.page
                [:div.header
                 [:a.brand {:href "#"}
                  "Workout"]
                 [:div.nav
                  (twitter-button)]]])))
