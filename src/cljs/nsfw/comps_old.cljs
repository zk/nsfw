(ns nsfw.comps-old
  (:require [nsfw.util :as util]
            [reagent.core :as r]
            [dommy.core :refer [listen! unlisten!
                                add-class! remove-class!] :refer-macros [sel1]]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
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
  (let [!comp (r/atom
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
                      (when on-update
                        (on-update
                          (decode text)
                          (fn []
                            (swap! !comp assoc
                              :editing? false))))
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

(defn $search [{:keys [on-change]}]
  [:div.search
   [:form
    [:input
     {:name "search-query"
      :on-change (fn [e]
                   (.preventDefault e)
                   (on-change (.. e -target -value)))
      :placeholder "Search"}]]])


(defn $prog-rot
  [{:keys [style color loading? size]}]
  [:div.prog-rot-sm
   {:class (when loading? "loading")
    :style style}
   [:div.box
    {:style (merge
              (when size
                {:width size
                 :height size})
              (when color
                {:background-color color}))}]])

(defn cdu-diff-multi [f]
  (fn [this [_ & old-args]]
    (let [new-args (rest (r/argv this))]
      (f old-args new-args))))


(defn $prog-bar-mock [_]
  (let [!ui (r/atom nil)
        !run (atom true)]
    (r/create-class
      {:reagent-render
       (fn [{:keys [loading? done? style stick-to]}]
         [:div.prog-bar-mock
          {:class (when done? "done")
           :style (merge
                    {:overflow 'hidden
                     :height 5}
                    (cond
                      (not stick-to)
                      {:width "100%"}

                      (= :top stick-to)
                      {:position 'absolute
                       :top 0
                       :left 0
                       :right 0}

                      (= :bot stick-to)
                      {:position 'absolute
                       :bottom 0
                       :left 0
                       :right 0})
                    style)}
          (when loading?
            [:div {:style {:position 'relative
                           :width "100%"
                           :height "100%"}}
             [:div.prog-bar-mock-bar
              {:style {:height 5
                       :width "100%"
                       :background-color 'green}}]
             [:div.prog-bar-mock-done-bar
              {:class (when done? "done")
               :style {:position 'absolute
                       :top 0
                       :left 0
                       :right 0
                       :bottom 0}}]])])})))
