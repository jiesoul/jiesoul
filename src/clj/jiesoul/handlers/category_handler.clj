(ns jiesoul.handlers.category-handler
  (:require [ring.util.response :as resp]
            [jiesoul.req-uitls :as ru]
            [jiesoul.models.category-model :as category-model]))

(defn get-categories [db]
  (fn [req]
    (let [query (ru/parse-query req)
          data (category-model/query db query)]
      )))

(defn create-category [db]
  (fn [req]
    (let [category (ru/parse-body req :category)
          create-time (java.time.Instant/now)]
      (do 
        (category-model/create! db (assoc category :create_time create-time))
        (resp/response {:message "创建"})))))