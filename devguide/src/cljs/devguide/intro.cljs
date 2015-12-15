(ns devguide.intro
  (:require [devguide.common :as com]))

(defn $render [!app bus]
  [:div.dg-section
   [:h1 "A Magical Journey"]
   [:p "NSFW is a kitchen-sink style library that helps you build solid webapps quickly and robustly. There are many, many assumptions built into these set of tools that may not apply to your particular use case, so probably don't use this."]
   [:h2 "General Dev Cycle"]
   [:p "One of the more important parts of efficient development is having very short change-check cycles. In other words, the amount of time between making a change and seeing the result of that change should be as short as possible. NSFW provides several mechanisms to help with this."]
   [:h3 "Reup"]
   [:p "Utilities for supporting a " [:code "clojure.tools.namespace"] " reloading dev lifecycle."]
   [:p "Using " [:code "nsfw.reup"] " lets you restart your dev environment (read: restarting servers, database connections, and other heavyweight resources) in an easy way, helping you start from a good known state whenever you make changes to your source. In addition to shortening your dev cycle, this will help you write more composible code."]
   [:h4 "Usage"]
   [:p
    "Place a file named"
    [:code "reup.clj"]
    " at the root of your project."]
   [:pre
    "
;; reup.clj
(in-ns 'user)

(require '[nsfw.reup :as reup])

(def reup
  (reup/setup
    {:start-app-sym 'main/start-app
     :stop-app-sym 'main/stop-app
     :tests-regex #\"devguide.*-test\"}))

(reup)


;; project.clj

:repl-options {:init (load-file \"reup.clj\")}
"]
   [:p
    "Evaluating "
    [:code "(user/reup)"]
    " will run:"]
   [:ol
    [:li [:code "main/stop-app"]]
    [:li [:code "main/start-app"]]
    [:li "Optionally run tests specified by the " [:code ":test-regex"] " key."]]

   [:p "Pro tip: bind " [:code "user/reup"] " to a key combo."]
   [:p "For reference, here are example start / stop functions:"]
   [:pre
    "(defn start-app []
  (compile-css)
  (let [res (server/start-aleph
              (entry/handler)
              {:port config/port})]
    (prn \"*** Server Up ***\")
    (fn []
      (server/stop-aleph res))))

(defn stop-app [f]
  (when f
    (f)))"]

   [:h3 "Figwheel"]
   [:p [:a {:href "https://github.com/bhauman/lein-figwheel"}
        "Figwheel"]
    " provides tools for instant feedback when developing ClojureScript applications."]

   [:h2 "App Entry Point"]
   [:p "Initialization of your app results in a running process in memory, one that handles requests and renders responses. Usually this includes creation of other heavyweight components, such as database connections and background processes."]
   [:p "NSFW proves two reloading-friendly helpers for generating the entry and tear-down functions for your app:"]
   (com/fn-doc
     {:name "gen-start-app"
      :ns "nsfw.server"}
     [:div
      [:h5 "Example"]
      [:p
       [:code "gen-start-app"]
       " will generate a function that, when called, will initialize your application."]
      [:pre "(server/gen-start-app
  {:create-handler entry/handler
   :create-ctx entry/create-ctx
   :destroy-ctx entry/destroy-ctx
   :server-opts {:port config/port}})"]
      (com/$options
        {:create-ctx [:div
                      "A function that should be used to set up heavyweight components. The return value (usually a map) will be passed to "
                      [:code ":create-handler"]
                      " for your use in generating the request handler."]
         :destroy-ctx [:div
                       "A function that will be passed the result of "
                       [:code ":create-ctx"]
                       " for you to release any resources your app uses."]
         :create-handler [:div "A function that returns a ring request handler that will be called for each incoming request. The result of "
                          [:code ":create-ctx"]
                          " will be passed to this function."]
         :server-opts [:div
                       "A map of server options, passed to "
                       [:code "nsfw.server/start-aleph"]
                       "."]})])
   (com/fn-doc
     {:name "gen-stop-app"
      :ns "nsfw.server"}
     [:p
      "Call with result of "
      [:code "((nsfw.server/gen-start-app ...))"]
      " to shut down app."])

   [:p
    "See this guide's "
    [:a {:href "https://github.com/zk/nsfw/blob/master/devguide/src/clj/main.clj"} "main.clj"]
    " for an example of this in action."]

   [:p "There should be a single entry point into your app that can be called in development, staging, and production environments. Typically this is the same function you pass to Reup as the " [:code ":start-app"] " key."]

   [:h3 "Aleph & Netty"]
   [:p
    "NSFW is built on "
    [:a {:href "https://github.com/ztellman/aleph"} "Aleph"]
    ", a high-performance, asynchronous communication server for Clojure, built on Netty. Aleph can be used for handling requests synchronously or asynchornously, for multiple different protocols (Websockets, TCP, etc), and has been battle-tested serving billions of requests a day in high-QPS environments."]])

(def views
  {::intro {:render $render
            :route "/"
            :nav-title "Overview"}})
