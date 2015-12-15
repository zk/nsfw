(ns devguide.entry
  (:require [nsfw.page :as page]
            [nsfw.ops :as ops]
            [nsfw.affix :as affix]
            [nsfw.util :as util]
            [nsfw.forms :as forms]
            [reagent.core :as rea]
            [clojure.string :as str]
            [devguide.common :as com]
            [devguide.intro :as intro]
            [devguide.request-handling :as request-handling]
            [devguide.spas :as spas]
            [devguide.markdown :as markdown]))

(enable-console-print!)

(def forms-section
  [:div.dg-section
   [:div.sec3
    [:div.sec3
     [:div.sec-header
      [:h2 "Forms"]]
     [:p "Is there anything as tedious as coding up forms? These components will help."]]
    [:div.sec3
     [:div.flex-row
      [:h3 [:code "<input />"]]
      [:div
       [:code "nsfw.forms/input"]]]
     [:p "For generating the html " [:code "input"] " element. Supports 'binding' to a cursor and optional path for easy data updates, and input / update formatters (for validation / masking)."]
     (com/$options {:!cursor "Backing cursor"
                    :path "Vector path into backing cursor"
                    :parse-value "Function called with value of input before update to backing cursor."
                    :format-value "Function called to format value shown in input."})
     [:h5 "Basic Usage"]
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
            [:pre @!le])]]])
     [:h5 "Value Display vs. Representation"]
     [:p "Sometimes the backing value must be formatted for display in the input, or what's in the input must be parsed in certain way to be stored in the backing cursor. For example, you may need to parse a text input as an integer, or insert dashes into a phone number (masking)."]
     [:p "Pass functions into "
      [:code "parse-value"]
      " and "
      [:code "format-value"]
      " to accomplish these. For the case of handling a phone number input, we'd like to store it as a string of digits, but we'd like to display it in the input with dashes separating groups:"]
     (let [!c (rea/atom {:phone-number "1234567890"})]
       [:div
        [(fn []
           [:pre
            (util/pp-str '(forms/input {:!cursor !c
                                        :path [:phone-number]
                                        :format-value
                                        (fn [s]
                                          (->> [(take 3 s)
                                                (take 3 (drop 3 s))
                                                (take 4 (drop 6 s))]
                                               (remove empty?)
                                               (interpose "-")
                                               flatten
                                               (apply str)))
                                        :parse-value
                                        (fn [s]
                                          (str/replace s #"[^0-9]" ""))}))
            "\n"
            "@!c ->\n" (util/pp-str @!c)])]
        [:div.example
         [(fn []
            (forms/input {:!cursor !c
                          :path [:phone-number]
                          :format-value
                          (fn [s]
                            (->> [(take 3 s)
                                  (take 3 (drop 3 s))
                                  (take 4 (drop 6 s))]
                                 (remove empty?)
                                 (interpose "-")
                                 flatten
                                 (apply str)))
                          :parse-value
                          (fn [s]
                            (str/replace s #"[^0-9]" ""))}))]]])]
    [:div.sec3
     [:div.flex-row
      [:h3 [:code "<textarea />"]]
      [:div
       [:code "nsfw.forms/textarea"]]]
     [:p]
     [:h5 "Basic Usage"]
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
    [:div.sec3
     [:div.flex-row
      [:h3 [:code "<select />"]]
      [:div
       [:code "nsfw.forms/select"]]]
     [:p "Notice that the initial value of " [:code ":full-name"] " is not set to the inital value of the selected option."]
     [:h5 "Basic Usage"]
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
   [:div.sec1
    [:div.sec3
     [:div.sec-header
      [:h2 "Layout"]]
     [:p "Utilities for putting things on the page."]]
    [:div.sec3
     [:div.flex-row
      [:h3 "Affix"]
      [:div
       [:code "nsfw.affix"]
       (->> ["cljs" "reagent"]
            (map (fn [s]
                   ^{:key s}
                   [:span.badge s])))]]
     [:p "Keep stuff on the screen as you scroll."]
     [:p "Similar to Bootstrap's affix plugin, this allows you to fix content at certain scroll points. " [:em "Contents will not be re-rendered on atoms that are derefed outside the scope of the call to " [:code "$wrap"] "."]
      " If you have content that needs to update, pass through an atom."]
     [:p "Want buttery-smooth affix transitions? Make sure there's no jitter as the affixed content transitions from in-flow to fixed positioning by setting the correct top margin in the widget options, and in the css for the affix wrapper."]
     [:div.sec3
      [:h5 "Usage"]
      [:pre
       (util/pp-str '[affix/$wrap
                      {:margin 0}
                      [:ul.nav
                       [:li "Reqeust Rendering"]
                       [:li "Layout"]]])]]
     (com/$options
       {:margin "Distance from top in pixels"
        :preserve-height? "Bool, will leave a wrapper div on the page with a height equivalent to the affixed element. Useful for using affix with in-page-flow elements."
        :scroller "Element or selector, optional. Used to calculate scroll relative to. Defaults to the Window object."
        :on-offset "Event callback. Called with the current offset of something in pixels."})]]])

(defn $page [views nav !app bus]
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
    [:div.col-sm-3.col-md-2
     [affix/$wrap
      {:margin 10}
      [page/nav
       {:!view-key (rea/cursor !app [:view-key])
        :bus bus}
       nav]]]
    [:div.col-sm-9.col-md-10
     [(or (:render (get views (-> @!app :view-key)))
          (fn [!state bus]
            [:div.sec1
             layout-section
             forms-section]))
      (rea/cursor !app [:state])
      bus]]]])

(defn $layout [!state bus]
  [:div.sec1
   layout-section
   forms-section])

(defn main [env]
  (let [!app (rea/atom {})
        views (merge
                intro/views
                request-handling/views
                {:request-rendering
                 {:render (fn [!state bus]
                            [:h1 "request rendering"])
                  :route "/request-rendering"
                  :nav-title "Static Rendering"}}
                markdown/views
                spas/views
                {:layout
                 {:render $layout
                  :route "/layout"
                  :nav-title "Layout"}})

        nav (->> views
                 (map (fn [[k {:keys [nav-title]}]]
                        (when nav-title
                          {:view-key k
                           :title nav-title})))
                 (remove nil?))

        routes ["" (->> views
                        (map (fn [[k {:keys [route]}]]
                               (when route
                                 {route k})))
                        (remove nil?)
                        (reduce merge))]

        bus (ops/bus
              {:!app !app}
              (page/nav-handlers
                {:views views
                 :routes routes}))]
    (page/dispatch-route routes
        (fn [handler route-params]
          (swap! !app
            #(-> %
                 (assoc-in [:view-key] handler)
                 (update-in [:state] merge route-params)))))
    (rea/render-component
      [$page views nav !app bus]
      (.-body js/document))
    (fn [])))

(defonce reload-hook
  (page/reloader
    (fn []
      {:main main})))
