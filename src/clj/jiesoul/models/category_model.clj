(ns jiesoul.models.category-model 
  (:require [next.jdbc.sql :as sql]))

(defn query [db opt]
  (sql/query db :category ))

(defn create! [db category]
  (sql/insert! db :category category))
