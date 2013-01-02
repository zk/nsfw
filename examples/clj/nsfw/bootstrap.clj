(ns nsfw.bootstrap
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]))

(server/start :entry (webapp/routes
                       [""] (webapp/cs :examples
                                       :css :bootstrap
                                       :entry :nsfw.bootstrap)))