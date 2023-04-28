(ns backend.db.article-db
  (:require [backend.util.db-util :as du]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log]))

(defn query [db opts]
  (let [[ws wv] (du/opt-to-sql opts)
        [ps pv] (du/opt-to-page opts) 
        q-sql (into [(str "select * from article " ws ps)] (into wv pv))
        articles (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from article " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:articles articles
     :total total
     :opts opts}))

(defn create! [db {:keys [detail id] :as article}]
  (try 
    (with-open [con (jdbc/get-connection db)]
      (let [detail (assoc detail :article_id id)]
        (jdbc/with-transaction [tx con]
          (sql/insert! tx :article (dissoc article :detail))
        (sql/insert! tx :article_detail detail)
          )))
    (catch java.sql.SQLException se (throw (ex-info "insert article: " se)))))

(defn update! [db {:keys [id detail] :as article}]
  (jdbc/with-transaction [tx db]
    (sql/update! tx :article_detail detail {:id id})
    (sql/update! tx :article (dissoc article :detail) {:id id})))

(defn delete! [db id]
  (jdbc/with-transaction [tx db]
    (sql/delete! tx :article_comment ["article_id = ?" id])
    (sql/delete! tx :article_detail {:article_id id})
    (sql/delete! tx :article {:id id})))

(defn get-by-id [db id]
  (sql/get-by-id db :article id {:builder-fn rs/as-unqualified-maps}))

(defn get-detail-by-article-id [db article-id]
  (sql/get-by-id db :article_detail {:article_id article-id} {:builder-fn rs/as-unqualified-maps}))
