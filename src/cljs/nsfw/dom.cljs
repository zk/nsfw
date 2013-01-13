(ns nsfw.dom
  (:use [nsfw.util :only [log]])
  (:require [crate.core :as crate]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.style :as style]
            [goog.events :as events]
            [goog.dom.query]
            [nsfw.util :as util])
  (:refer-clojure :exclude [val replace remove empty]))

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

(defn prevent [e]
  (.preventDefault (:event e)))

(defn stop-prop [e]
  (.stopPropagation (:event e)))

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

(defn root []
  (aget (dom/getElementsByTagNameAndClass "html") 0))

(defn selector [s]
  (dom/query s))

(defn $ [o]
  (cond
   (vector? o) (crate/html o)
   :else (selector o)))

(defn val
  ([el]
     (.-value el))
  ([el new-value]
     (set! (.-value el) new-value)))

(defn wrap-content [content]
  (cond
   (vector? content) ($ content)
   (string? content) ($ content)
   :else content))

(defn append [els content]
  (doseq [el (ensure-coll els)]
    (if (and (coll? content)
             (not (keyword? (first content))))
      (doseq [c content]
        (.appendChild el (wrap-content c)))
      (.appendChild el (wrap-content content))))
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

(defn onload [f]
  (set! (.-onload js/window) f))

(defn listen [els evt f]
  (doseq [el (ensure-coll els)]
    (events/listen el evt f))
  els)

(defn click [els f]
  (doseq [el (ensure-coll els)]
    (events/listen
     el "click" (fn [e]
                  (f (ge->map e) el))))
  els)

(defn mousedown [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "mousedown" f)))

(defn mouseup [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "mouseup" f)))

(defn mousemove [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "mousemove" f)))

(defn mouseover [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "mouseover" f)))

(defn mouseout [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "mouseout" f)))

(defn keydown [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "keydown" f))
  els)

(defn keyup [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "keyup" f))
  els)

(defn change [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "change" f))
  els)

(defn match-key [els key f]
  (doseq [el (ensure-coll els)]
    (keyup el (fn [e]
                (let [kc (.-keyCode e)]
                  (when (= key kc)
                    (f el (val el)))))))
  els)

(defn text [els text]
  (doseq [el (ensure-coll els)]
    (dom/setTextContent el (str text)))
  els)

(defn val-changed [els f]
  (doseq [el (ensure-coll els)]
    (let [f (fn [e]
              (f el (val el)))]
      (keyup el f)
      (change el f)))
  els)

(defn on-enter [els f]
  (doseq [el (ensure-coll els)]
    (keydown el (fn [e]
                  (when (= 13 (.-keyCode e))
                    (.stopPropagation e)
                    (.preventDefault e)
                    (f e)))))
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

(defn focus [el]
  (.focus el)
  el)

(defn replace [els content]
  (doseq [el (ensure-coll els)]
    (dom/replaceNode content el)))

(defn remove [els]
  (doseq [el (ensure-coll els)]
    (dom/removeNode el)))

(defn empty [els]
  (doseq [el (ensure-coll els)]
    (dom/removeChildren el))
  els)

(defn add-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/add el (name cls))))

(defn rem-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/remove el (name cls))))

(def body ($ "body"))
