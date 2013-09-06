(ns nsfw.util
  (:use hiccup.core
        [hiccup.page :only (doctype)]
        [clojure.pprint :only (pprint)])
  (:require [clj-stacktrace.repl :as stacktrace]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [cheshire.custom :as json])
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


(def iso-formatter (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn iso-8601 [o]
  (cond
   (= Date (class o)) (.format iso-formatter o)
   (= java.lang.Long (class o)) (.format iso-formatter (Date. o))
   (= nil o) nil
   (= "" o) nil
   :else nil))

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
  [#^String str]
  (let [alg (doto (java.security.MessageDigest/getInstance "MD5")
              (.reset)
              (.update (.getBytes str)))]
    (try
      (.toString (new BigInteger 1 (.digest alg)) 16)
      (catch java.security.NoSuchAlgorithmException e
        (throw (new RuntimeException e))))))

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

(defn from-json [s]
  (json/parse-string s true))

(defn url-encode [s]
  (when s
    (URLEncoder/encode s)))

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