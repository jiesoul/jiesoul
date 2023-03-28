(ns backend.handler.auth-handler
  (:require [buddy.hashers :as buddy-hashers]
            [backend.middleware.auth-middleware :refer [create-user-token]]
            [backend.db.user-token-db :as user-token-db]
            [backend.db.user-db :as user-db]
            [backend.util.req-uitl :as ru]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn login-auth
  "login to backend."
  [env username password]
  (log/debug "Enter login auth. username: " username " password: " password "env: " env)
  (let [db (:db env)
        user (user-db/get-user-by-name db username)]
    (log/debug "user: " user)
    (if (and user (buddy-hashers/check password (:users/password user)))
      (let [token (create-user-token db (:users/id user))]
        (resp/response  {:status :ok
                         :data {:token token
                                :user (dissoc user :users/password)}}))
      (resp/response {:status :failed
                      :message "用户名或密码错误"}))))

(defn logout [env]
  (fn [req]
    (let [db (:db env)
          token (ru/parse-header req "Token")]
      (user-token-db/disable-user-token db token)
      (resp/response {:status :ok}))))