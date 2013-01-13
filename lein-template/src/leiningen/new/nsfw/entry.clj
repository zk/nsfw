(ns {{name}}.entry
  (:require [nsfw.webapp :as webapp]))

(def routes
  (webapp/routes
   [""] (webapp/cs :app
                   :css [:bootstrap.min
                         :bootstrap-responsive.min
                         :app])))