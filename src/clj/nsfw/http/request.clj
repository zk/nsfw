(ns nsfw.http.request
  "Utilities for working with Ring requests")

(defn decode-body [content-length body]
  (when (and content-length
             (> content-length 0))
    (let [buf (byte-array content-length)]
      (.read body buf 0 content-length)
      (.close body)
      (String. buf))))

(defn response-body
  "Turn a HttpInputStream into a string."
  [{:keys [content-length body] :as req}]
  (if (string? body)
    body
    (decode-body content-length body)))
