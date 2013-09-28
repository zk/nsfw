(ns nsfw
  (:use (ring.middleware file file-info resource params nested-params
                         keyword-params multipart-params session)
        [ring.middleware.session.memory :only (memory-store)]
        [ring.middleware.session.cookie :only (cookie-store)]
        [ring.middleware.reload :only (wrap-reload)]
        [clojure.pprint])
  (:require [nsfw.env :as env]
            [nsfw.app :as app]
            [nsfw.server :as server]
            [nsfw.html :as html]
            [nsfw.http :as http]
            [clojure.string :as str]
            [clojure.tools.nrepl.server :as repl]
            [ring.middleware.reload-modified :as reload]
            [net.cgrand.moustache :as moustache]
            [cheshire.core :as cheshire]))

;; -- Environment--

(def env-str env/str)
(def env-int env/int)
(def env-bool env/bool)

(defn start-repl [port]
  (repl/start-server :port port))

(defn serve-routes [h path !components]
  (fn [r]
    (if path
      (if-let [res ((app/load-routes path !components) r)]
        res
        (h r))
      (h r))))

(defn file-exists? [path]
  (.exists (java.io.File. path)))

(defn catch-all [r]
  (let [path "resources/public/404.html"]
    {:status 404
     :type "text/html;charset=utf-8"
     :body (if (file-exists? path)
             (slurp path)
             "404")}))

(defn handle-errors [h debug-exceptions]
  (fn [r]
    (if debug-exceptions
      ((app/debug-exceptions h true) r)
      (try
        (h r)
        (catch Exception e
          (let [path "resources/public/500.html"]
            {:status 500
             :type "text/html;charset=utf-8"
             :body (if (file-exists? path)
                     (slurp path)
                     "500")}))))))

(defonce !components (atom {}))

(defn app [& opts]
  (let [{:keys [repl-port
                server-port
                entry-point
                app-nss
                session
                autoload
                debug-exceptions
                session-store]}
        (apply hash-map opts)]
    (try (when repl-port
           (start-repl repl-port))
         (catch Exception e
           (println "Can't start REPL on port" repl-port)
           (println e)))
    (let [sess (app/session-store (or session-store :encrypted-cookie))]
      (server/start :entry (moustache/app
                            (handle-errors debug-exceptions)
                            (wrap-reload :dirs ["src/clj"])
                            wrap-file-info
                            (wrap-file "resources/public" {:allow-symlinks? true})
                            wrap-params
                            wrap-nested-params
                            wrap-keyword-params
                            (wrap-session (or session {}))
                            (serve-routes autoload !components)
                            catch-all)
                    :port server-port))))

(def transform-components (html/mk-transformer !components))

(defn render-html [& body]
  (when-not (->> body (filter identity) empty?)
    (-> body
        transform-components
        html/html5
        http/html)))

(defn render-edn [body]
  {:headers {"Content-Type" "application/edn;encoding=utf-8"}
   :status 200
   :body (pr-str body)})

(defn render-json [body]
  {:headers {"Content-Type" "application/json;encoding=utf-8"}
   :status 200
   :body (cheshire/generate-string body)})

(defmacro defcomp
  "Define a html component"
  [name & rest]
  `(defn ~(with-meta name (assoc (meta name) :nsfw/comp-tag (keyword name)))
     ~@rest))

(defn parse-route [route]
  (cond
   (string? route) {:path route}
   (vector? route) (let [[method path] route]
                     {:method method
                      :path path})
   (map? route) route))


(defn parse-route-name [route]
  (let [name (:path route)
        name (-> name
                 (str/replace #"/" "")
                 (str/replace #":" "-")
                 (str/replace #"\*" "all"))
        name (if (empty? name)
               "index"
               name)
        name (str (or (:method route) "any") "-" name)
        name (symbol name)]
    name))

(defmacro defroute
  "Define a route var"
  [route & rest]
  (let [route# (parse-route route)
        name# (parse-route-name route#)]
    `(defn ~(with-meta name# (assoc (meta name#) :nsfw/route route#))
       ~@rest)))

(defmacro defhtml
  "Define a route var"
  [route params & rest]
  (let [route# (parse-route route)
        name# (parse-route-name route#)]
    `(defn ~(with-meta name# (assoc (meta name#) :nsfw/route route#)) ~params
       (render-html ~@rest))))

(defmacro defmiddleware
  "Define a route var"
  [name pred & mws]
  `(def ~(with-meta name (assoc (meta name) :nsfw/middleware true))
     {:pred ~pred
      :middleware [~@mws]}))