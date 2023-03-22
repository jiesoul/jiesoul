(ns jiesoul.webserver
  (:require [clojure.tools.logging :as log]
            [jiesoul.auth.handler :as auth]
            [jiesoul.auth.middleware :as auth-mw]
            [jiesoul.category.handler :as category]
            [jiesoul.user.handler :as user]
            [muuntaja.core :as mu-core]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [reitit.coercion.malli]
            [reitit.coercion.spec]
            [reitit.ring :as reitit-ring]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.dev]
            [reitit.ring.middleware.exception :as reitit-exception]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [ring.util.http-response :as ring-response]))

(defn info 
  "Gets the info."
  [_]
  (log/debug "Enter info.")
  {:status 200 :body {:info "/info.html => Info in HTML format"}})

(defn get-ds [env]
  (-> env 
      :db
      (jdbc/get-datasource)
      (jdbc/with-options {:builder-fn rs/as-unqualified-maps})))

(defn make-response [response-value]
  (if (= (:ret response-value) :ok)
    (ring-response/ok response-value)
    (ring-response/bad-request response-value)))

(def Token [:string {:re "^Token (.+)$"}])

(def sort-regex #"^\$sort( )?=( )?(.+)$")

(def filter-regex #"^\$filter( )?=( )?(.+)$")

(def page-regex #"^page=(\d+)&pre_page=(\d+)$")

(def search-regex #"^\$rearch=(.+)$")

(def email-reg "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,63}$")

(def Create-User [:map 
                  [:username string?]
                  [:password string?]
                  [:email [:string {:re email-reg}]]
                  [:roles [:set string?]]
                  [:age {:optional true} pos-int?]
                  [:nickname {:optional true} string?]
                  [:birthday {:optional true} string?]])

(def Update-User [:map 
                  [:id pos-int?]
                  [:nickname string?]
                  [:birthday string?]])


(def Update-Password [:map 
                      [:old-password string?]
                      [:new-password string?]
                      [:confirm-password string?]])

(def Create-Category [:map
                      [:name string?]
                      [:pid pos-int?]
                      [:alias-name {:optional true} string?]
                      [:description {:optional true} string?]])

(def Update-Category [:map
                      [:id pos-int?]
                      [:name string?]
                      [:pid pos-int?]
                      [:alias-name {:optional true} string?]
                      [:description {:optional true} string?]])

(def Create-Tag [:map
                 [:name string?]
                 [:pid pos-int?]
                 [:alias-name {:optional true} string?]
                 [:description {:optional true} string?]])

(def Update-tag [:map
                 [:id pos-int?]
                 [:name string?]
                 [:pid pos-int?]
                 [:alias-name {:optional true} string?]
                 [:description {:optional true} string?]])


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

      ["/info" {:get {:summary "Get info the api"
                      :parameters {:query [:map]}
                      :response {200 {:description "Info success"}}
                      :handler (fn [{}] (info env))}}]


      ["/login" {:post {:summary "login to the web site"
                        :parameters {:body [:map
                                            [:username string?]
                                            [:password string?]]}
                        :handler (fn [req] 
                                   (let [body (get-in req [:parameters :body])
                                         {:keys [username password]} body]
                                     (auth/login-auth env username password)))}}]

      ["/logout" {:post {:summary "用户退出"
                         :parameters {:header {:authorization Token}}
                         :handler (auth/logout env)}}]

      ["/users"
       {:swagger {:tags ["用户"]}}

       ["" {:get {:summary "查询用户"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query [:map]}
                  :handler (user/get-users env)}

            :post {:summary "创建用户"
                   :parameters {:header {:authorization Token}
                                :body [:map [:user Create-User]]}
                   :handler (user/create-user! env)}}]

       ["/:id" {:get {:summary "查看用户信息"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (user/get-user env)}

                :put {:summary "更新用户信息"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :body [:map [:user Update-User]]}
                      :handler (user/update-user-info! env)}

                :delete {:summary "删除用户"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (user/delete-user! env)}}]

       ["/:id/update-password" {:post {:summary "修改密码"
                                       :middleware [[auth-mw/wrap-auth env "user"]]
                                       :parameters {:header {:authorization Token}
                                                    :body [:map [:update-password Update-Password]]}
                                       :handler (user/update-password! env)}}]]

      ["/categories"
       {:swagger {:tags ["分类"]}}

       ["" {:get {:summary "查询分类"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query [:map]}
                  :handler (category/get-categories env)}

            :post {:summary "创建分类"
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :parameters {:header {:authorization Token}
                                :body [:map [:category Create-Category]]}
                   :handler (category/create-category! env)}}]


       ["/:id" {:get {:summary "获取分类"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (category/get-category env)}

                :put {:summary "更新分类"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]
                                   :body [:map [:category Update-Category]]}
                      :handler (category/update-category! env)}

                :delete {:summary "删除分类"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (category/delete-category! env)}}]]

      ["/tags"
       {:swagger {:tags ["标签"]}}

       ["/" {:get {:summary "查询标签"
                   :parameters {:header {:authorization Token}
                                :query [:map]}
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :handler (default-handler env)}}]]

      ;; ["/articles"
      ;;  {:swagger {:tags ["文章"]}}]

      ;; ["/discusses"
      ;;  {:swagger {:tags ["讨论"]}}]

      ;; ["/files"
      ;;  {:swagger {:tags ["files"]}}

      ;;  ["/upload" {:post {:summary "upload a file"
      ;;                     :parameters {:multipart {:file reitit-multipart/temp-file-part}
      ;;                                  :headers {:authorization Token}}
      ;;                     :responses {200 {:body {:file reitit-multipart/temp-file-part}}}
      ;;                     :handler (fn [{{{:keys [file]} :multipart} :parameters}]
      ;;                                {:status 200
      ;;                                 :body {:file file}})}}]

      ;;  ["/download" {:get {:summary "downloads a file"
      ;;                      :swagger {:produces ["image/png"]}
      ;;                      :parameters {:headers {:authorization Token}}
      ;;                      :handler (fn [_]
      ;;                                 {:status 200
      ;;                                  :headers {"Content-Type" "image/png"}
      ;;                                  :body (-> "reitit.png"
      ;;                                            (io/resource)
      ;;                                            (io/input-stream))})}}]]
      ]])

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
