(ns nsfw.monitor.server
  (:require [nsfw.server :as s]
            [nsfw.webapp :as webapp]
            [nsfw.html :as html])
  (:import [java.lang.management RuntimeMXBean ManagementFactory]
           [java.lang Runtime]))

(def rb (ManagementFactory/getRuntimeMXBean))

(def rt (Runtime/getRuntime))

(defn stats []
  {:uptime (.getUptime rb)
   :freemem (.freeMemory rt)
   :totalmem (.totalMemory rt)})

(s/start :entry
         (webapp/routes
           [""] (webapp/cs :monitor :entry 'nsfw.monitor.app)
           ["atoms"] (fn [{:keys [params]}]
                       {:headers {"Content-Type" "text/html"}
                        :body (pr-str (stats))})))