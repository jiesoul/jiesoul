(ns jiesoul.middleware
  (:require [reitit.ring.middleware.exception :as exception]))

(defn wrap [handler id]
  (fn [req]
    (handler (update req ::acc (fnil conj []) id))))

(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  {:status 500
   :body {:message message
          :exception (.getClass exception)
          :data {ex-data exception}
          :uri (:uri request)}})

(def exception-middleware
  (exception/create-exception-middleware
   (merge 
    exception/default-handlers
    {;; ex-data with :type ::error
     ::error (partial handler "error")
     
     ;; ex-data with ::exception or ::failure
     ::exception (partial handler "exception")
     
     ;; SQLException and all it's child classes
     java.sql.SQLException (partial handler "sql-exception")
     
     ;; override the default handler
     ::exception/default (partial handler "default")
     
     ::exception/wrap (fn [handler e request]
                        (println "ERROR " (pr-str (:uri request)))
                        (handler e request))})))