(ns nsfw.scraper
  (:require [nsfw.util :as util]
            [aleph.http :as http]
            [clj-http.client :as hc]
            [clj-http.cookies :as cookies]
            [byte-streams :as bs]
            [clojure.string :as str]
            [reaver :refer [parse extract-from text attr attrs]]))

#_(defn fetch-source [url source-cache]
    (let [req (if (string? url)
                {:method :get
                 :url url}
                url)]
      (or (get source-cache req)
          (let [resp @(http/request
                        (merge
                          {:headers {"User-Agent" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"}
                           :connection-timeout 5000
                           :ignore-ssl-certs? true
                           :follow-redirects? true
                           :insecure? true}
                          req))]
            (prn resp)
            (-> resp
                :body
                bs/to-string)))))

(def cookie-store (cookies/cookie-store))

(defn fetch [url source-cache]
  (let [req (if (string? url)
              {:method :get
               :url url}
              url)
        resp (hc/request
               (merge
                 {:headers {"User-Agent" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"}
                  :connection-timeout 5000
                  :ignore-ssl-certs? true
                  :follow-redirects false
                  :insecure? true
                  :cookie-store cookie-store}
                 req))]
    resp))

(defn fetch-source [url response-cache]
  (or (get response-cache url)
      (-> (fetch url response-cache)
          :body
          bs/to-string)))

(defn verify-fetch [url response-cache]
  (println " * Response...")
  (let [resp (fetch url response-cache)]
    (util/pp (dissoc resp :body))
    (println)
    (println " * Body...")
    (->> resp
         :body
         bs/to-string
         (take 1200)
         (apply str)
         prn)))

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
                _ (prn "Running" spec)
                ress (process scrapers spec state)
                {:keys [new-specs results]} (group-by
                                              (fn [spec]
                                                (if (:url spec)
                                                  :new-specs
                                                  :results))
                                              ress)]
            (try
              (report spec ress (:context state))
              (catch Exception e
                (.printStackTrace e)))
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

(defn ensure-coll [o]
  (if (coll? o)
    o
    [o]))

(defn ensure-seq [o]
  (if (or (list? o)
          (vector? o)
          (seq? o))
    o
    [o]))

(defn extract [src main-sel sels]
  (apply
    extract-from
    (parse src)
    main-sel
    (->> sels
         keys
         (sort-by #(name %))
         vec)
    (->> sels
         (sort-by #(name (first %)))
         (mapcat (fn [[k v]] v))
         vec)))

(defn extract-og-tags [src]
  (->> (extract
         src
         "html"
         {:tags ["meta" attrs]})
       first
       :tags
       (map (fn [{:keys [property content]}]
              [property content]))
       (filter (fn [[property _]]
                 property))
       (filter (fn [[property _]]
                 (re-find #"^og:" property)))
       (map (fn [[p c]]
              [(keyword p)
               c]))
       (into {})))

(defn extract-link-tags [src]
  (->> (extract
         src
         "html"
         {:tags ["link" attrs]})
       first
       :tags
       (map (fn [{:keys [rel href]}]
              [rel href]))
       (filter (fn [[rel _]]
                 (and rel (not= "stylesheet" rel))))
       #_(filter (fn [[property _]]
                 (re-find #"^og:" property)))
       (map (fn [[p c]]
              [(keyword p)
               c]))
       (into {})))

(defn extract-meta-tags [src]
  (->> (extract
         src
         "html"
         {:tags ["meta" attrs]})
       first
       :tags
       (map (fn [{:keys [property content]}]
              [property content]))
       (filter (fn [[property _]]
                 property))
       (map (fn [[p c]]
              [(keyword p)
               c]))
       (into {})))



(defn script-contents [src]
  (->> src
       (re-seq #"<\s*script[^>]*>(.*)</\s*script\s*>")
       (map second)))

(defn verify-product [{:keys [id source-id origin-url name price images width height] :as product}]
  (let [res (and (not (empty? origin-url))
                 (not (empty? id))
                 (not (empty? source-id))
                 (not (empty? price))
                 (not (empty? images))
                 (->> images
                      (map #(and
                              (not (empty? (:url %)))
                              (:width %)
                              (:height %)))
                      (reduce #(and %1 %2)))
                 width
                 height)]
    (when-not res
      (println "!! ERROR, product missing information:")
      (prn (keys product))
      (util/pp product))
    res))

(defn test-category [{:keys [urls scrapers]}]
  (println "\n")
  (println "--- Test Category ---")
  (util/pp
    ((:category scrapers)
     (fetch-source
       (:example-cat-url urls)
       {})
     {}
     {}
     ))
  (println))

(defn test-level [scraper-key target & [default-props]]
  (println "\n")
  (println "--- Test" scraper-key "---")
  (let [url (or (-> target scraper-key :example-url)
                (-> target scraper-key :example-spec :url))
        props (or (-> target scraper-key :props)
                  default-props)
        ress ((-> target scraper-key :scraper)
              (fetch-source
                url
                {})
              props
              {}
              )]
    (util/pp ress)
    (println ">> COUNT:" (count ress)))
  (println))

(defn test-combo [target]
  (let [cat (-> ((-> target :category-page :scraper)
                 (fetch-source
                   (-> target :category-page :example-url)
                   {})
                 {}
                 {})
                first
                :props)
        product (if (:product-page target)
                  (-> ((-> target :product-page :scraper)
                       (fetch-source
                         (-> target :product-page :example-url)
                         {})
                       cat
                       {})
                      first
                      :props)
                  cat)]
    (verify-product product)
    (util/pp product)))

(defn test-chain [target keys]
  (let [product
        (loop [keys keys
               props {}]
          (if (empty? keys)
            props
            (let [scraper-key (first keys)
                  scraper-fn (-> target scraper-key :scraper)]
              (recur
                (rest keys)
                (-> (scraper-fn
                      (fetch-source
                        (-> target scraper-key :example-url)
                        {})
                      props
                      {})
                    first
                    :props)))))]
    (verify-product product)
    (util/pp product)))

(defn spit-to-temp [src]
  (spit "/tmp/out.html" src))

(defn write-tmp-html [scraper-key target]
  (spit-to-temp
    (fetch-source
      (-> target scraper-key :example-url)
      {})))

(defn target->scraper [{:keys [category-page
                               product-page
                               image-set-page
                               whats-new-urls id
                               specs]}]
  {:id id
   :scrapers (merge
               (when category-page
                 {:category-page (:scraper category-page)})
               (when product-page
                 {:product-page (:scraper product-page)})
               (when image-set-page
                 {:image-set-page (:scraper image-set-page)}))
   :specs (or specs
              (->> whats-new-urls
                   (map (fn [url]
                          {:url url
                           :props {}
                           :scraper-key :category-page}))))})

(defn capitalize-name [s]
  (when s
    (->> (str/split s #"\b")
         (map str/capitalize)
         str/join
         str/trim)))

(defn cat-spec
  [[url & categories]]
  {:url url
   :scraper-key :category-page
   :props {:categories (map name categories)}})

(defn price-str->num [price-str]
  (when price-str
    (let [price-num (-> price-str
                        (str/replace #"[^0-9]*" "")
                        (Integer/parseInt))
          price-num (if (.contains price-str ".")
                      price-num
                      (* price-num 100))]
      price-num)))
