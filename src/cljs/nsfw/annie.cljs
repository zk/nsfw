(ns nsfw.annie
  (:require
   [nsfw.util :as util]
   [cljs.core.async :as async
    :refer [<! >! chan close! sliding-buffer put! take!
            alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def !animations (atom {}))

(defn spring [obj props])

;; https://github.com/chenglou/react-motion/blob/master/src/stepper.js

(def error-margin 0.001)

(def ms-per-frame (/ 1000 60))

(defn step
  [framerate
   x
   v
   dest-x
   k
   b]
  (let [f-spring (* (- k) (- x dest-x))
        f-damper (* (- b) v)
        a (+ f-spring f-damper)
        new-v (+ v (* a framerate))
        new-x (+ x (* new-v framerate))]
    (if (and (< (util/abs new-v) error-margin)
             (< (util/abs (- new-x dest-x)) error-margin))
      [dest-x 0]
      [new-x new-v])))

(defn raf [f] (.requestAnimationFrame js/window f))

(defn run [from to callback]
  (let [k 118
        b 18]
    (letfn [(do-frame [x v]
              (if (not= x to)
                (let [[next-x next-v] (step (/ ms-per-frame 1000) x v to k b)]
                  (callback next-x)
                  (raf (fn []
                         (do-frame next-x next-v))))))]
      (do-frame from 0))))

#_(enable-console-print!)

#_(prn
    (run
      0
      100
      (fn [& args]
        (prn args))))
