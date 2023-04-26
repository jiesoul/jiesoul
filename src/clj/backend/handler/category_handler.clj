(ns backend.handler.category-handler
  (:require [backend.db.category-db :as category-db]
            [clojure.tools.logging :as log]
            [backend.util.resp-util :as resp-util]))

(defn query-categories [{:keys [db]} opts]
  (log/debug "Query categories " opts)
  (let [data (category-db/query-categories db opts)
        _ (log/debug "query categories data: " data)]
    (resp-util/ok data)))

(defn create-category! [{:keys [db]} category]
  (log/debug "Creatge category " category)
  (let [_ (category-db/create! db category)]
    (resp-util/ok {} "添加成功")))

(defn get-category [{:keys [db]} id]
  (log/debug "Get category " id)
  (let [category (category-db/get-by-id db id)]
    (resp-util/ok {:category category})))

(defn update-category! [{:keys [db]} category]
  (log/debug "Update category " category)
  (let [_ (category-db/update! db category)]
    (resp-util/ok {})))

(defn delete-category! [{:keys [db]} id]
  (log/debug "Delete category " id)
  (let [_ (category-db/delete! db id)]
    (resp-util/ok {})))
