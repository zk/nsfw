(ns nsfw.date
  (:require [clojure.core :as cc])
  (:import [org.joda.time
            DateTime
            DateTimeZone]
           [org.joda.time.format
            DateTimeFormatter
            ISODateTimeFormat
            DateTimeFormat])
  (:refer-clojure :exclude [= > < >= <= pr]))

(def iso-parser (ISODateTimeFormat/dateTimeParser))
(def iso-formatter (.withZone (ISODateTimeFormat/dateTime)
                              DateTimeZone/UTC))

(defn from [o]
  (cond
   (cc/= DateTime (class o)) o
   (cc/= :now o) (DateTime.)
   (cc/= :yesterday o) (.minusDays (DateTime.) 1)
   (cc/= :tomorrow o) (.plusDays (DateTime.) 1)
   (string? o) (.parseDateTime iso-parser o)
   :else nil))

(defn to-iso [dt]
  (cond
   (cc/= DateTime (class dt)) (.print iso-formatter dt)
   :else (to-iso (from dt))))

(defn <=> [d0 d1]
  (.compareTo (from d0)
              (from d1)))

(defn = [d0 d1]
  (cc/= 0 (<=> d0 d1)))

(defn > [d0 d1]
  (cc/= 1 (<=> d0 d1)))

(defn < [d0 d1]
  (cc/= -1 (<=> d0 d1)))

(defn >= [d0 d1]
  (or (= d0 d1)
      (> d0 d1)))

(defn <= [d0 d1]
  (or (= d0 d1)
      (< d0 d1)))

(defn in-range [low high]
  (fn [date]
    (when date
      (and (>= date low)
           (< date high)))))

(defn pr [date pattern]
  (.print (DateTimeFormat/forPattern pattern)
          (from date)))

(defn buckets
  "Generates a vector of date-tuples."
  [start period-in-days count]
  (loop [buckets []
         count count
         start start]
    (let [end (.minusDays start period-in-days)]
      (if (clojure.core/= 0 count)
        (reverse buckets)
        (recur (conj buckets [end start])
               (dec count)
               end)))))

(defn into-buckets [ts-key start period-in-days count]
  (let [bs (buckets (from start) period-in-days count)]
    (fn [coll]
      (map (fn [bucket]
             (let [start (first bucket)
                   end (second bucket)
                   in-range? (in-range start end)]
               {:start start
                :end end
                :coll (filter #(in-range? (get % ts-key)) coll)}))
           bs))))