(ns backend.handler.article-handler
  (:require [backend.db.article-db :as article-db]
            [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [backend.db.article-comment-db :as article-comment-db]))

(defn query-articles [{:keys [db]} opt]
  (log/debug "Query articles " opt)
  (let [articles (article-db/query db opt)]
    (resp/response {:status :ok
                    :data {:articles articles}})))

(defn create-article! [{:keys [db]} article]
  (log/debug "Creatge article " article)
  (let [create-time (java.time.Instant/now)
        _ (article-db/create! db (assoc article :create_time create-time))]
    (resp/response {:status :ok
                    :data {}})))

(defn get-article [{:keys [db]} id]
  (log/debug "Get article " id)
  (let [article (article-db/get-by-id db id)]
    (resp/response {:status :ok
                    :data {:article article}})))

(defn update-article! [{:keys [db]} article]
  (log/debug "Update article " article)
  (let [_ (article-db/update! db article)]
    (resp/response {:status :ok})))

(defn delete-article! [{:keys [db]} id]
  (log/debug "Delete article " id)
  (let [_ (article-db/delete! db id)]
    (resp/response {:status :ok})))

(defn get-comments-by-article-id [{:keys [db]} article-id]
  (log/debug "Get comments by article id " article-id)
  (let [comments (article-comment-db/get-comments-by-article-id db article-id)]
    (resp/response {:status :ok 
                    :comments comments})))

(defn query-articles-comments [{:keys [db]} opt]
  (log/debug "Query articles comments " opt)
  (let [articles-comments (article-comment-db/query db opt)]
    (resp/response {:status :ok
                    :data {:articles-comments articles-comments}})))

(defn get-articles-comments-by-id [{:keys [db]} id]
  (log/debug "Get article comment " id)
  (let [article-comment (article-comment-db/get-by-id db id)]
    (resp/response {:status :ok 
                    :data {:article-comment article-comment}})))

(defn delete-articles-comments-by-id [{:keys [db]} id]
  (log/debug "Delete article comment " id)
  (let [_ (article-comment-db/delete! db id)]
    (resp/response {:status :ok})))

(defn delete-articles-comments-by-ids [{:keys [db]} id-set]
  (log/debug "Delete article comment " id-set)
  (let [_ (article-comment-db/delete-by-id-set! db id-set)]
    (resp/response {:status :ok})))

