(ns jiesoul.router
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [jiesoul.auth.middleware :as auth-mw]
            [jiesoul.middleware :as mw]
            [jiesoul.auth.handler :as auth]
            [jiesoul.user.handler :as user]
            [jiesoul.category.handler :as category]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [expound.alpha :as expound]))

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

(defn routes [db]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}} ;; prefix for all paths
             :handler (swagger/create-swagger-handler)}}]

     ["/api/v1"
      ["/login" {:swagger {:tags ["验证"]}
                 :post {:summary "用户登录"
                        :parameters {:body {:username string?, :password string?}}
                        :handler (auth/login db)}}]

      ["/logout" {:swagger {:tags ["验证"]}
                  :post {:summary "用户退出"
                         :parameters {:header {:authorization ::header-token}}
                         :handler (auth/logout db)}}]

      ;; user 相关API
      ["/users"
       {:swagger {:tags ["用户"]}}

       ["" {:get {:summary "查询用户"
                   :middleware [[auth-mw/wrap-auth db "user"]]
                   :parameters {:header {:authorization ::header-token}
                                :query ::query}
                   :handler (user/get-users db)}

             :post {:summary "创建用户"
                    :parameters {:header {:authorization ::header-token}
                                 :body {:user ::create-user}}
                    :handler (user/create-user! db)}}]

       ["/:id" {:get {:summary "查看用户信息"
                      :middleware [[auth-mw/wrap-auth db "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id int?}}
                      :handler (user/get-user db)}

                :put {:summary "更新用户信息"
                      :middleware [[auth-mw/wrap-auth db "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}
                                   :body {:user ::update-user}}
                      :handler (user/update-user-info! db)}

                :delete {:summary "删除用户"
                         :middleware [[auth-mw/wrap-auth db "user"]]
                         :parameters {:header {:authorization ::header-token}
                                      :path {:id ::id}}
                         :handler (user/delete-user! db)}}]

       ["/:id/update-password" {:post {:summary "修改密码"
                                       :middleware [[auth-mw/wrap-auth db "user"]]
                                       :parameters {:header {:authorization ::header-token}
                                                    :path {:id ::id}
                                                    :body {:update-password ::update-password}}
                                       :handler (user/update-password! db)}}]]
      
      ;; 分类API
      ["/categories"
       {:swagger {:tags ["分类"]}}

       ["" {:get {:summary "查询分类"
                  :middleware [[auth-mw/wrap-auth db "user"]]
                  :parameters {:header {:authorization ::header-token}
                               :query ::query}
                  :handler (category/get-categories db)}

             :post {:summary "创建分类"
                    :middleware [[auth-mw/wrap-auth db "user"]]
                    :parameters {:header {:authorization ::header-token}
                                 :body {:category ::create-category}}
                    :handler (category/create-category! db)}}]
      

       ["/:id" {:get {:summary "获取分类"
                      :middleware [[auth-mw/wrap-auth db "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}}
                      :handler (category/get-category db)}

                :put {:summary "更新分类"
                      :middleware [[auth-mw/wrap-auth db "user"]]
                      :parameters {:header {:authorization ::header-token}
                                   :path {:id ::id}
                                   :body {:category ::update-category}}
                      :handler (category/update-category! db)}

                :delete {:summary "删除分类"
                         :middleware [[auth-mw/wrap-auth db "user"]]
                         :parameters {:header {:authorization ::header-token}
                                      :path {:id ::id}}
                         :handler (category/delete-category! db)}}]]

      ["/tags"
       {:swagger {:tags ["标签"]}}]

      ["/articles"
       {:swagger {:tags ["文章"]}}]

      ["/discusses"
       {:swagger {:tags ["讨论"]}}]

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
                         mw/exception-middleware
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
