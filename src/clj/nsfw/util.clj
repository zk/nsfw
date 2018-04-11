(ns nsfw.util
  (:require [clj-stacktrace.repl :as stacktrace]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [cheshire.custom :as json]
            [cognitect.transit :as transit]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [hashids.core :as hashids]
            [byte-transforms :as bt]
            [byte-streams :as bs]
            [camel-snake-kebab.core :as csk])
  (:import [java.util Date]
           [java.text SimpleDateFormat]
           [java.net URLEncoder]
           [org.pegdown PegDownProcessor Extensions]
           [org.joda.time.format ISODateTimeFormat]))

(defn format-ms [ms format]
  (let [d (Date. ms)]
    (.format (SimpleDateFormat. format) d)))

(defn web-stacktrace [e req])

(defn now [] 1)

(defn throw-str [& args]
  (throw (Exception. (apply str args))))

(defn sha1 [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))]
    (->> (.digest (java.security.MessageDigest/getInstance "SHA1") bytes)
         (map #(Integer/toHexString (bit-and % 0xff)))
         (apply str))))

(defn uuid []
  (-> (java.util.UUID/randomUUID)
      (str)
      (str/replace #"-" "")))

;;

(def secure-random-obj (java.security.SecureRandom.))

(defn sec-rand-str
  "Generates a random string of bytes in hex"
  [n]
  (let [buf (byte-array n)]
    (.nextBytes secure-random-obj buf)
    (->> buf
         (map #(Integer/toHexString (bit-and % 0xff)))
         (apply str))))

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

(defn grav-url-for [email & [_ size]]
  (let [email (->> (or email "")
                   (str/trim)
                   (str/lower-case))
        url (str "https://gravatar.com/avatar/" (md5 email) "?d=identicon")]
    (if size
      (str url "&s=" size)
      url)))

;;

(json/add-encoder org.bson.types.ObjectId json/encode-str)

(defn to-json [o]
  (json/generate-string o))

(defn from-json [o & [prevent-keywordize]]
  (let [keywordize? (not prevent-keywordize)]
    (if (string? o)
      (json/parse-string o keywordize?)
      (json/parse-stream (io/reader o) keywordize?))))

;;

(defn from-transit [s & [handlers]]
  (when s
    (transit/read
      (transit/reader
        (if (string? s)
          (java.io.ByteArrayInputStream. (.getBytes s "UTF-8"))
          s)
        :json
        {:handlers handlers}))))

(defn to-transit [o & [handlers]]
  (let [bs (java.io.ByteArrayOutputStream.)]
    (transit/write
      (transit/writer bs :json {:handlers handlers})
      o)
    (.toString bs)))

(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

;;

(defn decode-body [content-length body]
  (when (and content-length
             (> content-length 0))
    (let [buf (byte-array content-length)]
      (.read body buf 0 content-length)
      (.close body)
      (String. buf))))

(defn response-body
  "Turn a HttpInputStream into a string."
  [{:keys [content-length body]}]
  (if (string? body)
    body
    (decode-body content-length body)))

;;

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

(defn parse-int [s & [default]]
  (if s
    (try
      (Integer/parseInt s)
      (catch Exception e
        default))
    default))

(defn markdown
  "Render HTML from markdown"
  [s]
  (let [pgp (PegDownProcessor. Extensions/ALL)]
    (when s
      (.markdownToHtml pgp (str/replace s #"!\[\]" "![ ]")))))
;;

(defn pp [& args]
  (apply pprint args))

(defn pp-str [o]
  (let [w (java.io.StringWriter.)]
    (pprint o w)
    (.toString w)))

(defn stacktrace->str [exc]
  (let [sw (java.io.StringWriter.)
        pw (java.io.PrintWriter. sw)]
    (.printStackTrace exc pw)
    (.toString sw)))

(defn ms [date]
  (cond
    (= java.util.Date (class date)) (.getTime date)
    (= org.joda.time.DateTime (class date))  (.getMillis date)))

(defn timeago [date-or-ms]
  (when date-or-ms
    (let [ms (if (number? date-or-ms)
               (- (now) date-or-ms)
               (- (now) (ms date-or-ms)))
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
        (< y 2) "1 year"
        :else (str (format "%.1f" y) " years")))))

(defn file-md5 [src]
  (-> src
      slurp
      md5))

(def cached-file-md5 (memoize file-md5))

(defn kebob-keyword [k]
  (when k
    (-> k
        name
        (str/replace #"_" "-")
        keyword)))

(defn kebob [m]
  (if (map? m)
    (->> m
         (map (fn [[k v]]
                [(kebob-keyword k) (kebob v)]))
         (into {}))
    m))

(defn snake-keyword [k]
  (when k
    (-> k
        name
        (str/replace #"-" "_")
        keyword)))

(defn snake [m]
  (if (map? m)
    (->> m
         (map (fn [[k v]]
                [(snake-keyword k) (snake v)]))
         (into {}))
    m))

(defn squeeze
  "Ellipses the middle of a long string."
  [n s]
  (let [n (max (- n 3) 0)]
    (cond
      (<= (count s) n) s
      :else (let [len (count s)
                  half-len (Math/round (/ len 2.0))
                  to-take-out (- len n)
                  half-take-out (Math/round (/ to-take-out 2.0))
                  first-half (take half-len s)
                  second-half (drop half-len s)]
              (str (->> first-half
                        (take (- half-len half-take-out))
                        (apply str))
                   "..."
                   (->> second-half
                        (drop half-take-out)
                        (apply str)))))))

(defn to-short-id [id salt-str]
  (hashids/encrypt id salt-str))

(defn from-short-id [short-id salt-str]
  (hashids/decrypt short-id salt-str))

(defn to-base64 [s & [o]]
  (when s
    (bs/to-string (bt/encode s :base64 o))))

(defn from-base64 [s & [o]]
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

(defn kebab-case [o]
  (csk/->kebab-case o))

(defn env-case [o]
  (csk/->SCREAMING_SNAKE_CASE o))

(defn write-page-data [key payload]
  (str "var "
       (env-case (name key))
       "="
       (-> payload
           to-transit
           to-json)
       ";"))

(defn stacktrace->string [e]
  (let [sw (java.io.StringWriter.)
        pw (java.io.PrintWriter. sw)]
    (.printStackTrace e pw)
    (.toString sw)))

(defn hex-str->byte-array [s]
  (.toByteArray (BigInteger. s 16)))

(defn spy [& os]
  (prn (apply str (butlast os)))
  (pp (last os))
  (last os))

(defn pluralize [n singular plural]
  (if (= 1 n)
    singular
    plural))


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
