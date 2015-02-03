(ns nsfw.reup
  "Utilities for supporting a clojure.tools.namespace reloading dev
  lifecycle."
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.string :as str]))

(defn exception? [e]
  (isa? (type e) Exception))

(defn ns-for-sym [sym]
  (when (.contains (str sym) "/")
    (-> sym
        str
        (str/split #"/")
        first
        symbol)))

(defn setup
  "Helper for initializing a clojure.tools.namespace dev
  lifecycle. See
  http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded
  for more info.

  This will return a function that, when called, will stop the
  current environment, reload all namespaces, and start a new
  environment.

  Params:
  * `start-app-sym` -- FQ symbol of a no-arg function which
  starts the environment
  * `stop-app-sym` -- FQ symbol of a 1-arg
  function which stops the environment. The result of calling the
  start app function is passed in as it's first parameter
  * `test-regex` -- Run tests after reload for all namespaces matching"

  [{:keys [start-app-sym stop-app-sym tests-regex]}]
  (intern 'user 'reup-app nil)
  (require (ns-for-sym start-app-sym) :reload)
  (require (ns-for-sym stop-app-sym) :reload)

  (when-not (resolve start-app-sym)
    (throw (Exception. (str "Can't resolve start-app-sym: " start-app-sym))))

  (when-not (resolve stop-app-sym)
    (throw (Exception. (str "Can't resolve stop-app-sym: " stop-app-sym))))

  (fn []
    (time
      (do
        (binding [*ns* (find-ns 'user)]
          (do
            (try
              (@(resolve stop-app-sym) @(resolve 'user/reup-app))
              (catch Exception e
                (println "Exception stopping app:" e)))))
        (alter-var-root (resolve 'user/reup-app) (constantly nil))
        (let [res (repl/refresh)]
          (println "EXCEPTION" (exception? res))
          (when (exception? res)
            (throw res)))
        (binding [*ns* (find-ns 'user)]
          (alter-var-root (resolve 'user/reup-app)
            (constantly (@(resolve start-app-sym)))))
        (.mkdir (java.io.File. "./.livereload"))
        (spit ".livereload/update.rf" (System/currentTimeMillis))
        (when tests-regex
          (clojure.test/run-all-tests tests-regex))))))
