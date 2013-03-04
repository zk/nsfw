(ns nsfw.ani
  (:require [nsfw.dom :as dom]
            [nsfw.util :as util]))

(def frame (let [w js/window]
             (or (.-requestAnimationFrame w)
                 (.-webkitRequestAnimationFrame w)
                 (.-webkitRequestAnimationFrame w)
                 (.-webkitRequestAnimationFrame w)
                 (.-webkitRequestAnimationFrame w)
                 (fn [f] (util/timeout f 17)))))

(def perf (.-performance js/window))

(def perf-now (and perf (or (.-now perf)
                            (.-webkitNow perf)
                            (.-msNow perf)
                            (.-mozNow perf))))

(def now (fn [] (.call perf-now perf)))

(def html (.-documentElement js/document))

(def unitless #{"lineHeight" "zoom" "zIndex" "opacity" "transform"})




 ; moz & spec

(defn frame-repeat [f]
  (frame (fn [] (f) (frame-repeat f))))

(defn cross
  [transition]
  {:-webkit-transition transition
   :-moz-transition transition
   :-o-transition transition
   :transition transition})

(defn css
  "Animated attribute transitions."
  [el attr val]
  (dom/style el (merge (cross (format "%s 1000ms" (name attr)))
                       {attr (str val "px")})))

(def anims (atom []))

(defn now [] (.now js/Date))

(defn interpolate [src target pos]
  (+ source
     (* (- target src)
        pos)))

(def parse-el (dom/$ [:div]))

(def props (->> '[backgroundColor
                  width]
                (map str)))
