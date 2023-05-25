(ns backend.middleware
  (:require [clojure.tools.logging :as log]
            [reitit.ring.middleware.exception :as exception]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn wrap-cors-middeleware
  "Wrap the server response with new headers to allow Cross Origin."
  [handler]
  ;; (wrap-cors handler 
  ;;            :access-control-allow-origin [*]
  ;;            :access-control-allow-methods [:get :put :post :delete :patch :options])
  
    (fn [request]
      (let [response (handler request)]
        (-> response
            (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
            (assoc-in [:headers "Access-Control-Allow-Headers"] "Content-Type, Authorization")
            (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELET,PATCH,OPTIONS,HEADERS")))))




(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  (log/error "ERROR uri: " (pr-str (:uri request)))
  (log/error "ERROR trace: " exception) 
  {:status 500 
   :body  {:message message 
           :exception (.getClass exception)
           :data (ex-data exception)
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
     ::exception/default (partial handler "unknown error")

     ::exception/wrap (fn [handler e request] 
                        (handler e request))})))