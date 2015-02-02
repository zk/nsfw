(ns nsfw.http.response
  (:require [ring.util.response :as rres]))

(defn content-type [resp content-type]
  (assoc-in resp [:headers "Content-Type"] content-type))

(def html-response {:status 200
                    :body ""
                    :headers {"Content-Type" "text/html;charset=utf-8"}})

(def edn-response {:status 200
                   :body nil
                   :headers {"Content-Type" "application/edn;charset=utf-8"}})

(def json-response {:status 200
                    :body nil
                    :headers {"Content-Type" "application/edn;charset=utf-8"}})

(defn html [body]
  (assoc html-response :body body))

(defn edn [body]
  (assoc edn-response :body body))

(defn json [body]
  (assoc json-response :body body))

(defn parse-ops-body [[-opts -body]]
  (let [opts (if -body -opts {})
        body (if -body -body -opts)]
    [opts body]))

(defn html-response
  "Takes a ring acceptable response body, additionally hiccup-style, and returns an html response"
  [& args]
  (let [[opts body] (parse-ops-body args)]
    (merge
      (html body)
      opts)))

;;;


(comment



  )
