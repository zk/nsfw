(ns nsfw.util)

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings.

   Borrowed and updated from mmcgrana."
  [x]
  (cond
   (string? x) x
   (keyword? x) (name x)
   (map? x) (.-strobj (reduce (fn [m [k v]]
                                (assoc m (clj->js k) (clj->js v))) {} x))
   (coll? x) (apply array (map clj->js x))
   :else x))

(defn log [& args]
  (.log js/console (if args (.-a args) nil)))

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

(defn to-json [data]
  (.stringify js/JSON (clj->js data)))
