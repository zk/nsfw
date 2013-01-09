(ns {{name}}.server
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]))

(server/start :entry (webapp/routes
                      [""] (webapp/cs :app
                                      :css [:bootstrap.min
                                            :bootstrap-responsive.min
                                            :app])))