(ns nsfw.util
  #? (:clj
      (:require [clojure.string :as str]
                [cheshire.custom :as json]
                [clojure.pprint :as pprint]
                [clojure.java.io :as io]
                [hashids.core :as hashids]
                [byte-transforms :as bt]
                [byte-streams :as bs]
                [camel-snake-kebab.core :as csk]
                [cognitect.transit :as transit]
                [camel-snake-kebab.core :as csk])
      :cljs
      (:require [clojure.string :as str]
                [nsfw.crypt :as crypt]
                [cognitect.transit :as transit]
                [goog.date :as gd]
                [goog.i18n.DateTimeFormat]
                [cljs.pprint :as pprint]
                [goog.string :as gstring]
                [goog.string.format]
                [camel-snake-kebab.core :as csk]))

  #? (:clj
      (:import [java.util Date]
               [java.text SimpleDateFormat]
               [java.net URLEncoder]
               [org.pegdown PegDownProcessor Extensions]
               [org.joda.time.format ISODateTimeFormat]))
  #? (:cljs
      (:import [goog.string StringBuffer]))

  (:refer-clojure :exclude [uuid]))


(defn now []
  #? (:clj
      (System/currentTimeMillis)
      :cljs
      (.now js/Date)))

#?
(:clj
 (do
   (def iso-formatter (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
   (def iso-parser (ISODateTimeFormat/dateTimeParser))))

(defn to-iso8601 [o]
  (cond
    (number? o) #? (:clj
                    (.format iso-formatter (Date. o))
                    :cljs
                    (.toUTCIsoString
                      (doto (gd/DateTime.)
                        (.setTime o))
                      false true))

    #? (:clj (= Date (class o))) #? (:clj (.format iso-formatter o))
    #? (:cljs (= js/Date (type o))) #? (:cljs
                                         (.toUTCIsoString
                                           (gd/DateTime. o)
                                           false true))

    (= nil o) nil
    (= "" o) nil
    :else nil))

(defn from-iso8601 [o]
  (cond
    (string? o)
    #? (:clj
        (.getMillis (.parseDateTime iso-parser o))
        :cljs
        (.getTime (gd/fromIsoString o)))
    :else o))


#? (:clj
    (defn uuid []
      (-> (java.util.UUID/randomUUID)
          str
          (str/replace #"-" ""))))

#? (:cljs
    (defn uuid []
      (let [d (now)
            uuid-str "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx"]
        (str/replace uuid-str
          #"[xy]"
          (fn [c]
            (let [r (bit-or (mod (+ d (* (.random js/Math) 16)) 16) 0)
                  d (.floor js/Math (/ d 16.0))]
              (.toString
                (if (= "x" c)
                  r
                  (bit-or
                    (bit-and 0x3 r)
                    0x8))
                16)
              ))))))

(defn round [n]
  #? (:clj
      (if (float? n)
        (Math/round n)
        n)
      :cljs
      (.round js/Math n)))

(defn abs [n]
  #? (:clj
      (Math/abs n)
      :cljs
      (.abs js/Math n)))

(defn ellipsis [n s]
  (when s
    (let [len (count s)]
      (if (> len n)
        (str (->> s
                  (take n)
                  (apply str))
             "...")
        s))))

(defn ellipsis-center
  "Ellipses the middle of a long string."
  [n s]
  (let [n (max (- n 3) 0)]
    (cond
      (<= (count s) n) s
      :else (let [len (count s)
                  half-len (round (/ len 2.0))
                  to-take-out (- len n)
                  half-take-out (round (/ to-take-out 2.0))
                  first-half (take half-len s)
                  second-half (drop half-len s)]
              (str (->> first-half
                        (take (- half-len half-take-out))
                        (apply str))
                   "..."
                   (->> second-half
                        (drop half-take-out)
                        (apply str)))))))

(defn ellipsis-left [n s]
  (when s
    (let [len (count s)]
      (if (> len n)
        (str "..."
             (->> s
                  reverse
                  (take n)
                  reverse
                  (apply str)))
        s))))

(defn sformat [pattern s]
  #? (:clj
      (format pattern s)
      :cljs
      (gstring/format pattern s)))

(defn date-format [o pattern]
  (cond
    (number? o)
    #? (:clj
        (.format (SimpleDateFormat. pattern) (Date. o))
        :cljs
        (.format (goog.i18n.DateTimeFormat. pattern)
          (.fromTimestamp gd/DateTime o)))

    #? (:clj (= Date (class o))) #? (:clj (.format (SimpleDateFormat. pattern) o))
    #? (:cljs (= js/Date (type o))) #? (:cljs (.format (goog.i18n.DateTimeFormat. pattern) o))

    :else nil))

(defn throw-str [& args]
  #? (:clj
      (throw (Exception. (apply str args)))
      :cljs
      (throw (js/Error. (apply str args)))))



#? (:clj
    (do
      (def secure-random-obj (java.security.SecureRandom.))

      (defn secure-random-str
        "Generates a random string of bytes in hex"
        [n]
        (let [buf (byte-array n)]
          (.nextBytes secure-random-obj buf)
          (->> buf
               (map #(Integer/toHexString (bit-and % 0xff)))
               (apply str))))))


#? (:clj
    (defn md5
      "Compute the hex MD5 sum of a string."
      [o & [opts]]
      (when o
        (let [md5-str (.toString
                        (new BigInteger 1
                          (bt/hash o :md5 opts))
                        16)
              pad (apply str (repeat (- 32 (count md5-str)) "0"))]
          (str pad md5-str))))
    :cljs
    (def md5 crypt/md5))



#? (:clj (json/add-encoder org.bson.types.ObjectId json/encode-str))

 (defn to-json [o & [opts-or-replacer space]]
   #? (:clj
       (json/generate-string o opts-or-replacer)
       :cljs
       (.stringify js/JSON (clj->js o) opts-or-replacer space)))

(defn from-json [o & [prevent-keywordize]]
  #? (:clj
      (let [keywordize? (not prevent-keywordize)]
        (if (string? o)
          (json/parse-string o keywordize?)
          (json/parse-stream (io/reader o) keywordize?)))
      :cljs
      (js->clj (.parse js/JSON o) :keywordize-keys
        (not prevent-keywordize))))


#? (:clj
    (defn to-transit [o & [opts]]
      (let [bs (java.io.ByteArrayOutputStream.)]
        (transit/write
          (transit/writer bs :json opts)
          o)
        (.toString bs)))
    :cljs
    (defn to-transit [o & [opts]]
      (transit/write
        (transit/writer :json opts)
        o)))

#? (:clj
    (defn from-transit [s & [opts]]
      (when s
        (transit/read
          (transit/reader
            (if (string? s)
              (java.io.ByteArrayInputStream. (.getBytes s "UTF-8"))
              s)
            :json
            opts))))
    :cljs
    (defn from-transit [s & [opts]]
      (transit/read
        (transit/reader :json opts)
        s)))

(defn url-encode [s]
  (when s
    #? (:clj
        (java.net.URLEncoder/encode s)
        :cljs
        (js/encodeURIComponent s))))

(defn url-decode [s & [{:keys [encoding]}]]
  (when s
    #? (:clj
        (java.net.URLDecoder/decode s
          (or encoding
              "UTF-8"))
        :cljs
        (js/decodeURI s))))

(defn distinct-by
  [key coll]
  (let [step (fn step [xs seen]
               (lazy-seq
                 ((fn [[f :as xs] seen]
                    (when-let [s (seq xs)]
                      (if (contains? seen (key f))
                        (recur (rest s) seen)
                        (cons f (step (rest s) (conj seen (key f)))))))
                  xs seen)))]
    (step coll #{})))


(defn parse-long [s & [default]]
  (if s
    #? (:clj
        (try
          (Integer/parseInt s)
          (catch Exception e
            default))
        :cljs
        (let [res (js/parseInt s)]
          (if (js/isNaN res)
            default
            res)))
    default))

(defn parse-long-base [s base & [default]]
  (if s
    #? (:clj
        (try
          (Long/parseLong s base)
          (catch Exception e
            default))
        :cljs
        (let [res (js/parseInt s base)]
          (if (js/isNaN res)
            default
            res)))
    default))

(defn parse-double [s & [default]]
  (if s
    #? (:clj
        (try
          (Double/parseDouble s)
          (catch Exception e
            default))
        :cljs
        (let [res (js/parseFloat s)]
          (if (js/isNaN res)
            default
            res)))
    default))

(defn pp-str [o]
  #? (:clj
      (let [w (java.io.StringWriter.)]
        (pprint/pprint o w)
        (.toString w))
      :cljs
      (let [sb (StringBuffer.)
            sbw (StringBufferWriter. sb)]
        (pprint/pprint o sbw)
        (str sb))))

(defn pp [o]
  #? (:clj
      (pprint/pprint o)
      :cljs
      (println (pp-str o))))


#? (:clj
    (defn ms [date]
      (cond
        (= java.util.Date (class date)) (.getTime date)
        (= org.joda.time.DateTime (class date))  (.getMillis date)))
    :cljs
    (defn ms [date]
      (.getTime date)))

(defn time-delta-parts [delta]
  (when delta
    (let [ms delta
          s (/ ms 1000)
          m (/ (int s) 60)
          h (/ (int m) 60)
          d (/ (int h) 24)
          y (/ (int d) 365.0)]
      {:ms (int (mod ms 1000))
       :d (int (mod d 365))
       :h (int (mod h 24))
       :m (int (mod m 60))
       :s (int (mod s 60))})))

(defn time-delta-desc [delta]
  (when delta
    (let [{:keys [ms s m h d y]} (time-delta-parts delta)]
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

(defn kebab-val [o]
  (csk/->kebab-case o))

(defn kebab-coll [m]
  (transform-keys
    m
    kebab-val))

(defn kebab-case [o]
  (if (coll? o)
    (kebab-coll o)
    (kebab-val o)))

(defn env-val [o]
  (csk/->SCREAMING_SNAKE_CASE o))

(defn env-case [o]
  (env-val o))

(defn camel-val [o]
  (csk/->camelCase o))

(defn camel-coll [o]
  (transform-keys
    o
    camel-val))

(defn camel-case [o]
  (if (coll? o)
    (camel-coll o)
    (camel-val o)))

(defn snake-val [o]
  (if (keyword? o)
    (keyword
      (namespace o)
      (csk/->snake_case
        (name o)))
    (csk/->snake_case o)))

(defn snake-coll [o]
  (transform-keys
    o
    snake-val))

(defn snake-case [o]
  (if (coll? o)
    (snake-coll o)
    (snake-val o)))

(defn spy [& os]
  (let [msg (->> os
                 butlast
                 (interpose " ")
                 (apply str))]
    (when-not (empty? msg)
      (println msg))
    (pp (last os))
    (last os)))


#? (:clj
    (do
      (defn to-short-id [id salt-str]
        (hashids/encrypt id salt-str))

      (defn from-short-id [short-id salt-str]
        (hashids/decrypt short-id salt-str))))


#? (:clj
    (do
      (defn to-base64-str [s & [o]]
        (when s
          (bs/to-string
            (bt/encode
              s
              :base64
              (or o
                  {:url-safe? false})))))

      (defn from-base64-str [s & [o]]
        (when s
          (bs/to-string (bt/decode s :base64 o))))

      (defn sha256-bytes [data & [o]]
        (when data
          (bt/hash data :sha256 o)))

      (defn sha256 [s & [o]]
        (bs/to-string (sha256-bytes s o)))

      (defn sha512-bytes [data & [o]]
        (when data
          (bt/hash data :sha512 o)))

      (defn sha512 [s & [o]]
        (bs/to-string (sha512-bytes s o)))

      (defn hex-str->byte-array [s]
        (.toByteArray (BigInteger. s 16)))))


(defn pluralize [n singular plural]
  (if (= 1 n)
    singular
    plural))


#? (:cljs
    (do
      (defn timeout [f delta]
        (js/setTimeout f delta))

      (defn interval [f delta]
        (js/setInterval f delta))

      (defn clear-timeout [timeout]
        (js/clearTimeout timeout))))

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

(defn lookup-map [key coll]
  (->> coll
       (map (fn [o]
              [(get o key)
               o]))
       (into {})))

(defn url-slug [s]
  (when s
    (-> s
        str/trim
        str/lower-case
        (str/replace #"\s+" "-")
        (str/replace #"[^a-zA-Z0-9_-]+" "-")
        (str/replace #"-+" "-"))))

(defn pad [s n]
  (sformat (str "%0" n "d") s))


(defn cos [theta]
  #?(:clj (Math/cos theta)
     :cljs (.cos js/Math theta)))

(defn sin [theta]
  #?(:clj (Math/sin theta)
     :cljs (.sin js/Math theta)))

(defn sqrt [n]
  #?(:clj (Math/sqrt n)
     :cljs (.sqrt js/Math n)))

(defn sq [n] (* n n))

(def PI
  #?(:clj Math/PI
     :cljs (.-PI js/Math)))

#?(:clj
   (defmacro file-contents [path]
     (let [fc# (slurp path)]
       `~fc#)))
