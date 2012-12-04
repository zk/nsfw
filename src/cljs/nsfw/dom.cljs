(ns nsfw.dom
  (:require [domina :as d]
            [domina.css :as dc]))

(def $ dc/sel)

(def append! d/append!)
(def prepend! d/prepend!)
(def add-class! d/add-class!)
(def delete! d/delete!)
(def style! d/set-styles!)
(def text! d/set-text!)
(def listen! d/listen!)
(def capture! d/capture!)

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
