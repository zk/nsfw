(ns nsfw.example
  (:use [nsfw.dom :only [$ append-to click style append mousemove attrs text]]
        [nsfw.util :only [log timeout]])
  (:require [nsfw.dom :as dom]))

(comment

  (defn circle []
    ($ [:svg:circle {:r "50"
                     :fill "red"}]))

  (def body ($ "body"))

  (append body ($ [:style {:type "text/css"}
                   "
* {
  padding: 0px;
  margin: 0px;
}

html, body {
  width: 100%;
  height: 100%;
  font-family: Helvetica, sans-serif;
}"]))



  (def h1 (let [timer-el ($ [:span "!!"])
                start (js/Date.)
                update (fn [update]
                         (let [now (js/Date.)
                               diff (/ (- (.getTime now) (.getTime start)) 1000)]
                           (dom/text timer-el diff)
                           (timeout #(update update) 1000)))]
            (update update)
            (-> ($ [:h1
                    timer-el
                    " "
                    "Δ"])
                (style {:position :absolute
                        :bottom :0px
                        :right :0px
                        :padding :10px
                        :font-size :20px})
                (append-to body))))
  )