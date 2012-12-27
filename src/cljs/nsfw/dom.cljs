(ns nsfw.dom
  (:use [nsfw.util :only [log clj->js]])
  (:require [crate.core :as crate]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.events :as events]
            [goog.dom.query]
            [cljs.reader :as reader]))

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

(defn click [els f]
  (doseq [el (ensure-coll els)]
    (events/listen el "click" f))
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

(defn match-key [els key f]
  (doseq [el (ensure-coll els)]
    (keyup el (fn [e]
                (let [kc (.-keyCode e)]
                  (when (= key kc)
                    (f el (val el)))))))
  els)

(defn text [els text]
  (doseq [el (ensure-coll els)]
    (dom/setTextContent el text))
  els)

(defn val-changed [els f]
  (doseq [el (ensure-coll els)]
    (keyup el
           (fn [e]
             (f el (val el)))))
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


;; Binding

(defn map->jsobj
  "makes a javascript map from a clojure one"
  [cljmap]
  (let [out (js-obj)]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn ajax [opts]
  (let [{:keys [path method data headers success error]}
        (merge
         {:path "/"
          :method "GET"
          :data {}
          :headers {"content-type" "application/clojure"}
          :success (fn [])
          :error (fn [])}
         opts)]
    (goog.net.XhrIo/send
     path
     (fn [e]
       (try
         (let [req (.-target e)]
           (if (.isSuccess req)
             ;; maybe pull js->clj
             (success (let [resp (.getResponseText req)]
                        (when-not (empty? resp)
                          (reader/read-string ))))
             (error req)))
         (catch js/Object e
           (.error js/console (.-stack e))
           (throw e))))
     method
     data
     (map->jsobj headers))))

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

(defn bind-el [atom el f]
  (bind atom (fn [ident from to]
               (f from to el))))

(defn bind-update [el atom f]
  (bind atom (fn [id old new] (f el new)))
  el)

(defn bind-render [el atom f]
  (bind atom (fn [id old new]
               (-> el
                   empty
                   (append (f new)))))
  (append el (f @atom))
  el)
