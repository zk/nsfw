(ns nsfw.date
  (:require [clojure.core :as cc])
  (:import [org.joda.time
            DateTime
            DateTimeZone]
           [org.joda.time.format
            DateTimeFormatter
            ISODateTimeFormat])
  (:refer-clojure :exclude [= > < >= <=]))

(def iso-parser (ISODateTimeFormat/dateTimeParser))
(def iso-formatter (.withZone (ISODateTimeFormat/dateTime)
                              DateTimeZone/UTC))

(defn from [s]
  (cond
   (cc/= :now s) (DateTime.)
   (cc/= :yesterday s) (.minusDays (DateTime.) 1)
   (cc/= :tomorrow s) (.plusDays (DateTime.) 1)
   (string? s) (.parseDateTime iso-parser s)
   :else nil))

(defn to-iso [dt]
  (cond
   (cc/= DateTime (class dt)) (.print iso-formatter dt)
   :else (to-iso (from dt))))

(defn = [d0 d1]
  (cc/= 0 (.compareTo d0 d1)))

(defn > [d0 d1]
  (cc/= 1 (.compareTo d0 d1)))

(defn < [d0 d1]
  (cc/= -1 (.compareTo d0 d1)))

(defn >= [d0 d1]
  (or (= d0 d1)
      (> d0 d1)))

(defn <= [d0 d1]
  (or (= d0 d1)
      (< d0 d1)))

(defn in-range [low high]
  (fn [date]
    (and (>= date low)
         (< date high))))
