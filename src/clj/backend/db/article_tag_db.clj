(ns backend.db.article-tag-db
  (:require [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn create-multi! [db article-id tag-ids]
  (log/debug "tag ids: " tag-ids)
  (let [data (map #(conj [article-id %] tag-ids))]
    (sql/insert-multi! db :article_tag [:article_id :tag_id] data {:return-keys true})))

(defn delete-by-article-id [db article-id]
  (sql/delete! db :article_tag {:article_id article-id}))