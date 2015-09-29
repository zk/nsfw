(ns nsfw.google-auth)

(defn url-encode [s]
  (js/encodeURIComponent s))

(defn sign-in-url [{:keys [client-id redirect-url]}]
  (str
    "https://accounts.google.com/o/oauth2/auth?"
    "response_type=code"
    "&"
    "client_id=" client-id
    "&"
    "redirect_uri=" (url-encode redirect-url)
    "&"
    "scope=email"))

(defn sign-in-link [opts & children]
  (vec
    (concat
      [:a.ga-sign-in {:href (sign-in-url opts)}]
      children)))
