(ns jiesoul.handlers.category-handler
  (:require [ring.util.response :as resp]
            [jiesoul.req-uitls :as ru]
            [jiesoul.models.category-model :as category-model]))

(defn get-categories [db]
  (fn [req]
    (let [query (ru/parse-query req)])))

(defn create-category [db]
  (fn [req]
    (let [])))