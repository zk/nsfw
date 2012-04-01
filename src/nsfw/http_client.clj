(ns nsfw.http-client
  (:use [clj-http.cookies :only (wrap-cookies)])
  (:require [clj-http.client :as cl]
            [clj-http.core :as core]))

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
      cl/wrap-input-coercion
      cl/wrap-output-coercion
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


