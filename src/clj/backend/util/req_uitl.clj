(ns backend.util.req-uitl 
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.walk :as walk]))

(defn parse-header
  [request token-name]
  (some->> (-> request :parameters :header :authorization)
           (re-find (re-pattern (str "^" token-name " (.+)$")))
           (second)))

(defn parse-body
  [req key]
  (get-in req [:parameters :body key]))

(defn parse-path 
  [req key]
  (get-in req [:parameters :path key]))

(defn sub-query 
  [s]
  (let [i (str/index-of s "=")
        l (count s)]
    (subs s (inc i) l)))

(defn get-str-by-key
  [req key]
  (-> req :parameters :query key))

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
                           "eq" [(nnext sql) (str w " = ?  ") (conj v snd)]
                           "like" [(nnext sql) (str w " like ? ") (conj v (str "%" snd "%"))]
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
        per-page (or (get query :per_page) 10)]
    [per-page (* per-page (dec page))]))

(defn search-convert
  [query]
  "")


(defn parse-query
  [req]
  (let [query (get-in req [:parameters :query])
        _ (log/debug "parameters query " query)]
    {:sort (sort-convert query)
     :filter (filter-convert query)
     :page (page-convert query)
     :q (search-convert query)}))