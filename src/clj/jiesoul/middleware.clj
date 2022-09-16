(ns jiesoul.middleware
  (:require [expound.alpha :as expound]
            [reitit.ring.middleware.exception :as exception]))

(defn wrap [handler id]
  (fn [req]
    (handler (update req ::acc (fnil conj []) id))))

(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  {:eroor  {:code 500
            :message message
            :exception (.getClass exception)
            :details {ex-data exception}
            :uri (:uri request)}})

(defn coercion-error-handler [status]
  (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})
        handler (exception/create-coercion-handler status)]
    (fn [exception request]
      (printer (-> exception ex-data :problems))
      (handler exception request))))

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

     :reitit.coercion/request-coercion (coercion-error-handler 400)
     :retiit.coercion/response-coercion (coercion-error-handler 500)

     ;; override the default handler
     ::exception/default (partial exception/wrap-log-to-console exception/default-handler)

     ::exception/wrap (fn [handler e request]
                        (println "ERROR " (pr-str (:uri request)))
                        (handler e request))})))