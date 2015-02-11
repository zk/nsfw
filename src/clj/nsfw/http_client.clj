(ns nsfw.http-client
  (:use [clj-http.cookies :only (wrap-cookies)])
  (:require [clj-http.client :as cl]
            [clj-http.core :as core])
  (:import (java.io InputStream File)
           (java.net URL UnknownHostException)
           (org.apache.http.entity ByteArrayEntity InputStreamEntity
                                   FileEntity StringEntity)))

(defn wrap-input-coercion [client]
  (fn [{:keys [body body-encoding length] :as req}]
    (if body
      (cond
       (string? body)
       (client (-> req (assoc :body (StringEntity. body (or body-encoding
                                                            "UTF-8"))
                              :character-encoding (or body-encoding
                                                      "UTF-8"))))
       (instance? File body)
       (client (-> req (assoc :body (FileEntity. body (or body-encoding
                                                          "UTF-8")))))
       (instance? InputStream body)
       (do
         (when (or (nil? length) (neg? length))
           (throw
            (Exception. ":length key is required for InputStream bodies")))
         (client (-> req (assoc :body (InputStreamEntity. body length)))))

       (instance? (Class/forName "[B") body)
       (client (-> req (assoc :body (ByteArrayEntity. body))))

       :else
       (client req))
      (client req))))

(defn wrap-request
  "Everything in default except exceptions on non 2xx responses. See
  clj-http.client."
  [request]
  (-> request
      cl/wrap-query-params
      cl/wrap-user-info
      cl/wrap-url
      cl/wrap-redirects
      cl/wrap-decompression
      wrap-input-coercion
      cl/wrap-basic-auth
      cl/wrap-accept
      cl/wrap-accept-encoding
      cl/wrap-content-type
      cl/wrap-form-params
      cl/wrap-method
      wrap-cookies
      cl/wrap-unknown-host))

(def #^{:doc
        "Executes the HTTP request corresponding to the given map and returns
   the response map for corresponding to the resulting HTTP response.

   In addition to the standard Ring request keys, the following keys are also
   recognized:
   * :url
   * :method
   * :query-params
   * :basic-auth
   * :content-type
   * :accept
   * :accept-encoding
   * :as

  The following additional behaviors over also automatically enabled:
   * Exceptions are thrown for status codes other than 200-207, 300-303, or 307
   * Gzip and deflate responses are accepted and decompressed
   * Input and output bodies are coerced as required and indicated by the :as
     option."}
  request
  (wrap-request #'core/request))

(defmacro with-connection-pool [& body]
  `(cl/with-connection-pool ~@body))
