(ns nsfw.route
  (:require [nsfw.util :as util]
            [nsfw.dom :as dom]
            [clojure.string :as str]))

(def route-info (atom {}))

(defn location-hash [] (.-hash (.-location js/window)))

(defn route->pattern
  "Converts a route like /:foo/:bar into a matching pattern like
  /(.*)/(.*)"
  [route]
  (let [var-matches (map second (re-seq #":([^\s/]+)" route))
        keywords (->> var-matches
                      (map keyword))
        route-re (str/replace route
                              (->> var-matches
                                   (map #(str ":" %))
                                   (interpose "|")
                                   (apply str)
                                   re-pattern)
                              "(.*)")]
    {:pattern (re-pattern route-re)
     :keys keywords}))

(defn exec-routes [parts]
  (let [pairs (partition 2 parts)
        clean-hash (->> (location-hash) (drop 1) (apply str))
        default (when (-> pairs last first keyword?)
                  (-> pairs last second))
        matched-fn (if (= "/" (first clean-hash))
                     (loop [pairs pairs]
                       (when-not (empty? pairs)
                         (let [pair (first pairs)
                               {:keys [pattern keys]} (route->pattern (first pair))]
                           (if (re-matches pattern clean-hash)
                             (let [args (drop 1 (re-matches pattern clean-hash))]
                               #(apply (second pair) args))
                             (recur (rest pairs))))))
                     (if (= :else (first (last pairs)))
                       ((second (last pairs)))))]
    ((or matched-fn default))))

(defn url-hash
  [& parts]
  (let [f #(exec-routes parts)]
    (dom/listen js/window "hashchange" (fn [e] (f)))
    (f)))
