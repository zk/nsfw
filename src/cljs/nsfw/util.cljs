(ns nsfw.util
  (:require #_[cljs-uuid-utils :as uu]
            [cljs.reader :as reader]
            [clojure.string :as str]))

(defn log [& args]
  (.log js/console (if args (to-array args) nil)))

(defn log-pass [res]
  (log res)
  res)

(defn lpr [& args]
  (.log js/console (to-array (map pr-str args))))

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

(defn clear-timeout [timeout]
  (js/clearTimeout timeout))

(defn uuid []
  #_(cljs-uuid-utils/make-random-uuid)
  (gensym))

(defn page-data [key]
  (reader/read-string (aget js/window (str/replace (name key) #"-" "_"))))
