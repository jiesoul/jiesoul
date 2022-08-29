(ns jiesoul.handlers.auth
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.middleware.auth :refer [create-token]]
            [jiesoul.models.users :as user-model]
            [ring.util.response :as resp]))

(defn login-authenticate [db]
  (fn [req]
    (let [username (-> req :body-params :username)
          password (-> req :body-params :password)
          user (user-model/get-user-by-name db username)]
      (if (and user (buddy-hashers/check password (:password user)))
        (resp/response  {:token (create-token db user)
                         :refresh-token (create-token db user {:valid-seconds 7200})})
        (resp/bad-request {:error "用户名密码错误"})))))

(defn logout [db]
  (fn [req]
    (resp/response {:message "ok"})))