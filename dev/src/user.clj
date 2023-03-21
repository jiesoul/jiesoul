(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.repl.state :as state]
            [jiesoul.core :as core]
            [ragtime.jdbc :as rt-jdbc]
            [ragtime.repl :as rt-repl]))

(ig-repl/set-prep! core/system-config)

(defn system [] (or state/system (throw (ex-info "System not running" {}))))

(defn env [] (:backend/env (system)))

(defn profile [] (:backend/env (system)))

(defn my-dummy-reset []
  (ig-repl/reset))

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

#_(comment
    (user/system)
    (user/env)
    (ig-repl/reset)
    (+ 1 2)
    )