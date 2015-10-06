(ns nsfw.comps
  (:require [nsfw.util :as util]
            [reagent.core :as rea]
            [dommy.core :refer [listen! unlisten!
                                add-class! remove-class!] :refer-macros [sel1]]
            [clojure.string :as str]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]]
            [clojure.string :as str]
            [cljs.reader :as reader])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn code-valid? [code-str]
  (try
    (reader/read-string code-str)
    true
    (catch js/Error e
      false)))

(defn decode [code-str]
  (reader/read-string code-str))

(defn $clojure-editor [{:keys [!state on-update editing?]}]
  (let [!comp (rea/atom
                {:editing? editing?
                 :text (util/pp-str @!state)})]
    (fn []
      (let [{:keys [editing? text]} @!comp]
        [:div.clojure-editor
         [:div.controls
          (if editing?
            [:div
             [:a {:href "#"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (swap! !comp assoc :editing? false))}
              "Cancel"]
             " | "
             (if (code-valid? text)
               [:a {:href "#"
                    :on-click
                    (fn [e]
                      (.preventDefault e)
                      (on-update
                        (decode text)
                        (fn []
                          (swap! !comp assoc
                            :editing? false)))
                      nil)}
                "Save"]
               [:span.disabled "Save"])]
            [:a {:href "#"
                 :on-click (fn [e]
                             (.preventDefault e)
                             (swap! !comp assoc :editing? true)
                             nil)}
             "Edit"])]
         (if editing?
           [:div.code-editor
            [:textarea
             {:name "foo"
              :value text
              :on-change (fn [e]
                           (swap! !comp assoc :text (.. e -target -value))
                           nil)}]]
           [:pre (util/pp-str @!state)])
         ]))))

(defn $table [{:keys [data fields]}]
  [:table.table
   [:thead
    [:tr
     (->> fields
          (map (fn [{:keys [title]}]
                 ^{:key title}
                 [:th title])))]]
   [:tbody
    (for [o data]
      ^{:key (pr-str o)}
      [:tr
       (->> fields
            (map (fn [{:keys [title key render]}]
                   ^{:key title}
                   [:td
                    (let [data (if key (key o) o)]
                      (if render
                        (render data data)
                        data))])))])]])
