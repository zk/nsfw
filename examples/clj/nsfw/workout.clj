(ns nsfw.workout
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp])
  (:import [java.util Date]))

(server/start
 :entry (webapp/routes
         [""] (webapp/cs :examples
                         :css :bootstrap
                         :entry :nsfw.workout)))