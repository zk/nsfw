(ns devguide.spas)

(defn $render [!state bus]
  [:div
   [:h1 "Single Page Applications"]])

(def views
  {::spas {:render $render
           :route "/spas"
           :nav-title "SPAs"}})
