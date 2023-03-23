(ns backend.category.handler
  (:require [ring.util.response :as resp]
            [backend.req-uitls :as ru]
            [backend.category.db :as db]))

(defn get-categories [db]
  (fn [req]
    (let [query (ru/parse-query req)
          categories (db/query db query)]
      (resp/response {:message "获取成功"
                      :data {:categories categories}}))))

(defn create-category! [db]
  (fn [req]
    (let [category (ru/parse-body req :category)
          create-time (java.time.Instant/now)]
      (do
        (db/create! db (assoc category :create_time create-time))
        (resp/response {:message "创建分类成功"
                        :data {}})))))

(defn get-category [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          category (db/get-by-id db id)]
      (resp/response {:message ""
                      :data {:category category}}))))

(defn update-category! [db]
  (fn [req]
    (let [category (ru/parse-body req :category)
          result (db/update! db category)]
      (resp/response {:data {}}))))

(defn delete-category! [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          result (db/delete! db id)]
      (resp/response {:message ""}))))
