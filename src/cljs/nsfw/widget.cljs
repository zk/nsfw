(ns nsfw.widget
  (:require [nsfw.dom :as $]
            [nsfw.bind :as bind]
            [nsfw.util :as util]
            [clojure.string :as str]))

(defn html
  ([m struct]
     (assoc m :init
            (fn [opts]
              ($/node struct))))
  ([m atom struct]
     (assoc m :init
            (fn [opts]
              (bind/render2 atom struct)))))

(defn el
  [m q]
  (assoc m :init (fn [opts]
                   (first ($/query q)))))

(defn event
  [m sel-ev transform]
  (let [events (:events m)
        [sel ev] ($/parse-sel-ev sel-ev)
        events (concat events [{:selector sel
                                :event ev
                                :transform transform}])]
    (assoc m :events events)))

(defn handle
  ([m msg handler]
     (let [handlers (:msg-handlers m)]
       (assoc m :msg-handlers (concat handlers [{:msg-type msg
                                                 :action handler}]))))
  ([m handler] (handle m nil handler)))

(defn state
  [m atom]
  (assoc m :!state atom))

(defn bind
  ([m query-fn handler]
     (let [bindings (:data-bindings m)]
       (assoc m :data-bindings (concat bindings [{:query-fn query-fn
                                                  :handler handler}]))))
  ([m handler]
     (bind m identity handler)))
