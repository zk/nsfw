(ns nsfw.util
  (:require [clj-stacktrace.repl :as stacktrace]
            [clojure.string :as string]))

(defn web-stacktrace [e req]
  (str "<html><body>"
       "<h1>500 - " (.getMessage e) "</h1>"
       
       "<pre>" (stacktrace/pst-str e) "</pre>"

       "<pre>" (string/replace (str req) #", " "\n") "</pre>"
       "</html></body>"))