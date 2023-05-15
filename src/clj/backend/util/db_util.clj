(ns backend.util.db-util 
  (:require [clojure.string :as str]
            [next.jdbc.result-set :as rs]
            [clojure.tools.logging :as log]))

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

(defn kebab-map [m]
  )

(defn as-kebab-maps [rs opts]
  (let [kebab #(str/replace % #"_" "-")]
    (rs/as-modified-maps rs (assoc opts :qualifier-fn kebab :label-fn kebab))))

(defn populate 
  [_ db-type]
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

(defn not-blank [s]
  (if (or (nil? s) (str/blank? (str/trim s))) nil (str/trim s)))

(defn sort-convert
  [opts]
  (let [sort (get opts :sort)]
    (not-blank sort)))

(defn op-convert
  [s]
  (loop [sql s w "" v []]
    (if (seq sql)
      (let [fst (first sql)
            snd (second sql)
            [ssql ww vv] (case fst
                           "eq" [(nnext sql) (str w " = ?  ") (conj v (del-qu snd))]
                           "lk" [(nnext sql) (str w " like ? ") (conj v (str "%" (del-qu snd) "%"))]
                           "ne" [(nnext sql) (str w " != ?") (conj v snd)]
                           "gt" [(nnext sql) (str w " > ? ") (conj v snd)]
                           "ge" [(nnext sql) (str w " >= ? ") (conj v snd)]
                           "lt" [(nnext sql) (str w " < ? ") (conj v snd)]
                           "le" [(nnext sql) (str w " <= ? ") (conj v snd)]
                           [(next sql) (str w " " fst " ") v])]
        (recur ssql ww vv))
      [w v])))

(defn filter-convert
  [opts]
  (if-let [filter (not-blank (get opts :filter))] 
    (-> filter
        (str/split #" +")
        op-convert)
    nil))

(defn page-convert
  [opts]
  (let [page (or (get opts :page) 1)
        page-size (or (get opts :page-size) 10)]
    [page-size (* page-size (dec page))]))

;; TODO
(defn search-convert [query]
  nil)

(defn opt-convert 
  [opts]
  (let [cq {:sort (sort-convert opts)
            :filter (filter-convert opts)
            :q (search-convert opts)}]
    cq))

(defn opt-to-sql [opts]
  (let [{:keys [filter]} (opt-convert opts)
        [s v] ["" []]
        [s v] (if filter
                [(str s " where " (first filter)) (into v (second filter))]
                [s v])
        _ (log/debug "opt to sql: [" s v "]")]
    [s v]))

(defn opt-to-page [opt]
  (let [page (page-convert opt)]
    (if page
      [(str " limit ? offset ? ") (into [] page)]
      ["" []])))

(defn opt-to-sort [opt]
  (if-let [sort (not-blank (:sort opt))]
    (str " order by " sort) 
    ""))