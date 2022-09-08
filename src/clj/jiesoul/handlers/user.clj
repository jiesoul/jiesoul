(ns jiesoul.handlers.user
  (:require [jiesoul.models.users :as user-model]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]
            [jiesoul.req-uitls :as req-utils]))

(defn get-users [db]
  (fn [req]
    (log/debug "request params: " (:parameters req))
    (let [where (req-utils/parse-query req)
          users (user-model/get-users db where)]
      (resp/response {:data users}))))

(defn create-user! [db]
  (fn [req] 
    (let [username (ru/parse-body req :username)])))

(defn get-user [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          user (user-model/get-user-by-id db id)]
      (resp/response {:data user}))))


