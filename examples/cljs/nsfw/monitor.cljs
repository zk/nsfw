(ns nsfw.monitor.app
  (:use [nsfw.dom :only [$]])
  (:require [nsfw.dom :as d]
            [nsfw.util :as u]
            [goog.net.XhrIo :as xhrio]))

(comment

  (def body ($ "body"))

  (def uptime ($ [:span.uptime]))

  (defn stats [stats-atom]
    (let [el (-> ($ [:div.stats])
                 (d/style {:color :#ccc
                           :position :absolute
                           :right :0px
                           :bottom :0px
                           :padding :10px}))
          render (fn [{:keys [uptime freemem totalmem]}]
                   (-> el
                       d/empty
                       (d/append [:div.uptime (or uptime 0)])
                       (d/append [:div.freemem (or freemem 0)])
                       (d/append [:div.totlamem (or totalmem 0)])))]
      (d/bind stats-atom (fn [o n] (render n)))
      el))

  (defn view [data-atom render]
    (let [a (atom (render @data-atom))]
      (d/bind data-atom (fn [o n]
                          (d/replace @a (reset! a (render n)))))
      @a))

  (defn render-stat [val]
    (-> ($ [:div.bar ""])
        (d/style {:height (str (* 100 val) "%")
                  :width :1px
                  :display :inline-block
                  :background-color :black})))

  (defn normalize [coll]
    (let [max (apply max coll)]
      (map #(/ % max) coll)))

  (defn render-stats [stats]
    (let [fs (reverse (:fs stats))
          max (:free-max stats)
          uptime (:uptime stats)]
      ($ [:div.stats
          [:div.label
           (format "%dm"
                   (/ uptime
                      (* 1000 60)))]
          " "
          [:div.bar-chart
           [:div.bar-chart-shim]
           (->> fs
                (map #(/ % max))
                (map render-stat))]
          [:div.free-num
           (format "%.1f free"
                   (/ (first (:fs stats))
                      (* 1024 1024)))]])))

  (defn poll [f path delta]
    (let [update (fn [update]
                   (try
                     (d/ajax :path path
                             :success f)
                     (finally (u/timeout #(update update) delta))))]
      (update update)))

  (defn link-to [atom path delta]
    (poll (fn [res] (reset! atom res))
          path
          delta)
    atom)

  (defn header []
    ($ [:div.header
        [:h1 "Monitor!"]]))

  (def stats-atom (atom nil))

  (-> body
      (d/append (header))
      (d/append (view stats-atom render-stats)))

  (defn update-stats [{:keys [freemem uptime]}]
    (fn [{:keys [free-max fs] :as stats}]
      (-> stats
          (assoc :free-max (if (> free-max freemem)
                             free-max
                             freemem))
          (assoc :fs (let [out (cons freemem fs)]
                       (if (> (count out) 50)
                         (drop-last out)
                         out)))
          (assoc :uptime uptime))))

  (poll (fn [res]
          (swap! stats-atom (update-stats res)))
        "/atoms" 1000)


  (defn update [atom]
    (reset! atom (.random js/Math))
    (u/timeout #(update atom) 1000))

  (def val1 (atom "hello world 1"))

  (def h1 ($ [:h1 @val1]))

  (d/bind val1 (fn [o n]
                 (d/text h1 n)))

  (update val1)

  (d/append body h1)



  (def val2 (atom "hello world 2"))

  (def h2 ($ [:h1 @val2]))

  (d/bind-el val2 h2 (fn [from to el]
                       (d/text el to)))

  (update val2)

  (d/append body h2)



  (def val3 (atom "hello world 3"))

  (def h3 ($ [:h1 @val3]))

  (defn update-text [from to el]
    (d/text el to))

  (d/bind-el val3 h3 update-text)

  (update val3)

  (d/append body h3)



  (def val4 (atom [{:todo "Milk" :due "tomorrow"}
                   {:todo "Coffee" :due "thursday"}
                   {:todo "Brains" :due "21 days later"}]))

  (defn render-todo [{:keys [todo due]}]
    (-> ($ [:div
            [:span.todo todo]
            " | "
            [:span.due due]])
        (d/style {:margin-left (str (* 100 (rand)) "px")})))

  (defn render-todos [from to el]
    (-> el
        d/empty
        (d/append (map render-todo to))))

  (def todos-el ($ [:div.todos]))

  (d/bind-el val4 todos-el render-todos)

  (u/interval
   (fn []
     (swap! val4 #(drop-last
                   (cons {:todo (rand)
                          :due (rand)} %))))
   1000)

  (d/append body todos-el)


)

(defn main [])