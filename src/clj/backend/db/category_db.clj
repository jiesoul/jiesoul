(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]))

(defn query [db opt]
  (let [s "select * from category "]
  (sql/query db :category (du/opt-to-sql s opt))))

(defn create! [db category]
  (sql/insert! db :category category))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id))
