(ns nsfw.github
  "Provides auth^2 from the v3 github api.

   This library assumes you're using a ring-compatible response format.

   The GitHub auth flow is relatively simple, and consists of three
   steps:

   1. Redirect users to github to request access permissions.
   2. Handle the redirect back from github and exchange the recieved
      temporary code for a long-lived access token by posting to the
      github api.
   3. Use the long-lived access token to query the user's protected
      information from the github api.

   See http://developer.github.com/v3/oauth#web-application-flow for
   more information."

  (:require [nsfw.http-client :as hc]
            [nsfw.util :as u]))

(def login-base-url "https://github.com/login/oauth")

(defn auth-redirect-url
  "Generates a github authorization URL."
  [client-id redirect-uri scopes & [state]]
  (str login-base-url
       "/authorize?"
       "client_id=" (u/url-encode client-id) "&"
       "redirect_uri=" (u/url-encode redirect-uri) "&"
       "scope=" (->> scopes
                     (interpose ",")
                     (apply str)
                     u/url-encode)
       (when state
         (str "state=" (u/url-encode state)))))

(defn exchange-code [client-id client-secret code & [state]]
  (when code
    (let [qp {:client_id client-id
              :client_secret client-secret
              :code code}
          qp (if-not state
               qp
               (merge qp {:state state}))
          res (hc/request {:method :post
                           :url (str login-base-url "/access_token")
                           :query-params qp
                           :headers {"Accept" "application/json"}})]
      (if (= 200 (:status res))
        (u/from-json (:body res))
        {:error "unknown"}))))

(defn user [token]
  (when token
    (let [res (hc/request {:method :get
                           :url "https://api.github.com/user"
                           :query-params {:access_token token}})]
      (-> res :body u/from-json))))
