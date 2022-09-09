(ns jiesoul.models.users
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [honey.sql :as hsql]
            [taoensso.timbre :as log]))

(extend-protocol rs/ReadableColumn
  Integer
  (read-column-by-index [x mrs i]
    (if (= (.getColumnName mrs i) "owner")
      (not (zero? x))
      x)))

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