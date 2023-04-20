(ns frontend.http 
 (:require [ajax.core :as ajax]
           [frontend.util :as f-util]
           [reitit.frontend.easy :as rfe]))

(def ^:private api-base "http://localhost:8080/api/v1")

(defn api-uri [route & s]
  (apply str api-base route s))

(defn get-headers [db]
  (let [token (get-in db [:token])
        ret (cond-> {:Accept "application/json" :Content-Type "application/json"}
                    token (assoc :authorization (str "Token " token)))
        _ (f-util/clog "get-headers, ret" ret)]
    ret))

(defn convert-get [data]
  )

(defn http [method db uri data on-success on-failure]
  (f-util/clog "http, uri" uri)
  (let [xhrio (cond-> {:debug true
                       :method method
                       :uri uri
                       :headers (get-headers db)
                       :format (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success [on-success]
                       :on-failure [on-failure]}
                      data (assoc :params data))]
    {:http-xhrio xhrio
     :db db}))

(def http-post (partial http :post))
(def http-get (partial http :get))
(def http-delete (partial http :delete))
(def http-put (partial http :put))
(def http-patch (partial http :patch))