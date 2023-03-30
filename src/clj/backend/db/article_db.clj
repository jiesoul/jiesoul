(ns backend.db.article-db
  (:require [next.jdbc.sql :as sql]
            [backend.util.db-util :as du]
            [next.jdbc :as jdbc]))

(defn query [db opt]
  (let [s "select * from article "]
    (sql/query db :article (du/opt-to-sql s opt))))

(defn create! [db {:keys [detail] :as article}]
  (with-open [con (jdbc/get-connection db)]
    (jdbc/with-transaction [tx con]
      (sql/insert! tx :article (dissoc article :detail))
      (sql/insert! tx :article_detail detail))))

(defn update! [db {:keys [id detail] :as article}]
  (jdbc/with-transaction [tx db]
    (sql/update! tx :article_detail detail {:id id})
    (sql/update! tx :article (dissoc article :detail) {:id id})))

(defn delete! [db id]
  (jdbc/with-transaction [tx db]
    (sql/delete! tx :article_comment ["article_id = ?" id])
    (sql/delete! tx :article_detail {:id id})
    (sql/delete! tx :article {:id id})))

(defn get-by-id [db id]
  (sql/get-by-id db :article id))

(defn get-detail-by-article-id [db article-id]
  (sql/get-by-id db :article_detail {:article_id article-id}))