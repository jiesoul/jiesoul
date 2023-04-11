(ns frontend.http 
 (:require [frontend.util :as f-util]
           [ajax.core :as ajax :refer []]))

(defn get-headers [db]
  (let [token (get-in db [:token])
        ret (cond-> {:Accept "application/json" :Content-Type "application/json"}
                    token (assoc :x-token token))
        _ (f-util/clog "get-headers, ret" ret)]
    ret))

(defn http [method db uri data on-success on-failure]
  (f-util/clog "http, uri" uri)
  (let [xhrio (cond-> {:debug true
                       :method method
                       :uri uri 
                       :headers (get-headers db)
                       :format (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keyword? true})
                       :on-success [on-success]
                       :on-failure [on-failure]}
                      data (assoc :params data))]
    {:http-xhrio xhrio
     :db db}))

(def http-post (partial http :post))
(def http-get (partial http :get))
(def http-update (partial http :update))
(def http-put (partial http :put))
(def http-patch (partial http :patch))