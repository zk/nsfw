(ns nsfw.twitter
  (:use [clojure.pprint :as pprint])
  (:require [oauth.twitter :as tw]
            [nsfw.util :as nu]))

(def tw-key "omKhSwwjUR60OeoVukj2nw")

(def tw-secret "tAB0jkD3J8LzxgnaW8WtmSUxlwRqCoWmmKJTzMbag")

(def request-token (tw/oauth-request-token tw-key tw-secret))

(println (tw/oauth-authorization-url (:oauth-token request-token)))


(defn auth-url [opts]
  (let [{:keys [callback token]}
        opts]
    (str (tw/oauth-authorization-url token)
         "&oauth_callback="
         (nu/url-encode callback))))

(println
 (auth-url {:callback "http://localhost:8080"
            :token (:oauth-token request-token)}))