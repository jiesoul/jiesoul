(ns backend.util.req-uitl 
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]))

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
  [req]
  (if-let [sort (get-str-by-key req :sort)]
    (-> sort
        (sub-query))
    ""))

(defn op-convert
  [s]
  (log/debug "s: " s)
  (loop [sql s w "" v []]
    (if (empty? sql)
      [w v]
      (let [fst (first sql)
            snd (second sql)
            [ssql ww vv] (case fst
                           "eq" [(nnext sql) (str w " = ?") (conj v (subs snd 1 (dec (count snd))))]
                           "like" [(nnext sql) (str w " like ?") (conj v (str "%" (subs snd 1 (dec (count snd))) "%"))]
                           "ne" [(nnext sql) (str w " != ?") (conj v snd)]
                           "gt" [(nnext sql) (str w " > ?") (conj v snd)]
                           "ge" [(nnext sql) (str w " >= ?") (conj v snd)]
                           "lt" [(nnext sql) (str w " < ?") (conj v snd)]
                           "le" [(nnext sql) (str w " <= ?") (conj v snd)]
                           [(next sql) (str w fst) v])]
        (recur ssql ww vv)))))

(defn filter-convert 
  [req]
  (if-let [filter (get-str-by-key req :filter)]
    (-> filter
        sub-query
        (str/split #" +")
        op-convert)
    []))

(defn page-convert
  [req]
  (let [page (-> req
                 (get-str-by-key :page)
                 (str/split #"&"))
        [no size] (->> page
                       (map #(str/split % #"="))
                       (map second)
                       (map #(Integer/parseInt %)))
        offset (* (dec no) size)]
    [size offset]))

(defn parse-query
  [req]
  {:sort (sort-convert req)
   :filter (filter-convert req)
   :page (page-convert req)})