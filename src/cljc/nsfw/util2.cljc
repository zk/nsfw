(ns nsfw.util2
  #? (:clj
      (:require [clojure.string :as str]
                [cheshire.custom :as json]
                [clojure.pprint :refer [pprint]]
                [clojure.java.io :as io]
                [hashids.core :as hashids]
                [byte-transforms :as bt]
                [byte-streams :as bs]
                [camel-snake-kebab.core :as csk]
                [cognitect.transit :as transit])
      :cljs
      (:require [clojure.string :as str]
                [nsfw.crypt :as crypt]
                [cognitect.transit :as transit]
                [goog.date :as gd]
                [goog.i18n.DateTimeFormat]))

  #? (:clj
      (:import [java.util Date]
               [java.text SimpleDateFormat]
               [java.net URLEncoder]
               [org.pegdown PegDownProcessor Extensions]
               [org.joda.time.format ISODateTimeFormat]))

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
        (gd/fromIsoString o))
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

 (defn to-json [o]
   #? (:clj
       (json/generate-string o)
       :cljs
       (.stringify js/JSON (clj->js o))))

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
    (defn to-transit [o & [handlers]]
      (let [bs (java.io.ByteArrayOutputStream.)]
        (transit/write
          (transit/writer bs :json {:handlers handlers})
          o)
        (.toString bs)))
    :cljs
    (defn to-transit [o & [handlers]]
      (transit/write
        (transit/writer :json handlers)
        o)))

#? (:clj
    (defn from-transit [s & [handlers]]
      (when s
        (transit/read
          (transit/reader
            (if (string? s)
              (java.io.ByteArrayInputStream. (.getBytes s "UTF-8"))
              s)
            :json
            {:handlers handlers}))))
    :cljs
    (defn from-transit [s & [handlers]]
      (transit/read
        (transit/reader :json handlers)
        s)))

(defn url-encode [s]
  (when s
    #? (:clj
        (java.net.URLEncoder/encode s)
        :cljs
        (js/encodeURIComponent s))))

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

(defn parse-int-base [s base & [default]]
  (if s
    #? (:clj
        (try
          (Integer/parseInt s base)
          (catch Exception e
            default))
        :cljs
        (let [res (js/parseInt s base)]
          (if (js/isNaN res)
            default
            res)))
    default))
