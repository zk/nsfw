(ns nsfw.gen
  (:require [clojure.string :as string]
            [fleet])
  (:import [java.io File]))

(def file-sep java.io.File/separator)

(defn file-exists [f-str]
  (.exists (File. f-str)))

(defn as-str [keyword-or-str]
  (if (keyword? keyword-or-str)
    (name keyword-or-str)
    keyword-or-str))

(defn underscore-name [name]
  (string/replace name #"-" "_"))

#_(def *project-root* "/Users/zkim/napplelabs/test-nsfw/my-proj/") ; for dev
(def *project-root* "./")

(defn resolve-file [& path-strs]
  (str *project-root* (apply str (interpose file-sep (map as-str path-strs)))))

(defn resolve-js [& path-strs]
  (let [path-strs (map as-str path-strs)]
    (str *project-root*
         "resources/public/js/"
         (apply str
                (interpose
                 file-sep
                 path-strs))
         (when (not (re-find #"\.js$" (first (reverse path-strs))))
           ".js"))))

(defn read-project-name []
  (let [f-str (resolve-file "project.clj")]
    (if (file-exists f-str)
      (let [rdr (clojure.lang.LineNumberingPushbackReader. (java.io.FileReader. f-str))
            pdef (read rdr)]
        (str (second pdef)))
      {:success false})))


(def *project-name* (read-project-name))
(def *underscore-project-name* (underscore-name *project-name*))

(def *project-src-path* (str (resolve-file "src" *underscore-project-name*) file-sep))

(defn resolve-project-src-file [& path-strs]
  (str *project-src-path*
       (apply str
              (interpose file-sep (map as-str path-strs)))))

(defn wget-js [url & [save-as]]
  (try
    (let [fn (.getFile (java.net.URL. url))
          name (.getName (java.io.File. fn))
          out-fn (if save-as
                   (resolve-js save-as)
                   (resolve-js name))]
      (spit out-fn (slurp url)))
    {:success true}
    (catch java.net.MalformedURLException e
      {:success false :message "Malformed URL" :exception e})
    (catch java.net.UnknownHostException e
      {:success false :message "Unknown Host" :exception e})
    (catch java.io.FileNotFoundException e
      {:success false :message "Remote File Not Found" :exception e})
    (catch Exception e (throw e))))

(defn mkdir [path-coll]
  (let [f (if (string? path-coll)
            (File. path-coll)
            (File. (apply resolve-file path-coll)))]
    (if (and (.exists f))
      {:success false :status "exists" :path (.getPath f)}
      (do
        (.mkdirs f)
        {:success true :status "created" :path (.getPath f)}))))

(defn resource-to-str [resource]
  (let [sb (StringBuilder.)]
    (with-open [stream (.getResourceAsStream (.getContextClassLoader (Thread/currentThread)) resource)]
      (loop [c (.read stream)]
           (if (neg? c)
             (str sb)
             (do
               (.append sb (char c))
               (recur (.read stream))))))))

(defn report-path-gen [status path]
  (println "  " status " | " path))

(defn copy-file [template target-path force]
  (let [target-path (resolve-file target-path)
        project-name (read-project-name)]
    (if (and (file-exists target-path) (not force))
      (report-path-gen "exists" target-path)
      (do
        (spit target-path ((fleet/fleet [project-name] template) project-name))
        (report-path-gen "created" target-path)))))

(defn project [& [force]]
  (let [dirs [["resources"]
              ["resources" "public"]
              ["resources" "public" "css"]
              ["resources" "public" "js"]
              ["resources" "public" "images"]
              ["src" "main"]]]
    (println "Initializing Project")
    (doseq [d dirs]
      (let [res (mkdir d)]
        (println "  " (:status res) " | " (:path res))))
    (println)
    (copy-file (resource-to-str "nsfw/bootstrap.tpl.clj") (resolve-project-src-file "boot.clj") force)
    (copy-file (resource-to-str "nsfw/routes.tpl.clj") (resolve-project-src-file "routes.clj") force)
    (copy-file (resource-to-str "nsfw/404.tpl.html") "resources/public/404.html" force)
    (println)))

(defn module [name & [force]]
  (let [name (as-str name)
        parts (string/split name #"\.")
        parts-count (count parts)
        path (take (- parts-count 1) (map underscore-name parts))
        file (str (first (drop (- parts-count 1) (map underscore-name parts))) ".clj")
        ns (symbol (apply str *project-name* "." (interpose "." parts)))
        tpl (str ((fleet/fleet [module-ns] (resource-to-str "nsfw/module.tpl.clj")) ns))
        out-file-name (apply resolve-project-src-file (apply vector (reverse (conj (reverse path) file))))]
    (mkdir (apply resolve-project-src-file path))
    (report-path-gen (if (or (not (file-exists out-file-name)) force)
                       (do (spit out-file-name tpl)
                           "created")
                       "exists")
                     out-file-name)))


