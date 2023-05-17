(ns backend.handler.tag-handler 
  (:require [backend.db.tag-db :as tag-db]
            [clojure.tools.logging :as log]
            [backend.util.resp-util :as resp-util]))

(defn query-tags [{:keys [db]} opts]
  (log/debug "Query tags " opts)
  (let [data (tag-db/query db opts)]
    (resp-util/ok data)))

(defn create-tag! [{:keys [db]} tag]
  (log/debug "Creatge tag " tag)
  (let [_ (tag-db/create! db tag)]
    (resp-util/ok {})))

(defn get-tag [{:keys [db]} id]
  (log/debug "Get tag " id)
  (let [tag (tag-db/get-by-id db id)]
    (resp-util/ok tag)))

(defn update-tag! [{:keys [db]} tag]
  (log/debug "Update tag " tag)
  (let [_ (tag-db/update! db tag)]
    (resp-util/ok {})))

(defn delete-tag! [{:keys [db]} id]
  (log/debug "Delete tag " id)
  (let [_ (tag-db/delete! db id)]
    (resp-util/ok {})))

(defn get-all-tags [{:keys [db]}]
  (let [rs (tag-db/get-all-tags db)]))

(defn get-hot-tags [{:keys [db]} query]
  )
