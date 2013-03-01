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

(def transition-prop
  (let [styles (.-style (.createElement js/document "a"))
        props ["webkitTransition" "MozTransition" "OTransition" "msTransition"]]
    (or (first (filter #(= "" (aget styles %)) props))
        "Transition")))

(def trans-end-prop
  (condp = transition-prop
    "webkitTransition" "webkitTransitionEnd" ; webkit
    "OTransition" "oTransitionEnd" ; opera
    "transitionend")) ; moz & spec

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

#_(defn transition [el val]
  (swap! anims #(conj % (trans el val))))

(defn interpolate [src target pos]
  (+ source
     (* (- target src)
        pos)))

(def parse-el (dom/$ [:div]))

(def props (->> '[backgroundColor
                  width]
                (map str)))

(defn normalize [el style]
  )

(defn transition [{:keys [dur ease] :or {dur "1s"}} props]
  (->> props
       (map #(->> [(name %)
                   dur
                   ease]
                  (filter identity)
                  (interpose " ")
                  (apply str)))
       (interpose ", ")
       (apply str)))

(defn act
  [el style opts]
  (let [t (->> (keys style)
               (transition opts))]
    (aset (aget el "style") transition-prop t)
    (dom/style el style)
    (dom/listen el trans-end-prop #(aset (aget el "style") transition-prop ""))
    el))