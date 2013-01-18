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

(defn frame-repeat [f]
  (frame (fn [] (f) (frame-repeat f))))

(defn cross [transition]
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

(def c (atom 0))

(defn trans [el val duration]
  (let [start-val (-> el dom/bounds :left)
        start (now)
        end val
        calc (fn [t] (+ start-val (* (- end start-val) t)))
        move (fn [t]
               (dom/style el {:left (str (calc t) "px")}))]
    (letfn [(on-frame []
              (let [n (now)
                    elapsed (- (now) start)
                    t (/ elapsed duration)]
                (when (<= t 1)
                  (move t)
                  (util/log (swap! c inc))
                  (frame on-frame))))]
      (frame on-frame))))

#_(defn transition [el val]
  (swap! anims #(conj % (trans el val))))