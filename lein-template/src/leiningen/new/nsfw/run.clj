(ns {{name}}.run
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]))

(defn -main [& args]
  (server/start :entry (webapp/routes
                        [""] (webapp/cs :app
                                        :css [:bootstrap.min
                                              :bootstrap-responsive.min
                                              :app]))))