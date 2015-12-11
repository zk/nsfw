(ns devguide.entry
  (:require [bidi.ring :refer [make-handler]]
            [devguide.config :as config]
            [nsfw.http :as http]
            [nsfw.util :as util]))

(defn four-oh-four [r]
  (merge
    (http/html-resp
      [:html5
       [:body.four-oh-four
        [:h1 "404"]]])
    {:status 404}))

(defn handle-exception [{:keys [exception]}]
  (.printStackTrace exception)
  (merge
    (http/html-resp
      [:html5
       [:head
        [:link {:rel "stylesheet" :href "/css/bootstrap.min.css"}]
        [:link {:rel "stylesheet" :href "/css/app.css"}]]
       [:body.five-hundred
        [:br]
        [:br]
        [:br]
        [:div.container
         [:div.row
          [:div.col-sm-6.col-sm-offset-3.text-center
           [:h1 "500"]
           [:h2 "Whoops! Sorry, looks like we had a problem making this page for you."]]]]]])
    {:status 500}))

#_(defn static [r]
    (http/html-resp
      [:html5
       [:head
        [:link {:rel "stylesheet"
                :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
                :crossorigin "anonymous"}]]
       [:body
        [:div.container
         [:div.row
          [:div.col-sm-12
           [:h1 "ok! (static)"]]]]]]))

(def lato
  {:css ["https://fonts.googleapis.com/css?family=Lato:400,300,700,100"]})

(def open-sans
  {:css ["https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700"]})

(defn cljs [r]
  (http/render-spec
    [http/bootstrap3
     lato
     open-sans
     {:css ["/css/app.css"]}
     {:js ["/cljs/app.js"]
      :env {:js-entry :main}}]))

(defn routes []
  ["/" {"" cljs}])

(defn handler []
  (-> (routes)
      make-handler
      (http/wrap-cookie-session
        config/session-domain
        (util/hex-str->byte-array config/session-key))
      http/wrap-params
      (http/wrap-404 four-oh-four)
      (http/wrap-exception handle-exception)
      http/wrap-html-response
      (http/wrap-file "resources/public")))
