(ns nsfw.facebook-auth
  (:require [nsfw.util :as util]))


(defn login-url [{:keys [client-id
                         redirect-uri
                         scopes]}]
  (str "https://www.facebook.com/dialog/oauth?client_id="
       client-id
       "&redirect_uri="
       (util/url-encode redirect-uri)))
