(ns nsfw.gen
  (:require [clojure.string :as string]
            [fleet])
  (:import [java.io File]))

(defn project-root [] "/Users/zkim/napplelabs/test-nsfw/my-proj/")

(defn file-exists [f-str]
  (.exists (File. f-str)))

(defn read-project-name []
  (let [f-str (str (project-root) "/project.clj")]
    (if (file-exists f-str)
      (let [rdr (clojure.lang.LineNumberingPushbackReader. (java.io.FileReader. f-str))
            pdef (read rdr)]
        (str (second pdef)))
      {:success false})))

(defn project-name-underscore []
  (string/replace (read-project-name) #"-" "_"))

(defn mkdir [path-coll]
  (let [f (File. (apply str (project-root) (interpose File/separator path-coll)))]
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
  (let [target-path (str (project-root) target-path)
        project-name (read-project-name)]
    (if (and (file-exists target-path) (not force))
      (report-path-gen "exists" target-path)
      (do
        (spit target-path ((fleet/fleet [project-name] template) project-name))
        (report-path-gen "created" target-path)))))

(defn proj-src-path [s]
  (str "src/" (project-name-underscore) "/" s))

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
    (copy-file (resource-to-str "nsfw/bootstrap.tpl.clj") (proj-src-path "boot.clj") force)
    (copy-file (resource-to-str "nsfw/routes.tpl.clj") (proj-src-path "routes.clj") force)
    (copy-file (resource-to-str "nsfw/404.tpl.html") "resources/public/404.html" force)
    (println)))

(project true)



