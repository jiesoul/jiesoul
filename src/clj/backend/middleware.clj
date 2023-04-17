(ns backend.middleware
  (:require [expound.alpha :as expound]
            [reitit.ring.middleware.exception :as exception]
            [clojure.tools.logging :as log]))

(defn wrap-cors
  "Wrap the server response with new headers to allow Cross Origin."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "x-requested-with,Authorization,Content-Type")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "*")))))


(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

;; (defn error [status message exception request]
;;   (log/debug "error info: " (:ex-data exception))
;;   (resp/bad-request
;;    {:eroor  {:code status
;;              :message message
;;              :exception (.getClass exception)
;;              :details {ex-data exception}
;;              :uri (:uri request)}}))

;; (defn coercion-error-handler [status]
;;   (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})]
;;     (fn [exception request]
;;       (log/error
;;        (printer (-> exception ex-data :problems)))
;;       (case status
;;         400 (error status "客户端错误" exception request)
;;         500 (error status "服务端错误" exception request)))))

(defn handler [message exception request]
  (let [error {:eroor  {:code 500
                        :message message
                        :exception (.getClass exception)
                        :data {ex-data exception}
                        :uri (:uri request)}}
        _ (log/error "found error: " error)]
    error))

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
     ::exception/default (partial handler "未知错误")

     ::exception/wrap (fn [handler e request]
                        (log/error "ERROR " (pr-str (:uri request)))
                        (handler e request))})))