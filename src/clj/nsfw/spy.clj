(ns nsfw.spy
  (:require [clojure.string :as str]
            [nsfw]
            [nsfw.util :as util]
            [nsfw.app :as app]
            [nsfw.html :as html]
            [clj-stacktrace.repl :as cst]))

(defn query-routes [path]
  (->> path
       app/namespaces-in
       app/routes-in-nss
       (sort-by :route)))

(defn query-comps [path]
  (->> path
       app/namespaces-in
       app/comps-in-nss))

(defn escape [s]
  (-> s
      (str/replace #"<" "&lt;")
      (str/replace #">" "&gt;")))

(def exc-css
  [["*" {:padding 0
         :margin 0}]
   [:body {:font-family "\"Helvetica Neue\", Helvetica, sans-serif"
           :padding 0}]
   [:header {:width "100%"
             :background-color :#ccc
             :padding "10px"}]
   [:.content {:padding "20px"}]
   [:pre {:margin-top "20px"}]
   ["h1,h2,h3,h4,h5,h6" {:font-weight "normal"}]])

(defn show-exceptions-mw [h]
  (fn [r]
    (try
      (h r)
      (catch Exception e
        (println "EXCEPTION")
        (html/response
         (html/html5
          [:head
           (apply html/embed-css exc-css)]
          [:body
           [:header
            "NSFW Console"]
           [:div.content
            [:section
             [:h3 "Stacktrace"]
             [:pre.stacktrace (cst/pst-str e)]]
            [:section
             [:h3 "Request"]
             [:pre.request (util/pp-str r)]]]]))))))