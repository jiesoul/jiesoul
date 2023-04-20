(ns backend.db.tag-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query [db opt]
  (let [s "select * from tag "]
  (sql/query db (du/opt-to-sql s opt) {:builder-fn rs/as-unqualified-maps})))

(defn create! [db tag]
  (sql/insert! db :tag tag))

(defn update! [db tag]
  (sql/update! db :tag tag {:id (:id tag)}))

(defn delete! [db id]
  (sql/delete! db :tag {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :tag id {:builder-fn rs/as-unqualified-maps}))
