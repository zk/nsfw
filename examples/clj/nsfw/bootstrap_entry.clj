(ns nsfw.bootstrap-entry
  (:require [nsfw.webapp :as webapp]))

(def routes
  (webapp/routes
   [""] (webapp/cs :examples
                   :css [:bootstrap.min
                         :bootstrap-responsive.min
                         :nsfw-components
                         :bootstrap]
                   :google-maps true
                   :entry :nsfw.bootstrap)))