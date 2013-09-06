(ns nsfw.util
  (:require #_[cljs-uuid-utils :as uu]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [nsfw.crypt :as crypt]
            [goog.date :as gd]
            [goog.i18n.DateTimeFormat]))

(defn log [& args]
  (.log js/console (if args (to-array args) nil)))

(defn log-pass [res]
  (log res)
  res)

(defn lpr [& args]
  (.log js/console (to-array (map pr-str args))))

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

(defn to-json [data]
  (.stringify js/JSON (clj->js data)))

(defn timeout [f delta]
  (js/setTimeout f delta))

(defn interval [f delta]
  (js/setInterval f delta))

(defn clear-timeout [timeout]
  (js/clearTimeout timeout))

(defn uuid []
  #_(cljs-uuid-utils/make-random-uuid)
  (gensym))

(defn page-data [key & [default]]
  (try
    (reader/read-string (aget js/window (str/replace (name key) #"-" "_")))
    (catch js/Error e
      (if default
        default
        (throw (str "Couldn't find page data " key))))))

(defn run-once [f]
  (let [did-run (atom false)]
    (fn [& args]
      (when-not @did-run
        (reset! did-run true)
        (apply f args)))))

(defn toggle [f0 f1]
  (let [!a (atom false)]
    (fn [& args]
      (let [res (if-not @!a (apply f0 args) (apply f1 args))]
        (swap! !a not)
        res))))

(defn ms [date]
  (.getTime date))

(defn now-ms []
  (ms (js/Date.)))


(defn timeago [date]
  (let [ms (- (now-ms) (ms date))
        s (/ ms 1000)
        m (/ s 60)
        h (/ m 60)
        d (/ h 24)
        y (/ d 365)]
    (cond
     (< s 60) "less than a minute"
     (< m 2) "1 minute"
     (< h 1) (str (int m) " minutes")
     (< d 1) (str (int h) " hours")
     (< d 2) "1 day"
     (< y 1) (str (int d) " days")
     (< y 2) "over a year")))

(defn ref? [o]
  (instance? cljs.core/Atom o))

#_(log timeago (js/Date. (- (now-ms) 100000)))

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

(defn format-date
  ([pattern date]
     (.format (goog.i18n.DateTimeFormat. pattern) date)))