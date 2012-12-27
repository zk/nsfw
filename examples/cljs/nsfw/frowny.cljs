(ns nsfw.frowny
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]))

(defn gen-id []
  (name (gensym)))

(defn remove-frowny! [frownies frowny]
  (swap! frownies (fn [fs]
                    (remove #(= frowny %) fs))))

(defn header []
  (dom/$
   [:div.header
    [:h1 "Frowny"]]))

(defn new-frowny [frownies]
  (dom/$
   [:div.new-frowny
    (-> (dom/$ [:input.frowny-text-input {:autofocus "autofocus"}])
        (dom/match-key
         13 (fn [el val]
              (swap! frownies conj {:id (gen-id) :text val})
              (dom/val el ""))))]))

(defn frowny [frownies frowny]
  [:li.frowny-item
   (:text frowny)
   (-> (dom/$ [:a {:href "#"} "delete"])
       (dom/click #(remove-frowny! frownies frowny)))])

(defn show-frownies [frownies]
  (-> (dom/$ [:ol.frownies])
      (dom/bind-render frownies
                       (fn [fs]
                         (map #(frowny frownies %) fs)))))

(defn main []
  (let [frownies (atom [{:id (gen-id) :text "foo bar"}
                        {:id (gen-id) :text "baz"}
                        {:id (gen-id) :text "bap"}])]
    (-> (dom/$ "body")
        (dom/append
         [:div.page
          (header)
          [:div.content
           (new-frowny frownies)
           (show-frownies frownies)]])
        (dom/append [:div.watermark ":("])
        (dom/append (repeat 100 [:br])))))