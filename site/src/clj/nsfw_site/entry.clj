(ns nsfw-site.entry
  (:require [nsfw.webapp :as webapp]))

(def routes
  (webapp/routes
   [""] (webapp/cs :app
                   :css [:bootstrap.min
                         :bootstrap-responsive.min
                         :nsfw-components
                         :app]
                   :google-maps true)))