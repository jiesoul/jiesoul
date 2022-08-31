(ns jiesoul.handlers.auth
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.middleware.auth :refer [create-user-token]]
            [jiesoul.models.users :as user-model]
            [jiesoul.models.token :as token-model]
            [jiesoul.middleware.auth :refer [parse-header]]
            [ring.util.response :as resp]
            [java-time :as jt]))

(defn login-authenticate [db]
  (fn [req]
    (let [username (-> req :body-params :username)
          password (-> req :body-params :password)
          user (user-model/get-user-by-name db username)]
      (if (and user (buddy-hashers/check password (:password user)))
        (let [token (create-user-token db user)]
          (resp/response  {:token token}))
        (resp/bad-request {:error "用户名或密码错误"})))))

(defn logout [db]
  (fn [req]
    (let [token (parse-header req "Token")]
      (token-model/disable-user-token db token)
      (resp/response {:message "Logout success!!"}))))