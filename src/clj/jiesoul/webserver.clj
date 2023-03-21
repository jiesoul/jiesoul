(ns jiesoul.webserver
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as ring-response]
            [jiesoul.auth.middleware :as auth-mw]
            [jiesoul.middleware :as mw]
            [jiesoul.auth.handler :as auth]
            [jiesoul.user.handler :as user]
            [jiesoul.category.handler :as category]
            [muuntaja.core :as mu-core]
            [reitit.coercion.spec]
            [reitit.ring.middleware.multipart :as reitit-multipart]
            [reitit.ring :as reitit-ring]
            [reitit.coercion.malli]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.exception :as reitit-exception]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.ring.middleware.dev]
            [expound.alpha :as expound]))
(defn info 
  "Gets the info."
  [_]
  (log/debug "Enter info.")
  {:status 200 :body {:info "/info.html => Info in HTML format"}})

(defn make-response [response-value]
  (if (= (:ret response-value) :ok)
    (ring-response/ok response-value)
    (ring-response/bad-request response-value)))

(def token-regex #"^Token (.+)$")
(s/def ::header-token
  (st/spec {:description "请求头部Token格式为：Token xxxxxxxxx"
            :spec (s/and string? #(re-matches token-regex %))}))
(expound/defmsg ::header-token "请求头部Token格式为：Token xxxxxxxxx")

(def query-sort-regex #"^\$sort( )?=( )?(.+)$")
(s/def ::sort 
       (st/spec {:description "排序请求格式为：$sort=xxx,xxx"
                 :spec (s/and string? #(re-matches query-sort-regex %))}))
(expound/defmsg ::sort "排序请求格式为：$sort=xxx,xxx")

(def query-filter-regex #"^\$filter( )?=( )?(.+)$")
(s/def ::filter 
       (st/spec {:description "过滤格式： $filter = xxxx eq 'xx' and xxx = x"
                 :spec (s/and string? #(re-matches query-filter-regex %))}))
(expound/defmsg ::filter "过滤格式： $filter=xxxx eq 'xx' and xxx = x")

(def query-page-regex #"^page=(\d+)&pre_page=(\d+)$")
(s/def ::page 
       (st/spec {:description "分页格式为： $page=x&$pre_page=x"
                 :spec (s/and string? #(re-matches query-page-regex %))}))
(expound/defmsg ::page "分页格式为： $page=x&$pre_page=x")

(def query-search-regex #"^\$rearch=(.+)$")
(s/def ::search (s/and string? #(re-matches query-filter-regex %)))

(s/def ::query (s/keys :opt-un [::filter ::sort ::page]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email 
       (st/spec {:spec (s/and string? #(re-matches email-regex %))}))

(s/def ::id int?)
(s/def ::pid int?)
(s/def ::username string?)
(s/def ::password string?)
(s/def ::age int?)
(s/def ::roles string?)
(s/def ::nickname string?)
(s/def ::birthday string?)

(s/def ::name string?)
(s/def ::alias-name string?)
(s/def ::description string?)

(s/def ::create-user (s/keys :req-un [::username ::password ::email ::roles] 
                             :opt-un [::age ::nickname ::birthday]))
(s/def ::update-user (s/keys :req-un [::id ::age ::nickname ::birthday]))

(s/def ::old-password string?)
(s/def ::new-password string?)
(s/def ::confirm-password string?)
(s/def ::update-password (s/keys :req-un [::old-password ::new-password ::confirm-password]))

(s/def ::create-category (s/keys :req-un [::name ::pid] 
                                 :opt-un [::alias-name ::description]))
(s/def ::update-category (s/keys :req-un [::name ::pid]
                                 :opt-un [::alias-name ::description]))

(s/def ::create-tag (s/keys :req-un [::name ::pid]
                                 :opt-un [::alias-name ::description]))
(s/def ::update-tag (s/keys :req-un [::name ::pid]
                                 :opt-un [::alias-name ::description]))

(def asset-version "1")

(defn default-handler [req]
  {:status 200 
   :body "this is default handler"})

(defn routes [env]
  "Routes."
  
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"
                              :description "site api"}
                       :tags [{:name "api", :description "api"}]} ;; prefix for all paths
             :handler (reitit-swagger/create-swagger-handler)}}]
     
     ["/api-docs/*"
      {:get {:no-doc true
             :handler (reitit-swagger-ui/create-swagger-ui-handler
                       {:config {:validatorUrl nil}
                        :url "/swagger.json"})}}]
     
     ["/api"
      {:swagger {:tags ["api"]}}

      ["/info" {:get {:summary "Get info the api"
                      :parameters {:query [:map]}
                      :response {200 {:description "Info success"}}
                      :handler (fn [{}] (info env))}}]


      ["/login" {:post {:summary "login to the web site"
                        :parameters {:body {:username string?, :password string?}}
                        :response {:body [:map 
                                          [:username string?]
                                          [:password string?]]}
                        :handler (fn [req] 
                                   (let [body (get-in req [:parameters :body])
                                         {:keys [username password]} body]
                                     (auth/login-auth env username password)))}}]

      ["/logout" {:swagger {:tags ["验证"]}
                  :post {:summary "用户退出"
                         :parameters {:header {:authorization ::header-token}}
                         :handler (auth/logout env)}}]

      ;; user 相关API
      ["/users"
       {:swagger {:tags ["用户"]}}

       ["" {:get {:summary "查询用户"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization ::header-token}
                               :query ::query}
                  :handler (user/get-users env)}

            :post {:summary "创建用户"
                   :parameters {:header {:authorization ::header-token}
                                :body {:user ::create-user}}
                   :handler (user/create-user! env)}}]

       ["/:id" {:get {:summary "查看用户信息"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id int?}}
                      :handler (user/get-user env)}

                :put {:summary "更新用户信息"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}
                                   :body {:user ::update-user}}
                      :handler (user/update-user-info! env)}

                :delete {:summary "删除用户"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization ::header-token}
                                      :path {:id ::id}}
                         :handler (user/delete-user! env)}}]

       ["/:id/update-password" {:post {:summary "修改密码"
                                       :middleware [[auth-mw/wrap-auth env "user"]]
                                       :parameters {:header {:authorization ::header-token}
                                                    :path {:id ::id}
                                                    :body {:update-password ::update-password}}
                                       :handler (user/update-password! env)}}]]

      ;; 分类API
      ["/categories"
       {:swagger {:tags ["分类"]}}

       ["" {:get {:summary "查询分类"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization ::header-token}
                               :query ::query}
                  :handler (category/get-categories env)}

            :post {:summary "创建分类"
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :parameters {:header {:authorization ::header-token}
                                :body {:category ::create-category}}
                   :handler (category/create-category! env)}}]


       ["/:id" {:get {:summary "获取分类"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}}
                      :handler (category/get-category env)}

                :put {:summary "更新分类"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}
                                   :body {:category ::update-category}}
                      :handler (category/update-category! env)}

                :delete {:summary "删除分类"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization ::header-token}
                                      :path {:id ::id}}
                         :handler (category/delete-category! env)}}]]

      ["/tags"
       {:swagger {:tags ["标签"]}}

       ["/" {:get {:summary "查询标签"
                   :parameters {:header {:authorization ::header-token}
                                :query ::query}
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :handler (default-handler env)}}]]

      ["/articles"
       {:swagger {:tags ["文章"]}}]

      ["/discusses"
       {:swagger {:tags ["讨论"]}}]

      ["/files"
       {:swagger {:tags ["files"]}}

       ["/upload" {:post {:summary "upload a file"
                          :parameters {:multipart {:file reitit-multipart/temp-file-part}
                                       :headers {:authorization ::header-token}}
                          :responses {200 {:body {:file reitit-multipart/temp-file-part}}}
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
                                                 (io/input-stream))})}}]]]])

(defn handler 
  "Handler."
  [routes]
  (-> 
   (reitit-ring/ring-handler
    (reitit-ring/router routes {:data {:muuntaja mu-core/instance
                                       :coercion (reitit.coercion.malli/create
                                                  {:error-keys #{:type :coercion :in #_:schema #_:value #_:errors :humanized #_:transformed}
                                                   :validate true
                                                   :enabled true
                                                   :strip-extra-keys true
                                                   :default-values true
                                                   :options nil})
                                       :middleware [reitit-swagger/swagger-feature
                                                    reitit-parameters/parameters-middleware
                                                    reitit-muuntaja/format-negotiate-middleware
                                                    reitit-muuntaja/format-response-middleware
                                                    (reitit-exception/create-exception-middleware
                                                     (merge
                                                      (reitit-exception/default-handlers
                                                       {::reitit-exception/wrap (fn [handler ^Exception e request]
                                                                                  (log/error e (.getMessage e))
                                                                                  (handler e request))})))
                                                    reitit-muuntaja/format-request-middleware
                                                      ;; coercing response bodys
                                                    reitit-coercion/coerce-response-middleware
                                                      ;; coercing request parameters
                                                    reitit-coercion/coerce-request-middleware]}})

    (reitit-ring/routes
    ;;  (reitit-swagger-ui/create-swagger-ui-handler {:path "/api-docs/v1"})
     (reitit-ring/redirect-trailing-slash-handler)
     (reitit-ring/create-file-handler {:path "/" :root "targer/shadow/dev/resources/public"})
     (reitit-ring/create-resource-handler {:path "/"})
     (reitit-ring/create-default-handler)))))
