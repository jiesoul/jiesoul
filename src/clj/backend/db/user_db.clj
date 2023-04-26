(ns backend.db.user-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query-users
  [db opts]
  (let [[ws wv] (du/opt-to-sql opts)
        [ps pv] (du/opt-to-page opts)
        q-sql (into [(str "select * from users " ws ps)] (into wv pv))
        users (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
        t-sql (into [(str "select count(1) :as c from users " ws)] wv)
        total (sql/query db t-sql)]
    {:users users 
     :total total
     :opts opts}))

(defn create-user! 
  [db user]
  (sql/insert! db :users user))

(defn update-user! 
  [db {:keys [id] :as user}]
  (sql/update! db :users (dissoc user :id) {:id id}))

(defn update-user-password!
  [db id password]
  (sql/update! db :users {:password password} {:id id}))

(defn get-user-by-name 
  [db username]
  (sql/get-by-id db :users username :username {:builder-fn rs/as-unqualified-maps}))

(defn get-user-by-id 
  [db id]
  (sql/get-by-id db :users id {:builder-fn rs/as-unqualified-maps}))

(defn delete-user!
  [db id]
  (sql/delete! db :users {:id id}))
