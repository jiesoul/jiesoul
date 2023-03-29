(ns backend.db.user-token-db
  (:require [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn save-user-token [db user-token]
  (sql/insert! db :user_token user-token))

(defn get-user-token-by-token 
  [db token]
  (sql/get-by-id db :user_token token :token {}))

(defn disable-user-token
  [db token]
  (let [now (java.time.Instant/now)]
    (sql/update! db :user_token {:expires_time now} {:token token})))

(defn update-user-token-expires-time
  [db id expires-time]
  (log/debug "udpate user token " id " expires time " expires-time)
  (sql/update! db :user_token {:expires_time expires-time} {:id id}))