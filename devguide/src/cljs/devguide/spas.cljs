(ns devguide.spas
  (:require [nsfw.util :as util]
            [nsfw.page :as page]
            [devguide.common :as com]))

(defn $render [!state bus]
  [:div.dg-section
   [:h1 "Single Page Applications"]
   [:p "When developing SPAs there are a few things you'll need to think about differently than when designing traditional statically rendered or progressive-enhancement apps."]
   [:p "There are a lot of nice things about moving rendering to the frontend."]
   [:h2 "App State & Event Lifecycle"]
   [:p "Code organization and coordination is accomplished via two mechanisms: a single, mutable reference that contains the renderable state of your application, and a shared event bus that is used to respond to events (both UI and user-generated)."]
   [:p "This organization allows you to build composible application components that are easy to test in isolation."]
   [:p "App state is represented as a single, top-level Reagent atom, which is used by rendering functions to display content on the page. This atom is where you'll put all information needed to render a given state of your page."]
   [:h3 "Core Setup"]
   [:p "In most cases, a response is rendered by the backend, including assets (css, js, etc) and configuration data (ENV) in serialized "
    [:a {:href "https://github.com/cognitect/transit-format"} "Transit"]
    " format. Based on the config data an entry point into your app is called, which:"]
   [:ol
    [:li "Sets up the initial app state based on config data, and data contained in the URL."]
    [:li "Sets up the event bus to respond to externally-sourced, asynchronous events."]
    [:li "Renders the page based on app state"]]
   [:p "Here's an example of this typical setup:"]
   [:pre
    ]
   [:h2 "URLs & Request Rendering"]
   [:p "Interacting with page URLs is done via the "
    [:a {:href ""} "HTML5 history API"]]
   [:p "Request routing is done using " [:a {:href "https://github.com/juxt/bidi"} "Bidi"]
    ", which allows for specifying routes with data."]

   (com/fn-doc
     {:name "push-path"
      :ns "nsfw.page"}
     [:div
      [:p "Push a path onto the history stack without navigating to a new page."]
      [:pre
       (util/pp-str '(page/push-path "/foo/" some-var "/bar"))]])

   (com/fn-doc
     {:name "pathname"
      :ns "nsfw.page"}
     [:div
      [:p "Returns the path compnent of the current URL."]
      [:pre
       (util/pp-str '(page/pathname))
       ";;=> "
       (page/pathname)]])

   (com/fn-doc
     {:name "fq-url"
      :ns "nsfw.page"}
     [:div
      [:p "Generate a fully-qualified (protocol, host, port, etc) URL based on arguments."]
      [:pre
       (util/pp-str '(page/fq-url "/the/" "quick/brown" "/fox"))
       ";;-> "
       (page/fq-url "/the/" "quick/brown" "/fox")]])])

;; Async

(def views
  {::spas {:render $render
           :route "/spas"
           :nav-title "Single Page Apps"}})
