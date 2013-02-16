(ns nsfw.dom
  (:use [nsfw.util :only [log]])
  (:require [dommy.template :as template]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.style :as style]
            [goog.events :as events]
            [goog.dom.query]
            [cljs.core :as cc]
            [nsfw.util :as util])
  (:refer-clojure :exclude [val replace remove empty drop]))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ICollection
  (-conj [coll o]
    (throw "Error: Can't conj onto a NodeList.")))

(defn ge->map
  "Turns a google closure event into a map.
   See http://goo.gl/87L84 for more info."
  [e]
  {:type (.-type e)
   :timestamp (.-timestamp e)
   :target (.-target e)
   :current-target (.-currentTarget e)
   :related-target (.-relatedTarget e)
   :offset-x (.-offsetX e)
   :offset-y (.-offsetY e)
   :client-x (.-clientX e)
   :client-y (.-clientY e)
   :screen-x (.-screenX e)
   :screen-y (.-screenY e)
   :button (.-button e)
   :key-code (.-keyCode e)
   :ctrl-key (.-ctrlKey e)
   :alt-key (.-altKey e)
   :shift-key (.-shiftKey e)
   :meta-key (.-metaKey e)
   :default-prevented (.-defaultPrevented e)
   :state (.-state e)
   :event e})

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

;; Dom

(defn root []
  (aget (dom/getElementsByTagNameAndClass "html") 0))

(defn selector
  ([s]
     (dom/query s))
  ([base s]
     (dom/query s base)))

(defn $
  ([o]
     (cond
      (coll? o) (template/node o)
      (or (keyword? o)
          (string? o)) (selector o)
      :else o))
  ([base o]
     (selector base o)))

(defn val
  ([el]
     (.-value el))
  ([el new-value]
     (set! (.-value el) new-value)))

(defn wrap-content
  [content]
  (cond
   (and (coll? content)
        (keyword? (first content))) ($ content)
   (string? content) ($ content)
   :else content))

(defn append [els content]
  (doseq [el (ensure-coll els)]
    (if (and (coll? content)
             (not (keyword? (first content))))
      (doseq [c content]
        (.appendChild el (wrap-content c))
        (when-let [on-insert (aget content "on-insert")]
          (on-insert el)))
      (do (.appendChild el (wrap-content content))
          (when-let [on-insert (aget content "on-insert")]
            (on-insert el)))))
  els)

(def apd append)

(defn append-to [child parents]
  (doseq [el (ensure-coll parents)]
    (append el child))
  child)

(defn style [els css-map]
  (let [jsobj (clj->js css-map)]
    (doseq [el (ensure-coll els)]
      (style/setStyle el jsobj)))
  els)

(defn size [el]
  (style/getSize el))

(defn attrs
  [els m]
  (doseq [el (ensure-coll els)]
    (doseq [key (keys m)]
      (.setAttribute el (name key) (get m key))))
  els)

(defn text [els text]
  (doseq [el (ensure-coll els)]
    (dom/setTextContent el (str text)))
  els)

(defn replace [els content]
  (doseq [el (ensure-coll els)]
    (dom/replaceNode content el))
  els)

(defn remove [els]
  (doseq [el (ensure-coll els)]
    (dom/removeNode el)))

(defn empty [els]
  (doseq [el (ensure-coll els)]
    (dom/removeChildren el))
  els)

(defn has-class? [el cls]
  (classes/has el (name cls)))

(defn add-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/add el (name cls)))
  els)

(defn rem-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/remove el (name cls)))
  els)

(def body ($ "body"))

;; Events

(defn prevent [e]
  (.preventDefault (:event e)))

(defn stop-prop [e]
  (.stopPropagation (:event e)))

(defn onload [f]
  (set! (.-onload js/window) f))

(defn listen [els evt f]
  (doseq [el (ensure-coll els)]
    (events/listen el (name evt) #(f (ge->map %) el)))
  els)

(defn handler [evt]
  (fn [els f]
    (listen els (name evt) f)))

(def click (handler :click))
(def dblclick (handler :dblclick))

(def mousedown (handler :mousedown))
(def mouseup (handler :mouseup))
(def mouseover (handler :mouseover))
(def mouseout (handler :mouseout))
(def mousemove (handler :mousemove))
(def selectstart (handler :selectstart))

(def keypress (handler :keypress))
(def keydown (handler :keydown))
(def keyup (handler :keyup))
(def blur (handler :blur))

(def change (handler :change))
(def select (handler :select))
(def submit (handler :submit))
(def input (handler :input))

(def dragstart (handler :dragstart))
(def dragenter (handler :dragenter))
(def dragover (handler :dragover))
(def dragleave (handler :dragleave))
(def drop (handler :drop))

(def touchstart (handler :touchstart))
(def touchmove (handler :touchmove))
(def touchend (handler :touchend))
(def touchcancel (handler :touchcancel))

(def contextmenu (handler :contextmenu))
(def error (handler :error))
(def help (handler :help))
(def load (handler :load))
(def losecapture (handler :losecapture))
(def readstatechange (handler :readstatechange))
(def resize (handler :resize))
(def scroll (handler :scroll))
(def unload (handler :unload))

(def hashchange (handler :hashchange))
(def pagehide (handler :pagehide))
(def pageshow (handler :pageshow))
(def popstate (handler :popstate))

(defn focus
  ([el]
     (.focus el)
     el)
  ([els f]
     (listen els :focus f)))

(defn select [el]
  (.select el))

(defn match-key [els key f]
  (doseq [el (ensure-coll els)]
    (keyup el (fn [e]
                (let [kc (.-keyCode e)]
                  (when (= key kc)
                    (f el (val el)))))))
  els)

(defn val-changed
  ([els f]
     (doseq [el (ensure-coll els)]
       (let [f (fn [e]
                 (f el (val el)))]  ; el, then val - side effects only
         (keyup el f)
         (change el f)))
     els)
  ([base sel f]
     (let [$el ($ base)
           $target ($ $el sel)]
       (val-changed $target f)
       $el)))

(defn on-enter [els f]
  (doseq [el (ensure-coll els)]
    (keydown el (fn [e]
                  (when (= 13 (:key-code e))
                    (prevent e)
                    (stop-prop e)
                    (f e el)))))
  els)

(defn scroll-end [els f]
  (doseq [el (ensure-coll els)]
    (let [timer (atom -1)]
      (listen js/window "scroll"
              (fn [e]
                (when (> @timer -1)
                  (util/clear-timeout @timer))
                (reset! timer (util/timeout #(f e) 150))))))
  els)

(defn on-insert [els f]
  (doseq [el (ensure-coll els)]
    (aset el "on-insert" f)))

(defn viewport-size []
  (let [vp (dom/getViewportSize)]
    [(.-width vp)
     (.-height vp)]))

(defn bounds [el]
  (let [b (style/getBounds el)]
    {:width (.-width b)
     :height (.-height b)
     :left (.-left b)
     :top (.-top b)}))

(defn scroll-to
  [el]
  (.scrollIntoView el true))