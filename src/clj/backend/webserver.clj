(ns backend.webserver
  (:require [clojure.tools.logging :as log]
            [backend.middleware.auth-middleware :as auth-mw]
            [muuntaja.core :as mu-core]
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
            [ring.util.http-response :as ring-response]
            [backend.middleware :refer [exception-middleware]]
            [backend.util.req-uitl :as req-util]
            [backend.handler.auth-handler :as auth-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.article-handler :as article-handler]))

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

(def Query [:map 
            [:page {:optional true} pos-int?]
            [:per-page {:optional true} pos-int?]
            [:sort {:optional true} string?]
            [:filter {:optional true} string?]
            [:q {:optional true} string?]])

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

(def Update-Tag [:map
                 [:id pos-int?]
                 [:name string?]
                 [:pid pos-int?]
                 [:alias-name {:optional true} string?]
                 [:description {:optional true} string?]])


(def Create-Article [:map 
                     [:id pos-int?]
                     [:title string?]])

(def Update-Article [:map 
                     [:id pos-int?]
                     [:title string?]])


(def Create-Article-Comment [:map 
                             [:id pos-int?]])


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
     
     ["/api/v1"

      ;; backend api
      ["" 
       {:swagger {:tags ["Auth"]}}
       ["/login" {:post {:summary "login to the web site"
                         :parameters {:body [:map
                                             [:username string?]
                                             [:password string?]]}
                         :handler (fn [req]
                                    (let [body (get-in req [:parameters :body])
                                          {:keys [username password]} body]
                                      (auth-handler/login-auth env username password)))}}]

       ["/logout" {:post {:summary "user logout"
                          :handler (auth-handler/logout env)}}]]

      ["/users" 
       {:swagger {:tags ["User"]}}

       ["" {:get {:summary "Query users"
                  :middleware [
                               [auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query Query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (user-handler/query-users env opt)))}

            :post {:summary "New user"
                   :parameters {:header {:authorization Token}
                                :body [:map [:user Create-User]]}
                   :handler (fn [req]
                              (let [user (req-util/parse-body req :user)]
                                (user-handler/create-user! env user)))}}]

       ["/:id" {:get {:summary "Get a user"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (user-handler/get-user env id)))}

                :put {:summary "Update a user"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :body [:map [:user Update-User]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (user-handler/update-user! env id)))}

                :delete {:summary "Delete a user"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (user-handler/delete-user! env id)))}}]

       ["/:id/password" {:patch {:summary "Update a user passwrod"
                                 :middleware [[auth-mw/wrap-auth env "user"]]
                                 :parameters {:header {:authorization Token}
                                              :body [:map [:update-password Update-Password]]}
                                 :handler (fn [req]
                                            (let [update-password (req-util/parse-body req :update-password)]
                                              (user-handler/update-user-password! env update-password)))}}]]

      ["/categories" 
       {:swagger {:tags ["Category"]}}

       ["" {:get {:summary "Query categories"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query Query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (category-handler/query-categories env opt)))}

            :post {:summary "New a category"
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :parameters {:header {:authorization Token}
                                :body [:map [:category Create-Category]]}
                   :handler (fn [req]
                              (let [category (req-util/parse-body req :category)]
                                (category-handler/create-category! env category)))}}]


       ["/:id" {:get {:summary "Get a category"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (category-handler/get-category env id)))}

                :put {:summary "Update a category"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]
                                   :body [:map [:category Update-Category]]}
                      :handler (fn [req]
                                 (let [category (req-util/parse-body req :category)]
                                   (category-handler/update-category! env category)))}

                :delete {:summary "Delete a category"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (category-handler/delete-category! env id)))}}]]

      ["/tags"
       {:swagger {:tags ["Tag"]}}

       ["" {:get {:summary "Query tags"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query Query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (tag-handler/query-tags env opt)))}

            :post {:summary "New a tag"
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :parameters {:header {:authorization Token}
                                :body [:map [:tag Create-Tag]]}
                   :handler (fn [req]
                              (let [tag (req-util/parse-body req :tag)]
                                (tag-handler/create-tag! env tag)))}}]


       ["/:id" {:get {:summary "Get a tag"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (tag-handler/get-tag env id)))}

                :put {:summary "Update a tag"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]
                                   :body [:map [:category Update-Tag]]}
                      :handler (fn [req]
                                 (let [tag (req-util/parse-body req :tag)]
                                   (tag-handler/update-tag! env tag)))}

                :delete {:summary "Delete a tag"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (tag-handler/delete-tag! env id)))}}]]

      ["/articles"
       {:swagger {:tags ["Article"]}}

       ["" {:get {:summary "Query articles"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query Query}
                  :handler (fn [req]
                             (let [opt (req-util/parse-query req)]
                               (article-handler/query-articles env opt)))}

            :post {:summary "New a article"
                   :middleware [[auth-mw/wrap-auth env "user"]]
                   :parameters {:header {:authorization Token}
                                :body [:map [:article Create-Article]]}
                   :handler (fn [req]
                              (let [article (req-util/parse-body req :article)]
                                (article-handler/create-article! env article)))}}]


       ["/:id" {:get {:summary "Get a article"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/get-article env id)))}

                :put {:summary "Update a article"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]
                                   :body [:map [:article Update-Article]]}
                      :handler (fn [req]
                                 (let [article (req-util/parse-body req :article)]
                                   (article-handler/update-article! env article)))}

                :delete {:summary "Delete a article"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-handler/delete-article! env id)))}}]

       ["/:id/comments" {:get {:summary "Query the comments of a article"
                               :middleware [[auth-mw/wrap-auth env "user"]]
                               :parameters {:header {:authorization Token}
                                            :path [:map [:id pos-int?]]}
                               :handler (fn [req]
                                          (let [article-id (req-util/parse-path req :id)]
                                            (article-handler/get-comments-by-article-id env article-id)))}}]]
      
      ["/aricles/comments" 
       {:swagger {:tags ["Articles Comments"]}}

       ["" {:get {:summary "Query all articles comments"
                  :middleware [[auth-mw/wrap-auth env "user"]]
                  :parameters {:header {:authorization Token}
                               :query Query}
                  :handler (fn [req]
                             (let [query (req-util/parse-query req)]
                               (article-handler/query-articles-comments env query)))}}]
       
       ["/:id" {:get {:summary "Get a article comment"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization Token}
                                   :path [:map [:id pos-int?]]}
                      :handler (fn [req]
                                 (let [id (req-util/parse-path req :id)]
                                   (article-handler/get-articles-comments-by-id env id)))}
                
                :delete {:summary "Delete a article comment"
                         :middleware [[auth-mw/wrap-auth env "user"]]
                         :parameters {:header {:authorization Token}
                                      :path [:map [:id pos-int?]]}
                         :handler (fn [req]
                                    (let [id (req-util/parse-path req :id)]
                                      (article-handler/delete-articles-comments-by-id env id)))}}]]


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
                                       :middleware [exception-middleware
                                                    reitit-swagger/swagger-feature
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
