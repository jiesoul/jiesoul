(ns backend.util.resp-util
  (:require [ring.util.response :as resp]))

(defn ok [data & message]
  (resp/response {:status  200
                  :message message
                  :data data}))

(defn redirect [url & data]
  (resp/redirect {:status  302
                  :headers {"Location" url}
                  :data data}))

(defn bad-request [message & error]
  (resp/bad-request {:status  400
                     :message message
                     :error    error}))

(defn not-found [message & error]
  (resp/not-found {:status  404
                   :message message
                   :error    error}))


