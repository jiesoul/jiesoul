(ns jiesoul.models.users
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]))

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