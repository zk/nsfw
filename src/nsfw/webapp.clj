(ns nsfw.webapp
  (:require [net.cgrand.moustache :as moustache]
            [nsfw.middleware :as nm]))

(defmacro routes [& body]
  `(moustache/app
    nm/wrap-web-defaults
    ~@body))
