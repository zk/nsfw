(ns nsfw.workout
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp])
  (:import [java.util Date]))

(def tw-key "omKhSwwjUR60OeoVukj2nw")

(def tw-secret "tAB0jkD3J8LzxgnaW8WtmSUxlwRqCoWmmKJTzMbag")

(server/start
 :entry (webapp/routes
         [""] (webapp/cs :examples
                         :css :workout
                         :entry :nsfw.workout)))