(ns nsfw.scraper
  (:require [nsfw.util :as util]
            [aleph.http :as http]
            [byte-streams :as bs]
            [clojure.string :as str]))

(defn fetch-source [url source-cache]
  (let [req (if (string? url)
              {:method :get
               :url url}
              url)]
    (or (get source-cache req)
        (-> @(http/request
               (merge
                 {:headers {"User-Agent" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"}
                  :connection-timeout 5000}
                 req))
            :body
            bs/to-string))))

(defn process [scrapers spec state]
  (let [{:keys [url scraper-key]
         parent-props :props} spec
         scraper (get scrapers scraper-key)]
    (if-not scraper
      [{:error (str "No scraper for type " (pr-str scraper-key))
        :spec spec}]
      (try
        (let [{:keys [source-cache context]} state
              {:keys [url]} spec
              src (fetch-source url source-cache)
              state (update-in state [:source-cache] assoc url src)
              ]
          (scraper src parent-props context))
        (catch Exception e
          [{:error (str "Exception during scrape: " e)
            :spec spec}])))))

(defn run-scrape [scrapers spec state continue-fn? report]
  (loop [specs [spec]
         out []]
    (let [continue? (continue-fn?)]
      (if-not continue?
        (concat out [{:error "Scrape halted"}])
        (if (empty? specs)
          out
          (let [spec (first specs)
                ress (process scrapers spec state)
                {:keys [new-specs results]} (group-by
                                              (fn [spec]
                                                (if (:url spec)
                                                  :new-specs
                                                  :results))
                                              ress)]
            (report spec ress (:context state))
            (recur
              (concat (rest specs) new-specs)
              (concat out results))))))))

(defn run-job [scrapers
               specs
               {:keys [state
                       continue-fn?
                       report-fn
                       job-id]
                :or {state {}
                     continue-fn? (constantly true)
                     report-fn (fn [_ _])
                     job-id (util/uuid)}}]
  (let [start-ts (util/now)]
    (->> specs
         (reduce
           (fn [out spec]
             (let [scrape (run-scrape scrapers spec state continue-fn? report-fn)
                   {:keys [results errors]} (group-by
                                              (fn [result]
                                                (if (:error result)
                                                  :errors
                                                  :results))
                                              scrape)
                   results (map :props results)]
               (-> out
                   (update-in [:results]
                     (fn [existing-results]
                       (if results
                         (concat existing-results results)
                         existing-results)))
                   (update-in [:errors]
                     (fn [existing-errors]
                       (if errors
                         (concat existing-errors errors)
                         existing-errors))))))
           {})
         (merge
           {:id job-id
            :seed-specs specs
            :start-ts start-ts
            :end-ts (util/now)}))))

(defn safe-println [& more]
  (.write *out* (str (->> more
                          (interpose " ")
                          (apply str))
                     "\n")))

(defn stdout-reporter [input output ctx]
  (safe-println ">" (:url input))
  (safe-println)
  (when (:error input)
    (safe-println "ERROR" (:error input)))
  (doseq [{:keys [url props scraper-key error]} output]
    (if error
      (safe-println "ERROR" error)
      (do
        (safe-println "  + url:" url)
        (safe-println "  + key:" (or scraper-key "NONE"))
        (safe-println "  + prs:")
        (safe-println (->> props
                           util/pp-str
                           (#(str/split % #"\n"))
                           (map-indexed
                             (fn [i s]
                               (str "    "
                                    (if (= i 0)
                                      "'"
                                      " ")
                                    s
                                    "\n")))
                           (apply str)))))
    (safe-println))
  (safe-println "---" (count output) "result(s)")
  (safe-println)
  (safe-println)
  (safe-println))


(comment
  (->> (run-job
         {:foo (fn [& args]
                 [{:props {:foo "bar"}}
                  {:props {:baz "bap"}}])}
         {:scraper-key :foo
          :url "http://www.zacharykim.com"
          :props {:some "props"}}
         {:source-cache {}
          :context {:this "context"}}
         (constantly true)
         prn)
       #_util/pp)


  (process
    {:foo (fn [& args]
            [{:props {:foo "bar"}}
             {:props {:baz "bap"}}])}
    {:type :foo
     :url "http://www.zacharykim.com"
     :props {:some "props"}}
    {:source-cache {}
     :context {:this "context"}}))
