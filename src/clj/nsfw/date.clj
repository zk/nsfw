(ns nsfw.date
  (:require [clojure.core :as cc]
            [nsfw.util :as util])
  (:import [org.joda.time
            DateTime
            DateTimeZone
            DateMidnight
            ReadableDateTime
            Interval
            Days]
           [org.joda.time.format
            DateTimeFormatter
            ISODateTimeFormat
            DateTimeFormat]
           [java.util Date])
  (:refer-clojure :exclude [= > < >= <= pr]))

(def iso-parser (ISODateTimeFormat/dateTimeParser))
(def iso-formatter (.withZone (ISODateTimeFormat/dateTime)
                              DateTimeZone/UTC))

(defn from [o]
  (cond
    (instance? ReadableDateTime o) o
    (cc/= Date (class o)) (DateTime. o)
    (cc/= :now o) (DateTime.)
    (cc/= :yesterday o) (.minusDays (DateTime.) 1)
    (cc/= :tomorrow o) (.plusDays (DateTime.) 1)
    (string? o) (.parseDateTime iso-parser o)
    (number? o) (DateTime. o)
    :else nil))

(defn to-zone [o zone-str]
  (.withZone
    (from o)
    (DateTimeZone/forID zone-str)))

(defn zone-offset [o zone-str]
  (.getOffset (DateTimeZone/forID zone-str) o))

(defn default-zone-offset [o]
  (.getOffset (DateTimeZone/getDefault) o))

(defn midnight [o]
  (DateMidnight. (from o)))

(defn start-of-day-ms [o]
  (.getMillis (.withTimeAtStartOfDay (from o))))

(defn to-iso [dt]
  (cond
   (cc/= nil dt) nil
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
  (.print
    (DateTimeFormat/forPattern pattern)
    (let [res (from date)]
      res)))

(defn in-day? [ts delta & [tz-id]]
  (let [date (if tz-id
               (org.joda.time.DateTime.
                 (DateTime/now)
                 (org.joda.time.DateTimeZone/forID
                   tz-id))
               (org.joda.time.DateTime. ts))

        date (.plusDays date (int delta))

        target (if tz-id
              (org.joda.time.DateTime.
                ts
                (org.joda.time.DateTimeZone/forID
                  tz-id))
              (org.joda.time.DateTime. ts))

        interval (Interval.
                   (.withTimeAtStartOfDay date)
                   Days/ONE)]
    (.contains
      interval
      target)))

(defn today? [ts & [tz-id]]
  (in-day? ts 0 tz-id))

(defn yesterday? [ts & [tz-id]]
  (in-day? ts -1 tz-id))

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
