(ns jiesoul.req-uitls)

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