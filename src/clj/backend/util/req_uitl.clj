(ns backend.util.req-uitl 
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.walk :as walk]))

(def DEFAULT-PAGE 1)
(def DEFAULT-PER-PAGE 10)

(defn parse-header
  [request token-name]
  (log/debug "parse header request: " (:header (:parameters request)))
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

(defn parse-query
  [req]
  (let [query (get-in req [:parameters :query])
        page (or (get query :page) DEFAULT-PAGE)
        per-page (or (get query :per-page) DEFAULT-PER-PAGE)
        _ (log/debug "parameters query " query)]
    (assoc query :page page :per-page per-page)))