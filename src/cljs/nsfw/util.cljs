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
  (try
    (reader/read-string (aget js/window (str/replace (name key) #"-" "_")))
    (catch js/Error e
      (throw (str "Couldn't find page data " key)))))

(defn run-once [f]
  (let [did-run (atom false)]
    (fn [& args]
      (when-not @did-run
        (reset! did-run true)
        (apply f args)))))

(defn toggle [f0 f1]
  (let [!a (atom false)]
    (fn [& args]
      (let [res (if-not @!a (apply f0 args) (apply f1 args))]
        (swap! !a not)
        res))))