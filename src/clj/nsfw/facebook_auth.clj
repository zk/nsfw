(ns nsfw.facebook-auth
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
          {:method :get
           :url "https://graph.facebook.com/v2.3/oauth/access_token"
           :query-params
           {:code code
            :client_id client-id
            :client_secret client-secret
            :redirect_uri redirect-uri
            :scope "public_profile,email"}})
        {:keys [error
                error_description
                token_type
                access_token
                expires_in]} (util/from-json body)]
    (if error
      {:success? false :error error}
      {:success? true
       :access-token access_token
       :token-type token_type
       :expires-in expires_in})))

(defn get-info [token]
  (let [{:keys [status body] :as resp}
        (hc/request
          {:method :get
           :url "https://graph.facebook.com/me"
           :query-params {:access_token token
                          :fields "name,email,picture,id"}})]
    (util/from-json body)))

(defn handler [opts f]
  (fn [r] (f r (exchange r opts))))
