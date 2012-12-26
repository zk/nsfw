(ns nsfw.webapp
  (:require [net.cgrand.moustache :as moustache]
            [nsfw.html :as html]
            [nsfw.middleware :as nm]
            [nsfw.util :as nu]
            [clojure.string :as str]))

(defmacro routes [& body]
  `(moustache/app
    nm/wrap-web-defaults
    ~@body))

(defn cs
  "Provies a route helper to set up a clojurescript app."
  [script & ops]
  (fn [r]
    (let [{:keys [entry title data]}
          (apply hash-map ops)]
      {:headers {"Content-Type" "text/html"}
       :body (html/html5
               [:head
                (when title
                  [:title title])
                (html/stylesheet (str "/css/" (name script) ".css"))]
               [:body
                (when data
                  [:script {:type "text/javascript"}
                   (str
                    (->> data
                         (map #(str "window."
                                    (name (key %))
                                    " = "
                                    (-> % val pr-str nu/to-json)))
                         (apply str))
                    ";")])
                (html/script (str "/js/" (name script) ".js"))
                (when entry
                  [:script {:type "text/javascript"}
                   (str (name entry) ".main()")])])})))