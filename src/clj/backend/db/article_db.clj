(ns backend.db.article-db
  (:require [backend.util.db-util :as du]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.tools.logging :as log] 
            [backend.db.tag-db :as tag-db]
            [clojure.string :as str]))

(defn query [db opts]
  (let [[ws wv] (du/opt-to-sql opts)
        [ps pv] (du/opt-to-page opts) 
        q-sql (into [(str "select * from article " ws ps)] (into wv pv))
        _ (log/debug "query sql: " q-sql)
        articles (sql/query db q-sql {:builder-fn rs/as-unqualified-kebab-maps})
        t-sql (into [(str "select count(1) as c from article " ws)] wv)
        total (:c (first (sql/query db t-sql)))]
    {:list articles
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
    (sql/update! tx :article_detail detail {:article_id id})
    (sql/update! tx :article (dissoc article :detail) {:id id})))

(defn push! [db { :keys [id tags] :as article}]
  (jdbc/with-transaction [tx db]
    (sql/update! tx :article article {:id id})
    (when-not (str/blank? tags) 
      (log/debug "create tag " tags)
      (loop [t (str/split tags #" ")]
        (if (seq t)
          (let [name (first t)
                result (tag-db/get-by-name tx name)]
            (if (seq result) 
              (recur (rest t))
              (do (tag-db/create! tx {:name name})
                  (recur (rest t)))))
          nil)))))

(defn save-comment! [db comment]
  (jdbc/with-transaction [tx db]
    (sql/insert! tx :article_comment comment)
    (jdbc/execute! tx "update article set comment_count = comment_count + 1 where id = ? " (:article_id comment))))

(defn delete! [db id]
  (jdbc/with-transaction [tx db]
    (sql/delete! tx :article_comment ["article_id = ?" id])
    (sql/delete! tx :article_detail {:article_id id})
    (sql/delete! tx :article {:id id})))

(defn get-detail-by-article-id [db article-id]
  (sql/find-by-keys db :article_detail {:article_id article-id} {:builder-fn rs/as-unqualified-kebab-maps}))

(defn get-by-id [db id]
  (jdbc/with-transaction [tx db]
    (let [article (sql/get-by-id tx :article id {:builder-fn rs/as-unqualified-kebab-maps})
          _ (log/debug "article: " article)
          detail (get-detail-by-article-id tx id)]
      (assoc article :detail (first detail)))))


