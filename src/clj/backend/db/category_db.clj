(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]))

(defn query-categories [db query]
  (try 
    (let [[ws wv] (du/opt-to-sql query)
          [ps pv] (du/opt-to-page query)
          q-sql (into [(str "select * from category " ws ps)] (into wv pv))
          _ (log/info "query categories sql: " q-sql)
          categories (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
          t-sql (into [(str "select count(1) as c from category " ws)] wv)
          _ (log/info "total categories sql: " t-sql)
          total (:c (first (sql/query db t-sql)))] 
      {:categories categories 
       :total total
       :query query})
    (catch java.sql.SQLException se (log/error "sql error: " se))))

(defn create! [db category]
  (sql/insert! db :category category {:return-keys true}))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id {:builder-fn rs/as-unqualified-maps}))
