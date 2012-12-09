(ns nsfw.dom
  (:use [nsfw.util :only [log clj->js]])
  (:require [crate.core :as crate]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.dom.query]))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ICollection
  (-conj [coll o]
    (throw "Error: Can't conj onto a NodeList.")))

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

(defn wrap-content [content]
  (cond
   (vector? content) ($ content)
   (string? content) ($ content)
   :else content))

(defn append [els content]
  (let [content (wrap-content content)]
    (doseq [el (ensure-coll els)]
      (.appendChild el content)))
  els)

(defn style [els css-map]
  (let [jsobj (clj->js css-map)]
    (doseq [el (ensure-coll els)]
      (style/setStyle el jsobj)))
  els)

(defn bind [atom function]
  (add-watch
   atom
   (gensym)
   (fn [key identity old-value new-value]
     (function identity old-value new-value))))

(defn bind-change [atom key f]
  (add-watch
   atom
   (gensym)
   (fn [k atom old new]
     (let [ov (get old key)
           nv (get new key)]
       (when (not= ov nv)
         (f atom old new))))))
