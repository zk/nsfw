(ns devguide.entry
  (:require [nsfw.page :as page]
            [nsfw.ops :as ops]
            [nsfw.affix :as affix]
            [nsfw.util :as util]
            [nsfw.forms :as forms]
            [reagent.core :as rea]))

(enable-console-print!)

(def forms-section
  [:div.dg-section
   [:div.sec2
    [:div.sec3
     [:h3 "Forms"]
     [:p "Form layout, validation, communication."]]
    [:div.sec4
     [:div.flex-row
      [:h3 "Input"]
      [:div
       [:code "nsfw.forms/input"]]]
     [:p "For generating the html " [:code "input"] " element. Supports 'binding' to a cursor and optional path for easy data updates, and input / update formatters (for validation / masking)."]
     [:h4 "Basic Usage"]
     (let [!c (rea/atom
                {:full-name "Zachary Kim"
                 :phone "123-456-7890"})
           !le (rea/atom "Last Event")]
       [:div
        [(fn []
           [:pre
            (util/pp-str '(forms/input
                            {:!cursor !c
                             :path [:full-name]}))
            "\n"
            "@!c -> \n" (util/pp-str @!c)])]
        [:div.sec4
         [(fn []
            [:div.example
             (forms/input {:!cursor !c
                           :path [:full-name]
                           :on-click (fn [& args]
                                       (reset! !le
                                         (str "Click: " (util/pp-str args)))
                                       nil)})])]]
        [:div.sec4
         [(fn []
            [:pre @!le])]]])]
    [:div.sec4
     [:div.flex-row
      [:h3 "Textarea"]
      [:div
       [:code "nsfw.forms/textarea"]]]
     [:p]
     [:h4 "Basic Usage"]
     (let [!c (rea/atom
                {:full-name "Zachary Kim"
                 :phone "123-456-7890"})]
       [:div
        [(fn []
           [:pre
            (util/pp-str '(forms/textarea {:!cursor !c
                                           :path [:full-name]}))
            "\n"
            "@!c -> \n" (util/pp-str @!c)])]
        [:div.sec4
         [(fn []
            [:div.example
             (forms/textarea {:!cursor !c
                              :path [:full-name]})])]]])]

    [:div.sec4
     [:div.flex-row
      [:h3 "Select"]
      [:div
       [:code "nsfw.forms/select"]]]
     [:p]
     [:h4 "Basic Usage"]
     (let [!c (rea/atom
                {:full-name "Zachary Kim"
                 :phone "123-456-7890"})]
       [:div
        [(fn []
           [:pre
            (util/pp-str '(forms/select
                            {:!cursor !c
                             :path [:full-name]}
                            [{:text "Hello"
                              :value "id1"}]))
            "\n"
            "@!c -> \n" (util/pp-str @!c)])]
        [:div.sec4
         [(fn []
            [:div.example
             (forms/select
               {:!cursor !c
                :path [:full-name]}
               [{:text "Hello"
                 :value "id1"}
                {:text "World"
                 :value "id2"}])])]]])]]])

(def layout-section
  [:div.dg-section
   [:div.sec2
    [:div.sec3
     [:h2 "Layout"]
     [:p "Utilities for putting things on the page."]]
    [:div.sec4
     [:div.flex-row
      [:h3 "Affix"]
      [:div
       [:code "nsfw.affix"]
       (->> ["cljs" "reagent"]
            (map (fn [s]
                   [:span.badge s])))]]

     [:p "Similar to Bootstrap's affix plugin, it allows you to fix content at certain scroll points. " [:em "Contents will not be re-rendered on atoms that are derefed outside the scope of the call to " [:code "$wrap"] "."]
      " If you have content that needs to update, pass through an atom."]
     [:p "Internally, scroll events are rate-limited to a frequency of 16ms, making the affix transition smooth like butter."]
     [:div.sec5
      [:h5 "Usage"]
      [:pre
       (util/pp-str '[affix/$wrap
                      {:margin 0}
                      [:ul.nav
                       [:li "Reqeust Rendering"]
                       [:li "Layout"]]])]]
     [:div.sec5
      [:h5 "Options (first arg)"]
      [:div.options-table
       (->> {:margin "Distance from top in pixels"
             :preserve-height? "Bool, will leave a wrapper div on the page with a height equivalent to the affixed element. Useful for using affix with in-page-flow elements."
             :scroller "Element or selector, optional. Used to calculate scroll relative to. Defaults to the Window object."
             :on-offset "Event callback. Called with the current offset of something in pixels."}
            (map (fn [[k v]]
                   ^{:key k}
                   [:div.row
                    [:div.col-xs-4.key.col-md-3
                     (str k)]
                    [:div.col-xs-8.val.col-md-9
                     v]])))]]]]])

(defn $page []
  [:div.container
   [:div.row
    [:div.col-sm-12
     [:div.sec4
      [:div.nsfw-logo
       [:h1 "NSFW"]
       [:div.title
        [:h2 "Dev"]
        [:h2 "Guide"]]]]]]
   [:div.row
    [:div.col-sm-3
     [affix/$wrap
      {:margin 0}
      [:ul.nav
       [:li "Reqeust Rendering"]
       [:li "Layout"]]]]
    [:div.col-sm-9
     [:div.sec1
      layout-section
      forms-section
      (->> (range 100)
           (map (fn [i]
                  ^{:key i}
                  [:div i])))
      [:ul
       [:li "Request Handling"]]]
     #_[:p "NSFW is a library for building web and mobile applications using the Clojure ecosystem."]]]])

(defn main [env]
  (let [!app (rea/atom {})
        bus (ops/bus
              {:!app !app}
              {})]
    (rea/render-component
      [$page !app bus]
      (.-body js/document))
    (fn [])))

(defonce reload-hook
  (page/reloader
    (fn []
      {:main main})))
