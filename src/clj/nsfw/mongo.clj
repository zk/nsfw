(ns nsfw.mongo
  (:require [nsfw.util :as util]
            [somnium.congomongo :as mon]
            [clojure.string :as str])
  (:import [java.net URI]))

(defn bson-id
  ([]
     (org.bson.types.ObjectId.))
  ([id-or-str]
     (org.bson.types.ObjectId/massageToObjectId id-or-str)))

(defn parse-username
  "ex. uri http://foo:bar@zaarly.com returns `\"foo\"`."
  [^URI uri]
  (when-let [un (->> uri
                     .getUserInfo
                     (take-while #(not= \: %))
                     (reduce str))]
    (if-not (empty? un) un nil)))

(defn parse-password
  "ex. uri http://foo:bar@zaarly.com returns `\"bar\"`."
  [^URI uri]
  (when-let [pw (->> uri
                     .getUserInfo
                     (drop-while #(not= \: %))
                     (drop 1)
                     (reduce str))]
    (if-not (empty? pw) pw nil)))


(defn decode-raw-query [raw-query]
  (str/split raw-query #"&"))

(defn parse-mongo-url
  "Takes a string representing a mongod connection and returns a map
  representing the connection information.

  ex. (parse-mongo-url \"mongodb://foo:bar@localhost:123/database?asdf=zxcv\")
  ;; => {:host     \"localhost\"
         :db       \"zaarly\"
         :port     27107
         :username \"foo\"
         :password \"bar\"
         :asdf     \"zxcv\"}"
  [mongo-url-str]
  (let [uri (java.net.URI. mongo-url-str)]
    {:host (.getHost uri)
     :db (->> uri .getPath (drop 1) (apply str))
     :port (.getPort uri)
     :username (parse-username uri)
     :password (parse-password uri)}))

(def fetch-one mon/fetch-one)
(def fetch mon/fetch)
(def fetch-and-modify mon/fetch-and-modify)
(def mongo! mon/mongo!)
(def make-connection mon/make-connection)
(def update! mon/update!)

(defn connect! [url]
  (let [{:keys [host db port username password]} (parse-mongo-url url)]
    (mon/mongo! :host host :db db :port port)
    (when username
      (mon/authenticate username password))))
