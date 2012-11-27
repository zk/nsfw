(ns nsfw.test.github
  (:use [nsfw.github] :reload)
  (:use [clojure.test])
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]
            [nsfw.html :as html]))

(deftest test-auth-redirect-url
  (is (= "https://github.com/login/oauth/authorize?client_id=c%2Fi&redirect_uri=r%2Fi&scope=f%2Fb%2Cbstate=s%2Fe"
         (auth-redirect-url "c/i" "r/i" ["f/b" "b"] "s/e"))))




;;; Integration testing for github token exchange

(def server-name :gh-auth-test-server)
(def server-port 15381)

;; Insert your test app's credentials here
(def client-id "d064eb8e10094af515da")
(def secret "ac8d6d0f2cb2b6483f210e0ef5eecaddb5332e2f")
(def scopes [:user :repo])
(def redirect-url "http://localhost:15381/exchange")

;; Templating
(defn cred-row [label val]
  [:tr
   [:td.label label]
   [:td.val val]])

(defn page [step]
  (fn [{:keys [query-params]}]
    (let [exchange-response (exchange-code client-id
                                           secret
                                           (get query-params "code"))
          user (user (:access_token exchange-response))]
      {:headers {"Content-Type" "text/html"}
       :body (html/html5
              [:head
               [:title "NSFW GitHub Auth Test"]
               (html/embed-css
                [:body     {:font-family "'Palatino Linotype', serif"}]
                [:h1 {:font-family "Helvetica, sans-serif"
                      :margin-bottom :30px}]
                [:div.page {:width :600px
                            :margin-left :auto
                            :margin-right :auto
                            :margin-top :100px}]
                ["a, a:visited" {:color :#88F
                                 :text-decoration :none}]
                [:em {:font-style :normal
                      :font-weight :bold}]
                [:table {:margin-bottom :20px
                         :margin-top :20px}]
                [:td {:padding-right :20px}]
                [:td.label {:text-align :right}]
                [:td.val {:font-weight :bold}]
                [:.step {:margin-bottom :30px}]
                [:.step.inactive {:color :#ccc}]
                [".step.inactive a" {:color :#ccf}]
                [:div.code {:font-family "monospace"
                            :margin-top :10px
                            :margin-bottom :10px}]
                [:div.user-info {:margin :20px}]
                ["div.user-info img" {:float :left
                                      :margin-right :10px
                                      :border-radius :3px}])]
              [:body
               [:div.page
                [:h1
                 [:a {:href "https://github.com/zkim/nsfw"} "NSFW"]
                 " GitHub Auth Test"]
                [:ol
                 [:li {:class (str "step" (when-not (= step :step1) " inactive"))}
                  "GitHub credentials:"
                  [:div
                   [:table
                    (cred-row "Client ID" client-id)
                    (cred-row "Client Secret" secret)
                    (cred-row "Scopes" (->> scopes (map name) (interpose ", ") (apply str)))
                    (cred-row "Redirect URL" redirect-url)]]
                  "Click"
                  [:a.redirect {:href (auth-redirect-url client-id redirect-url scopes)}
                   " here "]
                  "to be redirected to GitHub, where you'll be asked to authorize access to your account."]
                 (if (:error exchange-response)
                   [:li {:class (str "step" (when-not (= step :step2) " inactive"))}
                    "Ruh-roh! Looks like there was an error exchanging the temporary code:"
                    [:div.code
                     (str exchange-response)]]
                   [:li {:class (str "step" (when-not (= step :step2) " inactive"))}
                    "Your temporary code "
                    [:em (get query-params "code" "XXXXXXXXXXXX")]
                    " was exchanged for the long-lived "
                    [:em (get exchange-response :token_type "XXXXXX")]
                    " token "
                    [:em (get exchange-response :access_token "XXXXXXXXXXXXXXXXXXXXXXX")]
                    (let [{:keys [avatar_url login name email]} user]
                      [:div.user-info
                       [:img {:src avatar_url}]
                       [:div.login login]
                       [:div.name name]
                       [:div.email email]])])]]])})))

(def entry-point
  (webapp/routes
   [""] (page :step1)
   ["exchange"] (page :step2)))

(server/start :name server-name
              :port server-port
              :entry entry-point)



