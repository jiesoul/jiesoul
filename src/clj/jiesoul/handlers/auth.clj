(ns jiesoul.handlers.auth
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.middleware.auth :refer [create-user-token]]
            [jiesoul.models.token :as token-model]
            [jiesoul.models.users :as user-model]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]))

(defn login [db]
  (fn [req]
    (let [username (ru/parse-body req :username)
          password (ru/parse-body req :password)
          user (user-model/get-user-by-name db username)]
      (log/debug "req body: " (get-in req [:body]))
      (log/debug "username: " username " password: " password " is loading.")
      (if (and user (buddy-hashers/check password (:password user)))
        (let [token (create-user-token db user)]
          (resp/response  {:data {:token token
                                  :user (dissoc user :password)}}))
        (resp/bad-request {:error "用户名或密码错误"})))))

(defn logout [db]
  (fn [req]
    (let [token (ru/parse-header req "Token")]
      (token-model/disable-user-token db token)
      (resp/response {:message "Logout success!!"}))))