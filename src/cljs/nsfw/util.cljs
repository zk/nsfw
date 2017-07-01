(ns nsfw.util
  (:require #_[cljs-uuid-utils :as uu]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [nsfw.crypt :as crypt]
            [goog.date :as gd]
            [goog.i18n.DateTimeFormat]
            [cognitect.transit :as t]
            [goog.string :as gstring]
            [camel-snake-kebab.core :as csk]
            [cljs.pprint :as pprint])
  (:import [goog.string StringBuffer])
  (:refer-clojure :exclude [uuid]))

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

(defn timeout [f delta]
  (js/setTimeout f delta))

(defn interval [f delta]
  (js/setInterval f delta))

(defn clear-timeout [timeout]
  (js/clearTimeout timeout))

(defn ms [date]
  (.getTime date))

(defn now []
  (.now js/Date))

(defn ref? [o]
  (instance? cljs.core/Atom o))

(def md5 crypt/md5)

(defn grav-url-for [email & [_ size]]
  (let [email (->> (or email "")
                   (str/trim)
                   (str/lower-case))
        url (str "https://gravatar.com/avatar/" (md5 email) "?d=identicon")]
    (if size
      (str url "&s=" size)
      url)))

(defn parse-iso-8601 [iso-str]
  (gd/fromIsoString iso-str))

(defn to-iso-8601 [unix-time]
  (.toUTCIsoString (doto (gd/DateTime.)
                     (.setTime unix-time))
                   false true))

(defn format-date
  ([pattern date]
   (let [date (if (number? date)
                (.fromTimestamp gd/DateTime date)
                date)]
     (.format (goog.i18n.DateTimeFormat. pattern) date))))

(defn navigate-to [& parts]
  (aset (aget js/window "location") "href" (apply str parts)))

(def reader (t/reader :json))
(def writer (t/writer :json))

(defn to-transit [o]
  (t/write writer o))

(defn from-transit [s]
  (t/read reader s))

(defn to-json [o]
  (.stringify js/JSON o))

(defn from-json [o]
  (js->clj (.parse js/JSON o) :keywordize-keys true))

(defn ellipsis [n s]
  (when s
    (let [len (count s)]
      (if (> len n)
        (str (->> s
                  (take n)
                  (apply str))
             "...")
        s))))



(defn format-phone [phone]
  (when phone
    (let [phone (str/replace phone #"[^\d]" "")
          pc (count phone)]
      (str
        (when (> pc 3)
          "(")
        (->> phone (take 3) (apply str))
        (when (> (count phone) 3)
          (str
            ") "
            (->> phone (drop 3) (take 3) (apply str))))
        (when (> (count phone) 6)
          (str
            "-"
            (->> phone (drop 6) (apply str))))))))


(defn uuid []
  (let [d (now)
        uuid-str "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"]
    (str/replace uuid-str
      #"[xy]"
      (fn [c]
        (let [r (bit-or (mod (+ d (* (.random js/Math) 16)) 16) 0)
              d (.floor js/Math (/ d 16.0))]
          (str/replace
            (.toString
              (if (= "x" c)
                r
                (bit-or
                  (bit-and 0x3 r)
                  0x8))
              16)
            "-" ""))))))

(defn initials [s]
  (when s
    (let [ps (-> s
                 str/trim
                 (str/split #"\s+"))]
      (str/upper-case
        (str
          (first (first ps))
          (when (> (count ps) 1)
            (first (last ps))))))))

(defn sformat [& args]
  (apply gstring/format args))

(defn time-delta-desc [delta]
  (when delta
    (let [ms delta
          s (/ ms 1000)
          m (/ s 60)
          h (/ m 60)
          d (/ h 24)
          y (/ d 365.0)]
      (cond
        (< s 60) "less than a minute"
        (< m 2) "1 minute"
        (< h 1) (str (int m) " minutes")
        (< h 2) "1 hour"
        (< d 1) (str (int h) " hours")
        (< d 2) "1 day"
        (< y 1) (str (int d) " days")
        :else (str (sformat "%.1f" y) " years")))))

(defn timeago [millis]
  (when millis
    (time-delta-desc (- (now) millis))))

(defn timebefore [millis]
  (when millis
    (time-delta-desc (- millis (now)))))

(defn exact-timeago [millis]
  (when millis
    (let [ms (- (now) millis)
          s (/ ms 1000)
          m (/ s 60)
          h (/ m 60)
          d (/ h 24)
          y (/ d 365.0)]
      (cond
        (< s 60) (str (int s) "s")
        (< m 2) "1m"
        (< h 1) (str (int m) "m")
        (< h 2) "1h"
        (< d 1) (str (int h) "h")
        (< d 2) "1d"
        (< y 1) (str (int d) "d")
        :else (str (sformat "%.1f" y) "y")))))

(defn time-delta-exact [millis]
  (when millis
    (let [ms millis
          s (/ ms 1000)
          m (/ s 60)
          h (/ m 60)
          d (/ h 24)
          y (/ d 365.0)]
      (str
        (int h) "h"))))

(defn transform-keys [o transform-fn]
  (cond
    (map? o)
    (->> o
         (map (fn [[k v]]
                [(transform-fn k)
                 (transform-keys v transform-fn)]))
         (into {}))

    (coll? o)
    (->> o
         (map #(transform-keys % transform-fn))
         ((fn [out]
            (if (vector? o)
              (vec out)
              out)))
         doall)
    :else o))

(defn kebab-case [o]
  (csk/->kebab-case o))

(defn kebab-coll [m]
  (transform-keys
    m
    kebab-case))

(defn env-case [o]
  (csk/->SCREAMING_SNAKE_CASE o))

(defn camel-case [o]
  (csk/->camelCase o))

(defn camel-coll [o]
  (transform-keys
    o
    camel-case))

(defn snake-case [o]
  (if (keyword? o)
    (keyword
      (namespace o)
      (csk/->snake_case
        (name o)))
    (csk/->snake_case o)))

(defn snake-coll [o]
  (transform-keys
    o
    snake-case))

(defn page-data [key & [default]]
  (try
    (from-transit
      (aget js/window (env-case (name key))))
    (catch js/Error e
      (if default
        default
        (throw (js/Error. (str "Couldn't find page data " key)))))))

(defn pp-str [o]
  (let [sb (StringBuffer.)
        sbw (StringBufferWriter. sb)]
    (pprint/pprint o sbw)
    (str sb)))

(defn pp [o]
  (println (pp-str o)))

(defn pluralize [n singular plural]
  (if (= 1 n)
    singular
    plural))

(defn lookup-map [coll key]
  (->> coll
       (map (fn [o]
              [(get o key)
               o]))
       (into {})))

(defn url-encode [s]
  (js/encodeURIComponent s))

(defn abs [n]
  (.abs js/Math n))

(defn spy [o]
  (pp o)
  o)

(defn round [n]
  (when n
    (.round js/Math n)))

(defn url-slug [s]
  (when s
    (-> s
        str/trim
        str/lower-case
        (str/replace #"\s+" "-")
        (str/replace #"[^a-zA-Z0-9_-]+" "-")
        (str/replace #"-+" "-"))))
