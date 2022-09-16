(ns jiesoul.models.users-model
  (:require [next.jdbc.sql :as sql]
            [honey.sql :as hsql]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

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
  (log/debug "opt: " opt)
  (let [{:keys [sort filter page]} opt
        s "select * from users"
        v []
        [s v] (if (seq? filter) [s v] [(str s " where " (first filter)) (into v (second filter))])
        [s v] (if (empty? sort) [s v] [(str s " order by " sort) v])
        [s v] (if (empty? page) [s v] [(str s " limit ? offset ? ") (into v page)])]
    (log/debug "sql str: " (into [s] v))
    (sql/query db (into [s] v))))

(defn delete-user!
  [db id]
  (sql/delete! db :users {:id id}))
