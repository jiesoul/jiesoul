(ns backend.db.user-token-db
  (:require [backend.util.db-util :as du]
            [clojure.tools.logging :as log]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn query-users-tokens
  [db opts]
  (let [[ws wv] (du/opt-to-sql opts)
        ss (du/opt-to-sort opts)
        [ps pv] (du/opt-to-page opts)
        q-sql (into [(str "select * from user_token " ss ws ps)] (into wv pv))
        _ (log/debug "query users tokens sql: " q-sql (into wv pv))
        list (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from user_token " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list list
     :total total
     :opts opts}))

(defn save-user-token [db user-token]
  (sql/insert! db :user_token user-token))

(defn get-user-token-by-token 
  [db token]
  (sql/get-by-id db :user_token token :token {:builder-fn rs/as-unqualified-maps}))

(defn disable-user-token
  [db token]
  (let [now (java.time.Instant/now)]
    (sql/update! db :user_token {:expires_time now} {:token token})))

(defn update-user-token-expires-time
  [db id expires-time]
  (log/debug "udpate user token " id " expires time " expires-time)
  (sql/update! db :user_token {:expires_time expires-time} {:id id}))