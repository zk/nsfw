(ns nsfw.http
  "Request / response utilities"
  (:use [plumbing.core])
  (:require [plumbing.graph :as pg]
            [clojure.edn :as edn]))

(defn -graph [gmap strategy]
  (let [g (strategy gmap)]
    (fn [r]
      (:resp (g r)))))

(defn graph [gmap]
  (-graph gmap pg/lazy-compile))

(defn graph-eager [gmap]
  (-graph gmap pg/eager-compile))

(defn html [body]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body body})

(defn clj [body]
  {:headers {"Content-Type" "text/clj; charset=utf-8"}
   :body (pr-str body)})

(def header-for {:html {"Content-Type" "text/html; charset=utf-8"}
                 :js {"Content-Type" "text/javascript; charset=utf-8"}})

(defn headers [& opts]
  (apply merge (map header-for opts)))

(def html-header (headers :html))

(defn redirect [loc & opts]
  (merge
   {:headers {"Location" loc}
    :status 301}
   (apply hash-map opts)))

(defn decode-body [content-length body]
  (when (and content-length
             (> content-length 0))
    (let [buf (byte-array content-length)]
      (.read body buf 0 content-length)
      (.close body)
      (String. buf))))

(defn response-body
  "Turn a HttpInputStream into a string."
  [{:keys [content-length body]}]
  (if (string? body)
    body
    (decode-body content-length body)))

(defn decode-edn-body [r]
  (-> r response-body edn/read-string))

(defn decode-edn-mw [h]
  (fn [r]
    (let [content-type-raw (-> r :headers (get "content-type"))]
      (if (and content-type-raw
               (.contains content-type-raw "application/edn"))
        (h (assoc r :edn-body (decode-edn-body r)))
        (h r)))))

(defn tag-response-mw
  "Middleware for attaching metadata to a response.
   Usage: (-> h (tag-response-mw :handler-ns 'nsfw.http) ...)"
  [h k v]
  (fn [r]
    (let [resp (h r)]
      (when resp
        (vary-meta resp assoc k v)))))

(defn render-edn [body]
  {:headers {"Content-Type" "application/edn;encoding=utf-8"}
   :status 200
   :body (pr-str body)})
