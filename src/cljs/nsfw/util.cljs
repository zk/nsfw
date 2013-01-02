(ns nsfw.util
  (:require #_[cljs-uuid-utils :as uu]
            [cljs.reader :as reader]))

(defn log [& args]
  (.log js/console (if args (.-a args) nil)))

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

(defn to-json [data]
  (.stringify js/JSON (clj->js data)))

(defn timeout [f delta]
  (js/setTimeout f delta))

(defn interval [f delta]
  (js/setInterval f delta))

(defn uuid []
  #_(cljs-uuid-utils/make-random-uuid)
  (gensym))

(defn page-data [key]
  (reader/read-string (aget js/window (name key))))
