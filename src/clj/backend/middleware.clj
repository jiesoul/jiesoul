(ns backend.middleware
  (:require [clojure.tools.logging :as log]
            [expound.alpha :as expound]
            [reitit.ring.middleware.exception :as exception]
            [ring.util.response :as resp]))

(defn wrap-cors
  "Wrap the server response with new headers to allow Cross Origin."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "*")))))


(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  (log/error "Error message: " message)
  (log/error "Error: " exception) 
  {:status 500
   :message message
   :error  {:status 500
            :message message
            :exception (.getClass exception)
            :data (ex-data exception)
            :uri (:uri request)}})

(defn coercion-error-handler [status]
  (log/error "coercion error: " status)
  (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})]
    (fn [exception request]
      (printer (-> exception ex-data :problems))
      (case status
        400 (handler "request-coercion-exception" exception request)
        500 (handler "response-coercion-exception" exception request)
        (handler "未知错误" exception request)))))

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

     :reitit.coercion/request-coercion (partial coercion-error-handler 400)
     :retiit.coercion/response-coercion (partial coercion-error-handler 500)

     ;; override the default handler
     ::exception/default (partial handler "default")

     ::exception/wrap (fn [handler e request]
                        (log/error "ERROR " (pr-str (:uri request))) 
                        (log/error "ERROR: " e)
                        (handler e request))
     })))