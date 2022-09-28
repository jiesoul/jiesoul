(ns jiesoul.auth.handler
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.auth.middleware :refer [create-user-token]]
            [jiesoul.auth.user-token-db :as user-token-db]
            [jiesoul.user.db :as user-db]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [inertia.middleware :as inertia]
            [taoensso.timbre :as log]))

(defn login [db]
  (inertia/render "Auth/Login"))

(defn login-auth [db]
  (fn [req]
    (let [username (ru/parse-body req :username)
          password (ru/parse-body req :password)
          user (user-db/get-user-by-name db username)]
      (log/debug "username: " username " password: " password " is loading.")
      (if (and user (buddy-hashers/check password (:password user)))
        (let [token (create-user-token db user)]
          (resp/response  {:status :ok
                           :message "login ok"
                           :data {:token token
                                  :user (dissoc user :password)}}))
        (resp/response {:status :error
                           :message "用户名或密码错误"})))))

(defn logout [db]
  (fn [req]
    (let [token (ru/parse-header req "Token")]
      (user-token-db/disable-user-token db token)
      (resp/redirect "/login"))))