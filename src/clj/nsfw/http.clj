(ns nsfw.http
  "Request / response utilities"
  (:use [plumbing.core])
  (:require [plumbing.graph :as pg]
            [clojure.edn :as edn]
            [nsfw.util :as util]
            [hiccup.page]
            [hiccup.core]
            [cognitect.transit :as transit]
            [clojure.string :as str]
            [bidi.ring :as bidi]
            [ring.middleware
             file
             file-info
             session
             params
             nested-params
             multipart-params
             keyword-params]
            [ring.middleware.session.cookie :only (cookie-store)]
            [ring.util.response :only (response content-type)])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defn -graph [gmap strategy]
  (let [g (strategy gmap)]
    (fn [r]
      (:resp (g r)))))

(defn graph [gmap]
  (-graph gmap pg/lazy-compile))

(defn graph-eager [gmap]
  (-graph gmap pg/eager-compile))

(defn html-resp [body]
  {:headers {"content-type" "text/html;charset=utf-8"}
   :body body})

(defn edn [body]
  {:headers {"content-type" "application/edn;charset=utf-8"}
   :body (pr-str body)})

(def header-for {:html {"Content-Type" "text/html; charset=utf-8"}
                 :js {"Content-Type" "text/javascript; charset=utf-8"}})

(defn headers [& opts]
  (apply merge (map header-for opts)))

(def html-header (headers :html))

(defn redirect [loc opts]
  (merge
    {:headers {"Location" loc}
     :status 301}
    opts))

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

(defn wrap-edn-request [h]
  (fn [r]
    (let [content-type-raw (-> r :headers (get "content-type"))]
      (if (and content-type-raw
               (.contains content-type-raw "application/edn"))
        (h (assoc r :edn-body (decode-edn-body r)))
        (h r)))))

(defn render-edn [body]
  {:headers {"Content-Type" "application/edn; encoding=utf-8"}
   :status 200
   :body (pr-str body)})

(defn decode-content-type [cts]
  (let [parts (str/split cts #";")
        media-type (some->
                     (first parts)
                     str/trim)]
    {:media-type media-type
     :params (->> parts
                  rest
                  (map str/trim)
                  (remove empty?)
                  (map #(str/split % #"="))
                  (map (fn [[k v]]
                         [(keyword k) v]))
                  (into {}))}))

(defn content-type [request]
  (when-let [cts (or (get-in request [:headers "Content-Type"])
                     (get-in request [:headers "content-type"]))]
    (decode-content-type cts)))

(defn json-content? [req]
  (= "application/json"
     (:media-type (content-type req))))

(defn render-json
  ([opts body]
     (merge {:headers {"Content-Type" "application/json;encoding=utf-8"}
             :status 200
             :body (util/to-json body)}
            opts))
  ([body]
     (render-json {} body)))

(defn wrap-decode-json-body [h]
  (fn [r]
    (if (json-content? r)
      (try
        (let [json-body (-> r util/response-body util/from-json)]
          (h (assoc r :json-body json-body)))
        (catch JsonParseException e
          (render-json
            {:status 400}
            {:message "Problems parsing JSON"})))
      (h r))))

(defn html-response? [r]
  (= "text/html"
     (:media-type (content-type r))))

(defn hiccup->html-string [body]
  (if-not (vector? body)
    body
    (if (= :html5 (first body))
      (hiccup.page/html5 (rest body))
      (hiccup.core/html body))))

(defn wrap-html-response
  "Render hiccup vector into an HTML string when the content type is
  text/html."
  [h]
  (fn [req]
    (let [resp (h req)]
      (if (and (html-response? resp)
               (vector? (:body resp)))
        (update-in resp [:body] hiccup->html-string)
        resp))))

(defn wrap-json-response [h]
  (fn [r]
    (let [res (h r)]
      (-> res
          (update-in [:body] util/to-json)
          (assoc-in [:headers "Content-Type"]
            "application/json;charset=utf-8")))))

(defn wrap-json-request [h]
  (fn [r]
    (if (= "application/json"
           (:media-type (content-type r)))
      (h (update-in r [:body] util/from-json))
      (h r))))

(defn from-transit [s]
  (transit/read
    (transit/reader
      (if (string? s)
        (java.io.ByteArrayInputStream. (.getBytes s "UTF-8"))
        s)
      :json)))

(defn to-transit [o]
  (let [bs (java.io.ByteArrayOutputStream.)]
    (transit/write
      (transit/writer bs :json)
      o)
    (.toString bs)))

(defn wrap-transit-response [h]
  (fn [r]
    (let [res (h r)]
      (-> res
          (update-in [:body] to-transit)
          (assoc-in [:headers "Content-Type"]
            "application/transit+json;charset=utf-8")))))

(defn wrap-transit-request [h]
  (fn [r]
    (if (= "application/transit+json"
           (:media-type (content-type r)))
      (h (update-in r [:body] from-transit))
      (h r))))

(defn routes->bidi [routes]
  ["" (->> routes
           (group-by :path)
           (map (fn [[path route-fragments]]
                  [path (->> route-fragments
                             (map (fn [{:keys [method handler]}]
                                    [method handler]))
                             (into {}))]))
           (into {}))])

(defn routes->handler [routes]
  (bidi/make-handler (routes->bidi routes)))

(defn wrap-404 [h]
  (fn [r]
    (let [res (h r)]
      (if-not (nil? res)
        res
        {:status 404 :body "not found!!"}))))

(defn wrap-params [h]
  (-> h
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.multipart-params/wrap-multipart-params
      ring.middleware.nested-params/wrap-nested-params
      ring.middleware.params/wrap-params))

(defn wrap-file [h dir]
  (-> h
      (ring.middleware.file/wrap-file dir {:allow-symlinks? true})
      (ring.middleware.file-info/wrap-file-info)))

(defn wrap-cookie-session [h domain key]
  (-> h
      (ring.middleware.session/wrap-session
        {:store (ring.middleware.session.cookie/cookie-store
                  {:domain domain
                   :key key})})))
