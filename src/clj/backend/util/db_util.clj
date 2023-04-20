(ns backend.util.db-util 
  (:require [clojure.string :as str]
            [next.jdbc.result-set :as rs]))

(extend-protocol rs/ReadableColumn
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toInstant v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toInstant v)))

(defn as-kebab-maps [rs opts]
  (let [kebab #(str/replace % #"_" "-")]
    (rs/as-modified-maps rs (assoc opts :qualifier-fn kebab :label-fn kebab))))

(defn populate 
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str " generated always as identity "
                        " (start with 1, increment by 1) "
                        " primary key "))]
    auto-key))

(defn del-qu [s]
  (if (and (str/starts-with? s "'") (str/ends-with? s "'"))
    (let [end (dec (count s))]
      (subs s 1 end))
    s))

(defn sort-convert
  [query-params]
  (let [sort (get query-params :sort)]
    sort))

(defn op-convert
  [s]
  (loop [sql s w "" v []]
    (if (seq sql)
      (let [fst (first sql)
            snd (second sql)
            [ssql ww vv] (case fst
                           "eq" [(nnext sql) (str w " = ?  ") (conj v (del-qu snd))]
                           "like" [(nnext sql) (str w " like ? ") (conj v (str "'%" (del-qu snd) "%'"))]
                           "ne" [(nnext sql) (str w " != ?") (conj v snd)]
                           "gt" [(nnext sql) (str w " > ? ") (conj v snd)]
                           "ge" [(nnext sql) (str w " >= ? ") (conj v snd)]
                           "lt" [(nnext sql) (str w " < ? ") (conj v snd)]
                           "le" [(nnext sql) (str w " <= ? ") (conj v snd)]
                           [(next sql) (str w " " fst " ") v])]
        (recur ssql ww vv))
      [w v])))

(defn filter-convert
  [query]
  (if-let [filter (get query :filter)]
    (-> filter
        (str/split #" +")
        op-convert)
    nil))

(defn page-convert
  [query]
  (let [page (or (get query :page) 1)
        per-page (or (get query :per-page) 10)]
    [per-page (* per-page (dec page))]))

(defn search-convert
  [query]
  nil)

(defn query-convert 
  [query]
  {:sort (sort-convert query)
   :filter (filter-convert query)
   :page (page-convert query)
   :q (search-convert query)})

(defn opt-to-sql [s query]
  (let [{:keys [filter sort page] :as opt} (query-convert query)]
    (if (seq opt)
      (let [[s v] [s []]
            [s v] (if filter
                    [(str s " where " (first filter)) (into v (second filter))]
                    [s v])
            [s v] (if sort
                    [(str s " order by ? ") (conj v sort)]
                    [s v])
            [s v] (if page
                    [(str s " limit ? offset ? ") (into v page)]
                    [s v])]
        (into [s] v))
      s)))