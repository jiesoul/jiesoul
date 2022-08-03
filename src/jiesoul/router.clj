(ns jiesoul.router
  (:require [clojure.java.io :as io]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [selmer.parser :as html]
            [reitit.dev.pretty :as pretty]))

(def asset-version "1")
(defn template [data-page]
  (html/render-file "layout/default.html" {:paeg data-page}))

(defn routes [db]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}
                       :basePath "/"} ;; prefix for all paths
             :handler (swagger/create-swagger-handler)}}]
     ["/files"
      {:swagger {:tags ["files"]}}
      
      ["/upload"
       {:post {:summary "upload a file"
               :parameters {:multipart {:file multipart/temp-file-part}}
               :responses {200 {:body {:file multipart/temp-file-part}}}
               :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                          {:status 200
                           :body {:file file}})}}]

      ["/download"
       {:get {:summary "downloads a file"
              :swagger {:produces ["image/png"]}
              :handler (fn [_]
                         {:status 200
                          :headers {"Content-Type" "image/png"}
                          :body (-> "reitit.png"
                                    (io/resource)
                                    (io/input-stream))})}}]]

    ;;  ["/users" {:get (fn [{::r/keys [router]}]
    ;;                    {:status 200
    ;;                     :body (for [i (range 10)]
    ;;                             {:uri (-> router 
    ;;                                       (r/match-by-name ::user {:id i})
    ;;                                       (r/match->path {:iso "moly"}))})})}]
    ;;  ["/users/:id" 
    ;;   {:name ::user
    ;;    :get (constantly {:status 200 :body "user..."})}]
     ]

    {:data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; query-params & form-params
                         parameters/parameters-middleware
                           ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                           ;; encoding response body
                         muuntaja/format-response-middleware
                           ;; exception handling
                         exception/exception-middleware
                           ;; decoding request body
                         muuntaja/format-request-middleware
                           ;; coercing response bodys
                         coercion/coerce-response-middleware
                           ;; coercing request parameters
                         coercion/coerce-request-middleware
                           ;; multipart
                         multipart/multipart-middleware]}
     :exception pretty/exception})

  ;;  (ring/router
  ;;   ["/api" {:middleware [#(wrap % :api)]}
  ;;    ["/ping" {:handler handler}]
  ;;    ["/public/*" (ring/create-resource-handler)]
  ;;    ["/hello" {:handler (fn [name] (resp/response (str "hello " name)))}]

  ;;    ["/admin" {:middleware [[wrap :admin]]}
  ;;     ["/db" {:middleware [[wrap :db]]
  ;;             :handler handler}]]]) 

   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/api-docs"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not Found 404"})
      :method-not-allowed (constantly {:status 405, :body "kosh"})}))))
