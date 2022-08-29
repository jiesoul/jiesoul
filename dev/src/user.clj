(ns user
  (:require [integrant.repl :as ig-repl]
            [jiesoul.server :as server]
            [ragtime.jdbc :as rt-jdbc]
            [ragtime.repl :as rt-repl]
            [buddy.hashers :as buddy-hashers]))

(ig-repl/set-prep! (constantly server/system-config))

(defn load-db-config []
  {:datastore  (rt-jdbc/sql-database {:connection-uri "jdbc:sqlite:resources/database/jiesoul.db"})
   :migrations (rt-jdbc/load-resources "migrations")})

(defn migrate []
  (rt-repl/migrate (load-db-config)))

(defn rollback []
  (rt-repl/rollback (load-db-config)))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)
  (reset-all))