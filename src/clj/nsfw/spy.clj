(ns nsfw.spy
  (:require [clojure.string :as str]
            [nsfw]
            [nsfw.util :as util]
            [nsfw.app :as app]
            [nsfw.html :as html]))

(defn query-routes [path]
  (->> path
       app/namespaces-in
       app/routes-in-nss
       (sort-by :route)))

(defn query-comps [path]
  (->> path
       app/namespaces-in
       app/comps-in-nss))

(defn escape [s]
  (-> s
      (str/replace #"<" "&lt;")
      (str/replace #">" "&gt;")))

(nsfw/defroute "/_spy" [r]
  (nsfw/render-html
   [:page-body
    {:class "page-spy"}
    [:div.container
     [:div.row
      [:div.col-lg-12
       [:div.page-lead
        [:h1 "Spy"]
        [:p.lead "Look at what's going on in your app!"]]]]
     [:div.row
      [:div.col-lg-3
       [:ul.nav.demo-nav
        {:data-spy "affix"
         :data-offset-top "190"}
        [:li
         [:a {:href "#routes"} "Routes"]]
        [:li
         [:a {:href "#components"} "Components"]]]]
      [:div.col-lg-9
       [:section.spy
        [:h2#routes "Routes"]
        [:div.routes
         [:table.table.table-striped
          [:thead
           [:tr
            [:th "Path"]
            [:th "Handler"]]]
          [:tbody
           (map (fn [{:keys [path method middleware handler]}]
                  [:tr
                   [:td (or method "*")]
                   [:td [:a {:href path} path]]
                   [:td  handler]])
                (query-routes "src/clj"))]]]]
       [:section.components
        [:h2#components "Components"]
        (map (fn [{:keys [tag var]}]
               [:div.component
                [:h3 (pr-str [tag])]
                [:h4 [:code var]]
                [:pre (-> (if (fn? @var)
                            (@var {} "")
                            @var)
                          util/pp-str)]])
             (->> "src/clj/nsfw_site"
                  query-comps
                  (sort-by :tag)))]]]]]))