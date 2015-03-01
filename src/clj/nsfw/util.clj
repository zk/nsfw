(ns nsfw.util
  (:require [clj-stacktrace.repl :as stacktrace]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [cheshire.custom :as json]
            [clojure.pprint :refer [pprint]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [hashids.core :as hashids]
            [byte-transforms :as bt]
            [byte-streams :as bs])
  (:import [java.util Date]
           [java.text SimpleDateFormat]
           [java.net URLEncoder]
           [org.pegdown PegDownProcessor Extensions]))

;; Logging

(defn clean-val [val]
  (cond
   (string? val) (str "\"" (str/replace val #"\"" "\\\\\"") "\"")
   :else val))

(defn format-log-entry [key-vals]
  (->> (partition 2 key-vals)
       (map #(str (name (first %)) "=" (clean-val (second %))))
       (interpose " ")
       (reduce str)
       (str (System/currentTimeMillis) " ")))

(defn make-logger [app-id ns]
  (fn [& key-vals]
    (println (format-log-entry (concat [:app-id app-id :ns ns] key-vals)))))

(def iso-formatter (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn iso-8601 [o]
  (cond
   (= Date (class o)) (.format iso-formatter o)
   (= java.lang.Long (class o)) (.format iso-formatter (Date. o))
   (= nil o) nil
   (= "" o) nil
   :else nil))

(defn format-ms [ms format]
  (let [d (Date. ms)]
    (.format (SimpleDateFormat. format) d)))

(defn web-stacktrace [e req]
  (str "<html><body>"
       "<h1>500 - " (.getMessage e) "</h1>"

       "<pre>" (stacktrace/pst-str e) "</pre>"

       "<pre>" (str/replace (str req) #", " "\n") "</pre>"
       "</html></body>"))

(defn include-css
  ([n]
     (when n
       (if (= :all n)
         (->> (file-seq (java.io.File. "./public/css/"))
              (filter #(.endsWith (.getName %) ".css"))
              (filter #(.isFile %))
              (map #(.getName %))
              (map include-css))
         (let [filename (if (keyword? n)
                          (str (name n) ".css")
                          n)]
           (html [:link {:rel "stylesheet" :href (str "/css/" filename) :type "text/css"}])))))
  ([n & more]
     (cons (include-css n) (map include-css more))))

(defn include-js
  ([] nil)
  ([n]
     (when n
       (if (= :all n)
         (->> (file-seq (java.io.File. "./public/js/"))
              (filter #(.endsWith (.getName %) ".js"))
              (filter #(.isFile %))
              (map #(.getName %))
              (map include-css))
         (let [filename (if (keyword? n)
                          (str (name n) ".js")
                          n)]
           (html [:script {:type "text/javascript" :src (str "/js/" filename)}])))))
  ([n & more]
     (cons (include-js n) (map include-js more))))

(defn container [width & body]
  (apply vector (concat [:div {:class (str "container_" width)}] body)))

(def container-16 (partial container 16))

(defn grid [width & body]
  (apply vector (concat [:div {:class (str "grid_" width)}] body)))

(def grid-16 (partial grid 16))

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
    (.toString
      (new BigInteger 1
        (bt/hash o :md5 opts))
      16)))

(defn grav-url-for [email & [_ size]]
  (let [email (->> (or email "")
                   (str/trim)
                   (str/lower-case))
        url (str "https://gravatar.com/avatar/" (md5 email) "?d=identicon")]
    (if size
      (str url "&s=" size)
      url)))

(defn html-response [& body]
  (-> (html body)
      (resp/response)
      (resp/header "Content-Type" "text/html;charset=utf-8")))

(json/add-encoder org.bson.types.ObjectId json/encode-str)

(defn to-json [o]
  (json/generate-string o))

(defn from-json [o]
  (if (string? o)
    (json/parse-string o true)
    (json/parse-stream (io/reader o) true)))

(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

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

(def pp pprint)

(defn pp-str [o]
  (let [w (java.io.StringWriter.)]
    (pprint o w)
    (.toString w)))

(defn stacktrace->str [exc]
  (let [sw (java.io.StringWriter.)
        pw (java.io.PrintWriter. sw)]
    (.printStackTrace exc pw)
    (.toString sw)))

(defn now [] (System/currentTimeMillis))

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

(defn sha256 [s & [o]]
  (when s
    (bs/to-string (bt/hash s :sha256 o))))
