(ns backend.util.resp-util
  (:require [ring.util.response :as resp]))

(defn ok [data]
  (resp/response {:status :ok
                  :data data}))

(defn failed [msg]
  (resp/bad-request {:status :failed
                     :msg msg}))