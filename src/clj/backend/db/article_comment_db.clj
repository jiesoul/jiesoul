(ns backend.db.article-comment-db
  (:require [backend.util.db-util :as du]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [backend.db.article-comment-db :as article-comment-db]))

(defn query [db opt]
  (let [[w wv] (du/opt-to-sql opt)
        [p pv] (du/opt-to-page opt)
        q-sql (into [(str "select * from article_comment " w p)] (into wv pv))
        article-comments (sql/query db q-sql {:builder-fn rs/as-unqualified-maps})
        t-sql (into [(str "select count(1) as c from article_comment" w)] wv)
        total (-> (sql/query db t-sql)
              first
              :c)]
    {:data article-comments
     :total total}))

(defn create! [db article_comment]
  (sql/insert! db :article_comment article_comment))

(defn update! [db article_comment]
  (sql/update! db :article_comment article_comment {:id (:id article_comment)}))

(defn delete! [db id]
  (sql/delete! db :article_comment {:id id}))

(defn get-by-id [db id]
  (sql/get-by-id db :article_comment id {:builder-fn rs/as-unqualified-maps}))

(defn get-comments-by-article-id [db article-id]
  (sql/get-by-id db :article_comment {:article_id article-id} {:builder-fn rs/as-unqualified-maps}))

(defn delete-by-id-set! [db id-set]
  (sql/delete! db :article_comment ["id in ?" id-set]))

(defn delete-by-article-id! [db article-id]
  (sql/delete! db :article_comment ["article_id = ?" article-id]))