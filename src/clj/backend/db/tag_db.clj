(ns backend.db.tag-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn query [db opt]
  (let [[ws wv] (du/opt-to-sql opt)
        [ps pv] (du/opt-to-page opt)
        q-sql (into [(str "select * from tag " ws ps)] (into wv pv))
        _ (log/info "query tags: " q-sql)
        tags (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
        t-sql (into [(str "select count(1) as c from tag " ws)] wv)
        _ (log/info "Count tags: " t-sql)
        total (:c (:first (sql/query db t-sql)))] 
    {:tags tags
     :total total
     :query opt}))

(defn create! [db tag]
  (sql/insert! db :tag tag))

(defn update! [db tag]
  (sql/update! db :tag tag {:id (:id tag)}))

(defn delete! [db id]
  (sql/delete! db :tag {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :tag id {:builder-fn rs/as-unqualified-maps}))
