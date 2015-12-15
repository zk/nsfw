(ns devguide.request-handling)

(defn $render [!state bus]
  [:div.dg-section
   [:h1 "Request Handling"]
   [:p "A webapp must be able to specify code to handle incoming requests. This is done by routing requests to a set of handlers based on characteristics of the request, typically the HTTP method and URL path (however you're free to route requests to handlers however you choose)."]
   [:h2]])

(def views
  {::index {:render $render
            :route "/request-handling"
            :nav-title "Request Handling"}})
