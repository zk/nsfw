(ns nsfw
  (:require [nsfw.routing :as routing]
            [nsfw.html :as html]
            [hiccup.page]
            [hiccup.core]
            [nsfw.http :as http]
            [nsfw.http.response :as resp]
            [ring.middleware.file :as rm-file]
            [ring.middleware.file-info :as rm-file-info]
            [ring.middleware.params :as rm-params]
            [ring.middleware.session :as rm-session]
            [ring.middleware.session.cookie :as rm-session-cookie]
            [ring.middleware.nested-params :as rm-nested-params]
            [ring.middleware.keyword-params :as rm-keyword-params]
            [clojure.string :as str]))

(def router routing/router)

(def mount routing/mount)

(defn wrap-params [h]
  (-> h
      rm-keyword-params/wrap-keyword-params
      rm-nested-params/wrap-nested-params
      rm-params/wrap-params))

(defn wrap-file [h path & [opts]]
  (-> h
      rm-file-info/wrap-file-info
      (rm-file/wrap-file path opts)))

(def wrap-decode-edn-body http/wrap-decode-edn-body)

(def wrap-decode-json-body http/wrap-decode-json-body)

(def wrap-render-hiccup http/wrap-render-hiccup)

(def html-response resp/html-response)

(def wrap-session rm-session/wrap-session)

(def cookie-store rm-session-cookie/cookie-store)

;;;

(comment
  ((-> (fn [r] {:headers {"content-type" "text/html;charset=utf-8"} :body [:html5 [:h1 "hello world"]]})
       wrap-render-hiccup)
   {})

  ((-> [:any "/" (fn [r] {:body "hello world"})]
       router
       wrap-decode-json-body
       wrap-decode-edn-body)
   {:uri "/" :request-method :get})

  ((-> [:any "/" (fn [r] {:body "hello world"})]
       router
       wrap-decode-json-body
       wrap-decode-edn-body
       (wrap-file "resources/public"))
   {:uri "/img/landing-background.jpg" :request-method :get}))
