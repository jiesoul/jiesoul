(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]))

(defn query-categories [db query]
  (let [s "select * from category "
        sql (du/opt-to-sql s query)
        _ (log/info "query categories sql: " sql)]
    (sql/query db sql)))

(defn create! [db category]
  (sql/insert! db :category category))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id))
