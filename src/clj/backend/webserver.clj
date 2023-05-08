(ns backend.webserver
  (:require [backend.middleware.auth-middleware :as auth-mw]
            [muuntaja.core :as mu-core]
            [reitit.coercion.spec]
            [clojure.spec.alpha :as s] 
            [reitit.ring :as reitit-ring]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.dev]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [backend.middleware :refer [exception-middleware wrap-cors]]
            [backend.util.req-uitl :as req-util]
            [backend.handler.auth-handler :as auth-handler]
            [backend.handler.category-handler :as category-handler]
            [backend.handler.user-handler :as user-handler]
            [backend.handler.tag-handler :as tag-handler]
            [backend.handler.article-handler :as article-handler]
            [clojure.tools.logging :as log]))

;; (defn make-response [response-value]
;;   (if (= (:ret response-value) :ok)
;;     (ring-response/ok response-value)
;;     (ring-response/bad-request response-value)))

;; ;
;; (def sort-regex #"^\$sort( )?=( )?(.+)$")
;; (def filter-regex #"^\$filter( )?=( )?(.+)$")
;; (def page-regex #"^page=(\d+)&per_page=(\d+)$")
;; (def search-regex #"^\$q=(.+)$")
;; (def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

;; (s/def ::email-type (s/and string? #(re-matches email-regex %)))
(s/def ::not-empty-string (s/and string? #(> (count %) 0)))
(s/def ::password-type (s/and string? #(>= (count %) 8)))

(s/def ::token (s/and string? #(re-matches #"^Token (.+)$" %)))
(s/def ::id pos-int?)
(s/def ::pid pos-int?)
(s/def ::name ::not-empty-string)
(s/def ::description string?)

(s/def ::page pos-int?)
(s/def ::page-size pos-int?)
(s/def ::sort string?)
(s/def ::filter string?)
(s/def ::q string?)
(s/def ::query
  (s/keys :opt-un [::page ::page-size ::sort ::filter ::q]))

(s/def ::username string?)
(s/def ::nickname string?)
(s/def ::birthday string?)
(s/def ::password ::password-type)
(s/def ::age pos-int?)
(s/def ::avatar string?)
(s/def ::phone string?)
(s/def ::UserUpdate
  (s/keys :req-un [::id ::nickname ::birthday]
          :opt-un [::password ::age ::avatar ::phone]))

(s/def ::old-password ::password-type)
(s/def ::new-password ::password-type)
(s/def ::confirm-password ::password-type)
(s/def ::UserPassword 
  (s/keys :req-un [::id ::old-password ::new-password ::confirm-password]))

(s/def ::CategoryAdd
  (s/keys :req-un [::name]
          :opt-un [ ::description]))

(s/def ::CategoryUpdate
  (s/keys :req-un [::id ::name]
          :opt-un [::description]))

(s/def ::TagAdd
  (s/keys :req-un [::name]
          :opt-un [::description]))

(s/def ::TagUpdate
  (s/keys :req-un [::id ::name]
          :opt-un [::description]))

(s/def ::article-id string?)
(s/def ::category-id pos-int?)
(s/def ::title ::not-empty-string)
(s/def ::author string?)
(s/def ::summary string?)
(s/def ::content-html string?)
(s/def ::content-md string?)
(s/def ::tags string?)

(s/def ::ArticleAdd 
  (s/keys :req-un [::title]
          :opt-un [::summary]))

(s/def ::ArticleUpdate 
  (s/keys :req-un [::title]
          :opt-un [::summary]))

(s/def ::ArticleCommentAdd 
  (s/keys :req-un [::id ::article-id ::content ::pid]))

;; (def asset-version "1")

(defn routes [env]
  
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
                       :parameters {:body {:username ::username
                                           :password ::password}}
                       :handler (fn [req]
                                  (let [body (get-in req [:parameters :body])
                                        {:keys [username password]} body]
                                    (auth-handler/login-auth env username password)))}}]

     ["/logout" {:post {:summary "user logout"
                        :handler (auth-handler/logout env)}}]]

    ["/users" 
     {:swagger {:tags ["User"]}}

     ["" {:get {:summary "Query users"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (user-handler/query-users env query)))}}]

     ["/:id" {:get {:summary "Get a user"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (user-handler/get-user env id)))}

              :put {:summary "Update a user"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}
                                 :body {:user ::UserUpdate}}
                    :handler (fn [req]
                               (let [user (req-util/parse-body req :user)]
                                 (user-handler/update-user! env user)))}

                ;; :delete {:summary "Delete a user"
                ;;          :middleware [[auth-mw/wrap-auth env "user"]]
                ;;          :parameters {:header {:authorization ::token}
                ;;                       :path [:map [:id pos-int?]]}
                ;;          :handler (fn [req]
                ;;                     (let [id (req-util/parse-path req :id)]
                ;;                       (user-handler/delete-user! env id)))}
              }]

     ["/:id/password" {:put {:summary "Update a user passwrod"
                             :middleware [[auth-mw/wrap-auth env "user"]]
                             :parameters {:header {:authorization ::token}
                                          :path {:id pos-int?}
                                          :body {:update-password ::UserPassword}}
                             :handler (fn [req]
                                        (let [update-password (req-util/parse-body req :update-password)]
                                          (user-handler/update-user-password! env update-password)))}}]]

    ["/categories" 
     {:swagger {:tags ["Category"]}}

     ["" {:get {:summary "Query categories"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (category-handler/query-categories env query)))}

          :post {:summary "New a category"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}
                              :body {:category ::CategoryAdd}}
                 :handler (fn [req]
                            (let [category (req-util/parse-body req :category)]
                              (category-handler/create-category! env category)))}}]


     ["/:id" {:get {:summary "Get a category"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path   {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (category-handler/get-category env id)))}

              :put {:summary "Update a category"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}
                                 :body {:category ::CategoryUpdate}}
                    
                    :handler (fn [req]
                               (let [category (req-util/parse-body req :category)]
                                 (category-handler/update-category! env category)))}

              :delete {:summary "Delete a category"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (category-handler/delete-category! env id)))}}]]

    ["/tags"
     {:swagger {:tags ["Tag"]}}

     ["" {:get {:summary "Query tags"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [opt (req-util/parse-query req)]
                             (tag-handler/query-tags env opt)))}

          :post {:summary "New a tag"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}
                              :body {:tag ::TagAdd}}
                 :handler (fn [req]
                            (let [tag (req-util/parse-body req :tag)]
                              (tag-handler/create-tag! env tag)))}}]


     ["/:id" {:get {:summary "Get a tag"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (tag-handler/get-tag env id)))}

              :put {:summary "Update a tag"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}
                                 :body {:tag ::TagUpdate}}
                    :handler (fn [req]
                               (let [tag (req-util/parse-body req :tag)]
                                 (tag-handler/update-tag! env tag)))}

              :delete {:summary "Delete a tag"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (tag-handler/delete-tag! env id)))}}]]

    ["/articles"
     {:swagger {:tags ["Article"]}}

     ["" {:get {:summary "Query articles"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [opt (req-util/parse-query req)]
                             (article-handler/query-articles env opt)))}

          :post {:summary "New a article"
                 :middleware [[auth-mw/wrap-auth env "user"]]
                 :parameters {:header {:authorization ::token}}
                 :handler (fn [req]
                            (log/debug  "new a article req: " (:body-params req))
                            (let [article (req-util/parse-body req :article)]
                              (article-handler/create-article! env article)))}}]


     ["/:id" {:get {:summary "Get a article"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id string?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (article-handler/get-article env id)))}

              :patch {:summary "Update a article"
                      :middleware [[auth-mw/wrap-auth env "user"]]
                      :parameters {:header {:authorization ::token}
                                   :path {:id string?}}
                      :handler (fn [req]
                                 (let [article (req-util/parse-body req :article)]
                                   (article-handler/update-article! env article)))}

              :delete {:summary "Delete a article"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id string?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (article-handler/delete-article! env id)))}}]

     ["/:id/push" {:patch {:summary "Query the comments of a article"
                           :middleware [[auth-mw/wrap-auth env "user"]]
                           :parameters {:header {:authorization ::token}
                                        :path {:id string?}}
                           :handler (fn [req]
                                      (let [article (req-util/parse-body req :article)]
                                        (article-handler/push! env article)))}}]

     ["/:id/comments" {:get {:summary "Query the comments of a article"
                             :middleware [[auth-mw/wrap-auth env "user"]]
                             :parameters {:header {:authorization ::token}
                                          :path {:id string?}}
                             :handler (fn [req]
                                        (let [article-id (req-util/parse-path req :id)]
                                          (article-handler/get-comments-by-article-id env article-id)))}
                       :post {:summary "add a comments of the article"
                              :parameters {:path {:id string?}}
                              :handler (fn [req]
                                         (let [comment (req-util/parse-body req :comment)]
                                           (article-handler/save-comment! env comment)))}}]]
    
    ["/aricles/comments" 
     {:swagger {:tags ["Articles Comments"]}}

     ["" {:get {:summary "Query all articles comments"
                :middleware [[auth-mw/wrap-auth env "user"]]
                :parameters {:header {:authorization ::token}
                             :query ::query}
                :handler (fn [req]
                           (let [query (req-util/parse-query req)]
                             (article-handler/query-articles-comments env query)))}}]
     
     ["/:id" {:get {:summary "Get a article comment"
                    :middleware [[auth-mw/wrap-auth env "user"]]
                    :parameters {:header {:authorization ::token}
                                 :path {:id pos-int?}}
                    :handler (fn [req]
                               (let [id (req-util/parse-path req :id)]
                                 (article-handler/get-articles-comments-by-id env id)))}
              
              :delete {:summary "Delete a article comment"
                       :middleware [[auth-mw/wrap-auth env "user"]]
                       :parameters {:header {:authorization ::token}
                                    :path {:id pos-int?}}
                       :handler (fn [req]
                                  (let [id (req-util/parse-path req :id)]
                                    (article-handler/delete-articles-comments-by-id env id)))}}]]


      ;; ["/files"
      ;;  {:swagger {:tags ["files"]}}
    
      ;;  ["/upload" {:post {:summary "upload a file"
      ;;                     :parameters {:multipart {:file reitit-multipart/temp-file-part}
      ;;                                  :headers {:authorization ::token}}
      ;;                     :responses {200 {:body {:file reitit-multipart/temp-file-part}}}
      ;;                     :handler (fn [{{{:keys [file]} :multipart} :parameters}]
      ;;                                {:status 200
      ;;                                 :body {:file file}})}}]
    
      ;;  ["/download" {:get {:summary "downloads a file"
      ;;                      :swagger {:produces ["image/png"]}
      ;;                      :parameters {:headers {:authorization ::token}}
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
                                       :coercion reitit.coercion.spec/coercion
                                       :middleware [reitit-swagger/swagger-feature
                                                    reitit-parameters/parameters-middleware
                                                    reitit-muuntaja/format-negotiate-middleware
                                                    reitit-muuntaja/format-response-middleware 
                                                    reitit-muuntaja/format-request-middleware
                                                    ;; coercing response bodys
                                                    reitit-coercion/coerce-response-middleware
                                                    ;; coercing request parameters
                                                    reitit-coercion/coerce-request-middleware
                                                    ;; 跨站
                                                    wrap-cors
                                                    exception-middleware
                                                    ]}})

    (reitit-ring/routes
    ;;  (reitit-swagger-ui/create-swagger-ui-handler {:path "/api-docs/v1"})
     (reitit-ring/redirect-trailing-slash-handler)
     (reitit-ring/create-file-handler {:path "/" :root "targer/shadow/dev/resources/public"})
     (reitit-ring/create-resource-handler {:path "/"})
     (reitit-ring/create-default-handler)))))
