(ns jiesoul.models.token
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn save-user-token [db user-token]
  (sql/insert! db :user_token user-token))

(defn get-user-token-by-token 
  [db token]
  (sql/get-by-id db :user_token token :token {}))