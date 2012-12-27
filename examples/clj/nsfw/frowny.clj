(ns nsfw.frowny
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]))


(server/start :entry (webapp/routes
                      [""] (webapp/cs :examples
                                      :entry :nsfw.frowny
                                      :css :frowny)))