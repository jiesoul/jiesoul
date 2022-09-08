(ns jiesoul.req-uitls 
  (:require [clojure.string :as str]
            [clojure.string :as s]))

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

(defn op-convert 
  [s]
  (-> s
      (str/replace "eq" "=")
      (str/replace "ne" "!=")
      (str/replace "gt" ">")
      (str/replace "ge" ">=")
      (str/replace "lt" "<")
      (str/replace "le" "<=")))

(defn page-convert 
  [req]
  (let [page-no (-> req :query :page-no)
        page-size (-> req :qurey :page-size)]
    {:page-no page-no
     :page-size page-size}))

(defn sub-query 
  [s]
  (let [i (str/index-of s "=")
        l (count s)]
    (subs s (inc i) l)))

(defn get-str-by-key 
  [req key]
  (if-let [s (-> req :parameters :query key)]
    (->  s
         sub-query
         op-convert)
    ""))

(defn parse-query
  [req]
  {:sort (-> (get-str-by-key req :sort))
   :filter (-> req (get-str-by-key :filter))
   })