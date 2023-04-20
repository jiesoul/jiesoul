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

(defn error [status message]
  (fn [e request]
    (log/error "error info: " (ex-data e))
    {:status status
     :message message
     :error {:exception (.getClass e)
             :ex-data (ex-data e)
             :uri (:uri request)}}))

(defn coercion-error-handler [status]
  (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})]
    (fn [exception]
      (printer (-> exception ex-data :problems))
      (case status
        400 (error status "请求验证错误") 
        500 (error status "响应验证错误")
        (error status "未知错误")))))

(defn handler [message exception request]
  {:status 500
   :message message
   :body  {:status 500
           :message message
           :exception (.getClass exception)
           :data (ex-data exception)
           :uri (:uri request)}})

(defn default-handler
  [^Exception e _]
  {:status 500
   :error {:type "exception"
          :class (.getName (.getClass e))}})

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

    ;;  :reitit.coercion/request-coercion (coercion-error-handler 400)
    ;;  :retiit.coercion/response-coercion (coercion-error-handler 500)

     ;; override the default handler
     ::exception/default (partial handler "default")

     ::exception/wrap (fn [handler e request]
                        (log/error "ERROR " (pr-str (:uri request)))
                        (handler e request))
     })))