(ns nsfw.scraper
  (:require [nsfw.util :as util]
            [aleph.http :as http]
            [byte-streams :as bs]))

(defn fetch-source [url source-cache]
  (or (get source-cache url)
      (-> @(http/get url
             {:headers {"User-Agent" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"}})
          :body
          bs/to-string)))

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
          [{:error (str "Exception during scrape: " e)}])))))

(defn run-job [scrapers spec state continue? report]
  (->> (if (continue?)
         (let [ress (process scrapers spec state)]
           (->> ress
                (mapcat (fn [res]
                          (if (:url res)
                            (if-not (get scrapers (:scraper-key res))
                              [res]
                              (trampoline
                                run-job
                                scrapers
                                res
                                state
                                continue?
                                report))
                            [res])))))
         [{:error "Scrape halted"}])
       (#(do (report spec %) %))))


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
