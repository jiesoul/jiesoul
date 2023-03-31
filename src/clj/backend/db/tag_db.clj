(ns backend.db.tag-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]))

(defn query [db opt]
  (let [s "select * from tag "]
  (sql/query db (du/opt-to-sql s opt))))

(defn create! [db tag]
  (sql/insert! db :tag tag))

(defn update! [db tag]
  (sql/update! db :tag tag {:id (:id tag)}))

(defn delete! [db id]
  (sql/delete! db :tag {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :tag id))
