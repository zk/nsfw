(ns nsfw.util
  (:use [hiccup core
         [page-helpers :only (doctype)]])
  (:require [clj-stacktrace.repl :as stacktrace]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [org.danlarkin.json :as json]))

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

(def *local-js-root* "./public/js")

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

(defn as-str [thing]
  (if (keyword? thing)
    (str (name thing))
    (str thing)))

(defn css [& in]
  (apply
   str
   (interpose
    " "
    (map 
     (fn [r]
       (cond
        (vector? r) (let [sels (take (- (count r) 1) r)
              rules (first (reverse r))]
         
          (str (if (vector? (first sels))
                 (apply str (interpose ", " (map #(apply str (interpose " " (map as-str %))) sels)))
                 (apply str (interpose " " (map as-str sels))))
               " {"
               (apply str (map #(str (name (key %)) ":" (as-str (val %)) ";") rules))
               "}"))
        :else r))
     in))))

(defn throw-str [& args]
  (throw (Exception. (apply str (interpose " " args)))))

(defn md5-sum
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
  (when email
    (let [email (->> email
                     (str/trim)
                     (str/lower-case))
          url (str "http://gravatar.com/avatar/" (md5-sum email))]
      (if size
        (str url "?s=" size)
        url))))

(defn html-response [& body]
  (-> (html body)
      (resp/response)
      (resp/header "Content-Type" "text/html;charset=utf-8")))

(defn json-encode [o]
  (json/encode o))

(defn json-decode [s]
  (json/decode s))

(defn ich-tpl [name & body]
  (html
   [:script {:id name :type "text/html"}
    body]))

(defn sha1-str [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))] 
    (->> (.digest (java.security.MessageDigest/getInstance "SHA1") bytes)
         (map #(Integer/toHexString (bit-and % 0xff)))
         (apply str))))

(defn uuid []
  (-> (java.util.UUID/randomUUID)
      (str)
      (str/replace #"-" "")))

(defn html5 [& content]
  (html
   (doctype :html5)
   [:html
    content]))

