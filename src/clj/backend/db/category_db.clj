(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]))

(defn query-categories [db query]
  (let [s "select * from category "
        sql (du/opt-to-sql s query)
        _ (log/info "query categories sql: " sql)]
    (sql/query db sql {:builder-fn rs/as-unqualified-maps})))

(defn create! [db category]
  (sql/insert! db :category category {:return-keys true}))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id {:builder-fn rs/as-unqualified-maps}))
