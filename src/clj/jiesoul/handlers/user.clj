(ns jiesoul.handlers.user
  (:require [jiesoul.models.users :as user-model]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]))

(defn get-users [db]
  (fn [req]
    (log/debug "request params: " (:parameters req))
    (let [users (user-model/get-users db)]
      (resp/response {:data users}))))

(defn create-user! [db]
  (fn [req] 
    (let [username (ru/parse-body req :username)])))

(defn get-user [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          user (user-model/get-user-by-id db id)]
      (resp/response {:data user}))))


