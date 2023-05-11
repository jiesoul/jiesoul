(ns backend.handler.article-handler
  (:require [backend.db.article-db :as article-db]
            [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [backend.db.article-comment-db :as article-comment-db]
            [backend.util.resp-util :as resp-util]))

(defn gen-id []
  (let [now (java.time.LocalDateTime/now)
        dtf (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmssSSSSS")]
    (. dtf format now)))

(defn query-articles [{:keys [db]} opts]
  (log/debug "Query articles " opts)
  (let [data (article-db/query db opts)]
    (resp-util/ok data)))

(defn create-article! [{:keys [db]} article]
  (log/debug "Creatge article " article)
  (let [create-time (java.time.LocalDateTime/now)
        id (gen-id)
        _ (article-db/create! db (-> article
                                     (assoc :create_time create-time
                                            :id id)))]
    (resp-util/ok {})))

(defn get-article [{:keys [db]} id]
  (log/debug "Get article " id)
  (let [article (article-db/get-by-id db id)]
    (resp-util/ok article)))

(defn update-article! [{:keys [db]} article]
  (log/debug "Update article " article)
  (let [_ (article-db/update! db article)]
    (resp-util/ok {})))

(defn delete-article! [{:keys [db]} id]
  (log/debug "Delete article " id)
  (let [_ (article-db/delete! db id)]
    (resp-util/ok {})))

(defn get-comments-by-article-id [{:keys [db]} article-id]
  (log/debug "Get comments by article id " article-id)
  (let [comments (article-comment-db/get-comments-by-article-id db article-id)]
    (resp-util/ok comments)))

(defn query-articles-comments [{:keys [db]} opt]
  (log/debug "Query articles comments " opt)
  (let [articles-comments (article-comment-db/query db opt)]
    (resp-util/ok articles-comments)))

(defn get-articles-comments-by-id [{:keys [db]} id]
  (log/debug "Get article comment " id)
  (let [article-comment (article-comment-db/get-by-id db id)]
    (resp-util/ok article-comment)))

(defn delete-articles-comments-by-id [{:keys [db]} id]
  (log/debug "Delete article comment " id)
  (let [_ (article-comment-db/delete! db id)]
    (resp-util/ok {})))

(defn delete-articles-comments-by-ids [{:keys [db]} id-set]
  (log/debug "Delete article comment " id-set)
  (let [_ (article-comment-db/delete-by-id-set! db id-set)]
    (resp-util/ok {})))

(defn push! [{:keys [db]} article]
  (let [old-article (article-db/get-by-id db (:id article))]
    (if (= (:push-flag old-article) 1) 
      (resp-util/bad-request "Aritcle been pushed")
      (let [push-time (java.time.LocalDateTime/now)
            result (article-db/push! db (assoc article 
                                               :push_time push-time
                                               :push_flag 1))]
        (resp-util/ok {})))))

(defn save-comment! [{:keys [db]} comment]
  (let [result (article-db/save-comment! db comment)]
    (resp-util/ok {})))
