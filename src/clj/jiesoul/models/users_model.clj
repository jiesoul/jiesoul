(ns jiesoul.models.users-model
  (:require [next.jdbc.sql :as sql]
            [honey.sql :as hsql]
            [taoensso.timbre :as log]))

(defn create-user! 
  [db user]
  (sql/insert! db :users user))

(defn update-user! 
  [db {:keys [id] :as user}]
  (sql/update! db :users (dissoc user :id) {:id id}))

(defn update-user-password!
  [db {:keys [id password]}]
  (sql/update! db :user {:password password} {:id id}))

(defn get-user-by-name 
  [db username]
  (sql/get-by-id db :users username :username {}))

(defn get-user-by-id 
  [db id]
  (sql/get-by-id db :users id))

(defn get-users 
  [db opt]
  (log/debug "where: " opt)
  (let [query (hsql/format {:select [:*] :from [:users]})]
    (sql/query db query)))

(defn delete-user!
  [db id]
  (sql/delete! db :users {:id id}))
