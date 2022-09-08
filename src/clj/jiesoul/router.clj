(ns jiesoul.router
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
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
            [reitit.swagger-ui :as swagger-ui]
            [expound.alpha :as expound]))

(def token-regex #"^Token (.+)$")
(defn valid-token? [s] (re-matches token-regex s))
(s/def ::header-token
  (st/spec {:description "请求头部Token格式为：Token xxxxxxxxx"
            :spec (s/and string? valid-token?)}))
(expound/defmsg ::header-token "请求头部Token格式为：Token xxxxxxxxx")

(def query-sort-regex #"^\$sort=(.+)$")
(defn valid-sort? [s] (re-matches query-sort-regex s))
(s/def ::sort 
       (st/spec {:description "排序请求格式为：$sort=xxx,xxx"
                 :spec (s/and string? valid-sort?)}))
(expound/defmsg ::sort "排序请求格式为：$sort=xxx,xxx")

(def query-filter-regex #"^\$filter=(.+)$")
(defn valid-filter? [s] (re-matches query-filter-regex s))
(s/def ::filter 
       (st/spec {:description "过滤格式： $filter=xxxx eq 'xx' and xxx = x"
                 :spec (s/and string? valid-filter?)}))
(expound/defmsg ::filter "过滤格式： $filter=xxxx eq 'xx' and xxx = x")

(def query-page-regex #"^\$page=(\d+)&\$pre_page=(\d+)$")
(defn valid-page? [s] (re-matches query-page-regex s))
(s/def ::page 
       (st/spec {:description "分页格式为： $page=x&$pre_page=x"
                 :spec (s/and string? valid-page?)}))
(expound/defmsg ::page "分页格式为： $page=x&$pre_page=x")

(def query-search-regex #"^\$rearch=(.+)$")
(defn valid-search? [s] (re-matches query-search-regex s))
(s/def ::search (s/and string? valid-search?))

(def asset-version "1")

(defn default-handler [req]
  {:status 200 
   :body "this is default handler"})

(defn coercion-error-handler [status]
  (let [printer (expound/custom-printer {:theme :figwheel-theme, :print-specs? false})
        handler (exception/create-coercion-handler status)]
    (fn [exception request]
      (printer (-> exception ex-data :problems))
      (handler exception request))))

(defn routes [db]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
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
                         :parameters {:header {:authorization ::header-token}}
                         :handler (auth/logout db)}}]


      ["/users"
       {:swagger {:tags ["users"]}}

       ["/" {:get {:summary "get users"
                   :middleware [[auth-mw/wrap-auth db "user"]]
                   :parameters {:header {:authorization ::header-token}
                                :query (s/keys :opt-un [::filter ::sort ::page])}
                   :handler (user/get-users db)}

             :post {:summary "create new user"
                    :parameters {:header {:authorization ::header-token}
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
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id int?}}
                      :handler (user/get-user db)}

                :put {:summary "update a user info"
                      :middleware [[auth-mw/wrap-auth db "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id int?}}
                      :handler default-handler}

                :delete {:summary "delete a user"
                         :middleware [[auth-mw/wrap-auth db "user"]]
                         :parameters {:header {:authorization ::header-token}
                                      :path {:id int?}}
                         :handler default-handler}}]
       ["/:id/update-password" {:post {:summary "update user password"
                                       :middleware [[auth-mw/wrap-auth db "user"]]
                                       :parameters {:header {:authorization ::header-token}
                                                    :body {:old-pass string?
                                                           :new-pass string?
                                                           :con-pass string?}}
                                       :handler default-handler}}]]

      ["/files"
       {:swagger {:tags ["files"]}}

       ["/upload" {:post {:summary "upload a file"
                          :parameters {:multipart {:file multipart/temp-file-part}
                                       :headers {:authorization ::header-token}}
                          :responses {200 {:body {:file multipart/temp-file-part}}}
                          :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                     {:status 200
                                      :body {:file file}})}}]

       ["/download" {:get {:summary "downloads a file"
                           :swagger {:produces ["image/png"]}
                           :parameters {:headers {:authorization ::header-token}}
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
                         (exception/create-exception-middleware
                          (merge
                           exception/exception-middleware
                           mw/exception-middleware
                           {:reitit.coercion/request-coercion (coercion-error-handler 400)
                            :retiit.coercion/response-coercion (coercion-error-handler 500)}))
                           ;; decoding request body
                         muuntaja/format-request-middleware
                           ;; coercing response bodys
                         coercion/coerce-response-middleware
                           ;; coercing request parameters
                         coercion/coerce-request-middleware
                           ;; multipart
                         multipart/multipart-middleware]}
     :exception pretty/exception})

   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/api-docs/v1"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "未找到"})
      :method-not-allowed (constantly {:status 405, :body "非法的调用"})}))))
