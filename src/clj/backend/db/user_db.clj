(ns backend.db.user-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.sql :as sql]
            [taoensso.timbre :as log]))

(defn query-users
  [db query]
  (let [s "select * from users"
        sql (du/opt-to-sql s query)
        _ (log/info "query user sql: " sql)]
    (sql/query db sql)))

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
  (sql/get-by-id db :users username :username ""))

(defn get-user-by-id 
  [db id]
  (sql/get-by-id db :users id))

(defn delete-user!
  [db id]
  (sql/delete! db :users {:id id}))
