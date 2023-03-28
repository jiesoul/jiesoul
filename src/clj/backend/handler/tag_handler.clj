(ns backend.handler.tag-handler 
  (:require [backend.db.tag-db :as tag-db]
            [clojure.tools.logging :as log]
            [ring.util.response :as resp]))

(defn query-tags [{:keys [db]} opt]
  (log/debug "Query tags " opt)
  (let [tags (tag-db/query db opt)]
    (resp/response {:status :ok
                    :data {:tags tags}})))

(defn create-tag! [{:keys [db]} tag]
  (log/debug "Creatge tag " tag)
  (let [create-time (java.time.Instant/now)
        _ (tag-db/create! db (assoc tag :create_time create-time))]
    (resp/response {::status :ok})))

(defn get-tag [{:keys [db]} id]
  (log/debug "Get tag " id)
  (let [tag (tag-db/get-by-id db id)]
    (resp/response {:status :ok
                    :data {:tag tag}})))

(defn update-tag! [{:keys [db]} tag]
  (log/debug "Update tag " tag)
  (let [_ (tag-db/update! db tag)]
    (resp/response {:status :ok})))

(defn delete-tag! [{:keys [db]} id]
  (log/debug "Delete tag " id)
  (let [_ (tag-db/delete! db id)]
    (resp/response {:status :ok})))