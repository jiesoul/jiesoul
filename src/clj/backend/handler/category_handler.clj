(ns backend.handler.category-handler
  (:require [ring.util.response :as resp]
            [backend.db.category-db :as category-db]
            [clojure.tools.logging :as log]))

(defn query-categories [{:keys [db]} opt]
  (log/debug "Query categories " opt)
  (let [categories (category-db/query db opt)]
    (resp/response {:status :ok
                    :data {:categories categories}})))

(defn create-category! [{:keys [db]} category]
  (log/debug "Creatge category " category)
  (let [create-time (java.time.Instant/now)
        _ (category-db/create! db (assoc category :create_time create-time))]
    (resp/response {:status :ok
                    :data {}})))

(defn get-category [{:keys [db]} id]
  (log/debug "Get category " id)
  (let [category (category-db/get-by-id db id)]
    (resp/response {:status :ok
                    :data {:category category}})))

(defn update-category! [{:keys [db]} category]
  (log/debug "Update category " category)
  (let [_ (category-db/update! db category)]
    (resp/response {:status :ok})))

(defn delete-category! [{:keys [db]} id]
  (log/debug "Delete category " id)
  (let [_ (category-db/delete! db id)]
    (resp/response {:status :ok})))
