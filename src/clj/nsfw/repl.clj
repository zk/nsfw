(ns nsfw.repl
    (:require [clojure.tools.nrepl.server :as repl]))

(defn start [& {:keys [port]}]
  (repl/start-server :port port))