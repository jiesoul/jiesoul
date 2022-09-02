(ns jiesoul.router
  (:require [clojure.java.io :as io]
            [reitit.middleware :as middleware]
            [jiesoul.handlers.auth :as auth]
            [jiesoul.handlers.user :as user]
            [jiesoul.middleware.auth :as auth-mw]
            [jiesoul.middleware :as mw]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]))

(def asset-version "1")

(defn default-handler [req]
  {:status 200 
   :body "this is default handler"})

(defn routes [db]
  (ring/ring-handler
   (ring/router [["/swagger.json"
                  {:get {:no-doc true
                         :swagger {:info {:title "my-api"}} ;; prefix for all paths
                         :handler (swagger/create-swagger-handler)}}]

                 ["/api/v1"
                  ["/login" {:swagger {:tags ["Auth"]}
                             :post {:summary "User Login"
                                    :parameters {:body {:username string?, :password string?}}
                                    :handler (auth/login db)}}]

                  ["/logout" {:swagger {:tags ["Auth"]}
                              :post {:summary "User Logout"
                                     :parameters {:header {:authorization string?}}
                                     :handler (auth/logout db)}}]


                  ["/users"
                   {:swagger {:tags ["users"]}}

                   ["/" {:get {:summary "get users"
                               :middleware [[auth-mw/wrap-auth db "user"]]
                               :parameters {:header {:authorization string?}}
                               :handler (user/get-users db)}

                         :post {:summary "create new user"
                                :parameters {:header {:authorization string?}
                                             :body {:username string?
                                                    :password string?
                                                    :email string?
                                                    :age int?
                                                    :roles string?
                                                    :nickname string? 
                                                    :birthday string?}}
                                :handler (user/create-user! db)}}]

                   ["/:id" {:get {:summary "get a user"
                                  :middleware [[auth-mw/wrap-auth db "user"]]
                                  :parameters {:header {:authorization string?}
                                               :path {:id int?}}
                                  :handler (user/get-user db)}

                            :put {:summary "update a user info"
                                  :middleware [[auth-mw/wrap-auth db "user"]]
                                  :parameters {:header {:authorization string?}
                                               :path {:id int?}}
                                  :handler default-handler}

                            :delete {:summary "delete a user"
                                     :middleware [[auth-mw/wrap-auth db "user"]]
                                     :parameters {:header {:authorization string?}
                                                  :path {:id int?}}
                                     :handler default-handler}}
                    ["/update-password" {:post {:parameters {:body {:old-pass string?
                                                                    :new-pass string?
                                                                    :con-pass string?}}
                                                :handler default-handler}}]]]

                  ["/files"
                   {:swagger {:tags ["files"]}}

                   ["/upload" {:post {:summary "upload a file"
                                      :parameters {:multipart {:file multipart/temp-file-part}
                                                   :headers {:authorization string?}}
                                      :responses {200 {:body {:file multipart/temp-file-part}}}
                                      :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                                 {:status 200
                                                  :body {:file file}})}}]

                   ["/download" {:get {:summary "downloads a file"
                                       :swagger {:produces ["image/png"]}
                                       :parameters {:headers {:authorization string?}}
                                       :handler (fn [_]
                                                  {:status 200
                                                   :headers {"Content-Type" "image/png"}
                                                   :body (-> "reitit.png"
                                                             (io/resource)
                                                             (io/input-stream))})}}]]]]

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
                                     multipart/multipart-middleware

                                     mw/exception-middleware]}
                 :exception pretty/exception})

   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/api-docs/v1"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "未找到"})
      :method-not-allowed (constantly {:status 405, :body "非法的调用"})}))))
