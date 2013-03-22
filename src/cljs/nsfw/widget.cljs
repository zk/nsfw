(ns nsfw.widget
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]
            [clojure.string :as str]))

(defn data [t & name-val-pairs]
  (->> name-val-pairs
       (partition 2)
       (map (fn [[key val]]
              [key (atom val)]))
       (into {})
       (merge t)))

(defn render [{:keys [$el] :as t} sel f]
  (f (dom/$ $el sel))
  t)

(defn bind [t data-key f]
  (bind/update (:$el t) (data-key t) f)
  t)

(defn parse-sel-ev [sel-ev]
  (let [event (->> sel-ev
                   name
                   reverse
                   (take-while #(not= "." %))
                   reverse
                   (apply str))
        sel (->> sel-ev
                 name
                 reverse
                 (drop-while #(not= "." %))
                 (drop 1)
                 reverse
                 (apply str))]
    [sel event]))

(defn event [t & rest]
  (let [$el (:$el t)
        events (butlast rest)
        f (last rest)
        sel-evs (map parse-sel-ev events)]
    (doseq [[sel ev] sel-evs]
      (let [$el (if (empty? sel)
                  $el
                  (dom/$ $el sel))]
        (dom/listen $el ev (fn [e] (f $el e t))))))
  t)

(defn html [t struct]
  (assoc t :$el (dom/$ struct)))

(def new {})
