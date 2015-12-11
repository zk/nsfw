(ns devguide.config
  (:require [nsfw.env :as env]))

(def port (or (env/int :port) 5000))

(def session-domain (or (env/str :session-domain) "devguide"))

(def session-key (or (env/str :session-key) "1c0c68676d399818f1421437794652e7"))
