(ns jiesoul.handlers.auth-handler
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.middleware.auth-middleware :refer [create-user-token]]
            [jiesoul.models.token-model :as token-model]
            [jiesoul.models.users-model :as user-model]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]))

(defn login [db]
  (fn [req]
    (let [username (ru/parse-body req :username)
          password (ru/parse-body req :password)
          user (user-model/get-user-by-name db username)]
      (log/debug "username: " username " password: " password " is loading.")
      (if (and user (buddy-hashers/check password (:password user)))
        (let [token (create-user-token db user)]
          (resp/response  {:status :ok
                           :message "login ok"
                           :data {:token token
                                  :user (dissoc user :password)}}))
        (resp/bad-request {:status :error
                           :message "用户名或密码错误"})))))

(defn logout [db]
  (fn [req]
    (let [token (ru/parse-header req "Token")]
      (token-model/disable-user-token db token)
      (resp/response {:status :ok
                      :message "Logout success!!"}))))