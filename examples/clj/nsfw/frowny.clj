(ns nsfw.frowny
  (:use [clojure.pprint])
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]))

(defn read-body
  "Turn a HttpInputStream into a string."
  [request]
  (if (string? (:body request))
    (:body request)
    (when (and (:content-length request)
               (> (:content-length request) 0))
      (let [buf (byte-array (:content-length request))]
        (.read (:body request) buf 0 (:content-length request))
        (String. buf)))))

(def frownies (atom []))

(defn update-frownies! [new-frownies]
  (reset! frownies new-frownies))

(server/start :entry (webapp/routes
                       [""] (webapp/cs :examples
                                       :entry :nsfw.frowny
                                       :css :frowny
                                       :data {:frownies @frownies})
                       ["update-frownies"] (fn [r]
                                             (update-frownies!
                                              (-> r
                                                  read-body
                                                  read-string))
                                             {:status 200})))