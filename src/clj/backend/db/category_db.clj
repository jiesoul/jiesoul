(ns backend.db.category-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]))

(defn query-categories [db opts]
  (try 
    (let [[ws wv] (du/opt-to-sql opts)
          ss (du/opt-to-sort opts)
          [ps pv] (du/opt-to-page opts)
          q-sql (into [(str "select * from category "  ws ss ps)] (into wv pv))
          _ (log/info "query categories sql: " q-sql)
          categories (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
          t-sql (into [(str "select count(1) as c from category " ws)] wv)
          _ (log/info "total categories sql: " t-sql)
          total (:c (first (sql/query db t-sql)))] 
      {:list categories 
       :total total
       :opts opts})
    (catch java.sql.SQLException se 
      (throw (ex-info "query error" se)))))

(defn create! [db category]
  (try 
    (sql/insert! db :category category {:return-keys true})
    (catch java.sql.SQLException se 
      (throw (ex-info "create category error" se)))))

(defn update! [db category]
  (sql/update! db :category category {:id (:id category)}))

(defn delete! [db id]
  (sql/delete! db :category {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :category id {:builder-fn rs/as-unqualified-kebab-maps}))

(defn find-by-name [db name]
  (sql/find-by-keys db :category {:name name} {:builder-fn rs/as-unqualified-kebab-maps}))

(defn get-all-category [db]
  (sql/query db ["select * from category"] {:builder-fn rs/as-unqualified-kebab-maps}))
