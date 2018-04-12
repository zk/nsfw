(ns nsfw.annie
  (:require
   [nsfw.util :as util]
   [cljs.core.async :as async
    :refer [<! >! chan close! sliding-buffer put! take!
            alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def !animations (atom {}))

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

(defn -run [continue? from to callback & [spring-params]]
  (let [k (or (:k spring-params) 118)
        b (or (:b spring-params) 18)]
    (letfn [(do-frame [x v]
              (if (and (continue?) (not= x to))
                (let [[next-x next-v] (step (/ ms-per-frame 1000) x v to k b)]
                  (callback next-x)
                  (raf (fn []
                         (do-frame next-x next-v))))))]
      (do-frame from 0))))

(defn run [!anims from to callback]
  (let [!run-anim? (atom true)
        keep-running? (fn [] @!run-anim?)
        stop-anim (fn []
                    (reset! !run-anim? false))]
    (swap! !anims assoc (gensym) stop-anim)
    (-run
      keep-running?
      from
      to
      callback)))

(defn spring [!anims from to callback & [spring-params]]
  (let [!run-anim? (atom true)
        keep-running? (fn [] @!run-anim?)
        stop-anim (fn []
                    (reset! !run-anim? false))]
    (swap! !anims assoc (gensym) stop-anim)
    (-run
      keep-running?
      from
      to
      callback
      spring-params)))

(defn stop-all [!anims]
  (doseq [[k v] @!anims]
    (v))
  (reset! !anims {}))
