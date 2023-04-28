(ns backend.util.req-uitl 
  (:require [clojure.tools.logging :as log]))

(def DEFAULT-PAGE 1)
(def DEFAULT-PAGE-SIZE 10)

(defn parse-header
  [request token-name]
  (log/debug "parse header request: " (:header (:parameters request)))
  (some->> (-> request :parameters :header :authorization)
           (re-find (re-pattern (str "^" token-name " (.+)$")))
           (second)))

(defn parse-body
  [req key]
  (get-in req [:body-params key]))

(defn parse-path 
  [req key]
  (get-in req [:parameters :path key]))

(defn parse-query
  [req]
  (let [query (get-in req [:parameters :query])
        page (or (get query :page) DEFAULT-PAGE)
        page-size (or (get query :page-size) DEFAULT-PAGE-SIZE)
        _ (log/debug "parameters query " query)]
    (assoc query :page page :page-size page-size)))