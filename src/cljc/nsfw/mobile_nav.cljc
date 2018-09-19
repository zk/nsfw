(ns nsfw.mobile-nav
  #?(:cljs
     (:require [nsfw.util :as nu]
               [nsfw.components :as nc]
               [nsfw.popbar :as pb]
               [nsfw.page :as page]
               [reagent.core :as r])))

#?
(:cljs
 (do
   (def !mobile-nav (r/atom nil))

   (def show-pb (pb/gen-show !mobile-nav))

   (defn show []
     (show-pb :content))

   (def hide (pb/gen-hide !mobile-nav))

   (def visible? (pb/gen-visible? !mobile-nav))

   (defn $container [& args]
     (let [[opts & children] (page/ensure-opts args)]
       [pb/$standalone
        !mobile-nav
        {:style {:height "100%"}
         :class "mobile-nav-container"
         :initial-view :content}
        [{:key :content
          :stick-to :right
          :comp (page/elvc
                 [:div
                  {:style (merge
                            {:height "100%"}
                            (:style opts))}]
                 children)}]]))

   (defn $hamburger-menu [opts]
     [nc/$hamburger-menu
      (merge
        {:open? (visible?)
         :on-toggle
         (fn []
           (if (visible?)
             (hide)
             (show)))}
        opts)])))
