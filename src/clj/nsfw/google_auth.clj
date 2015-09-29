(ns nsfw.google-auth
  (:require [nsfw.util :as util]
            [nsfw.http-client :as hc]
            [clj-jwt.core :as jwt]))

(defn exchange [ring-req
                {:keys [client-id
                        client-secret
                        redirect-uri]}]
  (let [code (-> ring-req :params :code)
        {:keys [status body] :as resp}
        (hc/request
          {:method :post
           :url "https://www.googleapis.com/oauth2/v3/token"
           :query-params
           {:code code
            :client_id client-id
            :client_secret client-secret
            :grant_type "authorization_code"
            :redirect_uri redirect-uri}})
        {:keys [error
                error_description
                access_token
                id_token]} (util/from-json body)]
    (if error
      {:success? false :error-code error :error error_description}
      (let [jwt (jwt/str->jwt id_token)
            {:keys [email email_verified]} (:claims jwt)]
        {:success? true
         :access-token access_token
         :email email :email-verified? email_verified
         :jwt (into {} jwt)}))))


(defn handler [opts f]
  (fn [r] (f (exchange r opts))))

#_(exchange
    {:params {:code "foo"}}
    {:client-id "485322283358-kb9f73crtfsdqkseh60ko09nojj6654b.apps.googleusercontent.com"
     :client-secret "LRuB8djiGO_68TVz7uz1BzcO"
     :redirect-uri "http://localhost:8080/admin-login/callback"})
