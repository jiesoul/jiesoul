(ns jiesoul.handler
  (:require [reitit.ring :as ring]
            [reitit.core :as r]
            [ring.util.response :as resp]
            [jiesoul.middleware :refer [exception-middleware]]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as html]))

(def asset-version "1")
(defn template [data-page]
  (html/render-file "layout/default.html" {:paeg data-page}))

(defn wrap [handler id]
  (fn [request]
    (handler (update request ::acc (fnil conj []) id))))

(defn handler [{::keys [acc]}]
  {:status 200, :body (str (conj acc :handler)) })

(defn app [db]
  (ring/ring-handler
   (ring/router
    [["/users" {:get (fn [{::r/keys [router]}]
                       {:status 200
                        :body (for [i (range 10)]
                                {:uri (-> router 
                                          (r/match-by-name ::user {:id i})
                                          (r/match->path {:iso "moly"}))})})}]
     ["/users/:id" 
      {:name ::user
       :get (constantly {:status 200 :body "user..."})}]
    
    ["/fail" (fn [_] (throw (ex-info "fail" {:type ::failure})))]
    {:data {:middleware [exception-middleware]}}])

   (ring/router
    ["/api" {:middleware [#(wrap % :api)]}
     ["/ping" {:handler handler}]
     ["/public/*" (ring/create-resource-handler)]
     ["/hello" {:handler (fn [name] (resp/response (str "hello " name)))}]
     
     ["/admin" {:middleware [[wrap :admin]]}
      ["/db" {:middleware [[wrap :db]]
              :handler handler}]]]) 

  (ring/routes
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not Found 404"})
     :method-not-allowed (constantly {:status 405, :body "kosh"})}))))
