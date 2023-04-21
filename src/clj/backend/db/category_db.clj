(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]))

(defn query-categories [db query]
  (try 
    (let [[w wv] (du/query-to-sql query)
          [p pv] (du/query-to-page query)
          sql (into [(str "select * from category " w p)] (into wv pv))
          _ (log/info "query categories sql: " sql)
          categoryies (sql/query db sql {:builder-fn rs/as-unqualified-maps})
          t-sql (into [(str "select count(1) as total from category " w)] wv)
          _ (log/info "total categories sql: " t-sql)
          total (sql/query db t-sql)]
      [categoryies (first total)])
    (catch java.sql.SQLException se (prn "sql error" se))))

(defn create! [db category]
  (sql/insert! db :category category {:return-keys true}))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id {:builder-fn rs/as-unqualified-maps}))
