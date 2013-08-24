(ns nsfw.foreman
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh])
  (:import [java.lang Runtime]
           [java.io BufferedInputStream]))

;; GO AVAY

(comment
  (use 'clojure.pprint)

  (defonce procs (atom {}))

  (->> "Procfile"
       slurp
       (#(str/split % #"\n"))
       (map #(map str/trim (str/split % #":" 2)))
       pprint)

  (.exec (Runtime/getRuntime) "ls -aul")

  (let [proc (.exec (Runtime/getRuntime) "./loop.sh")
        bis (BufferedInputStream. (.getInputStream proc))
        buf (byte-array 1024)
        line (atom "")]
    (Thread/sleep 1000)
    ))
