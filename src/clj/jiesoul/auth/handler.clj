(ns jiesoul.auth.handler
  (:require [buddy.hashers :as buddy-hashers]
            [jiesoul.auth.middleware :refer [create-user-token]]
            [jiesoul.auth.user-token-db :as user-token-db]
            [jiesoul.user.db :as user-db]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn login-auth
  "login to backend."
  [env username password]
  (log/debug "Enter login. username: " username " password: " password)
  (let [user (user-db/get-user-by-name env username)]
    (if (and user (buddy-hashers/check password (:password user)))
      (let [token (create-user-token env user)]
        (resp/response  {:status :ok
                         :message "login ok"
                         :data {:token token
                                :user (dissoc user :password)}}))
      (resp/response {:status :error
                      :message "用户名或密码错误"}))))

(defn logout [db]
  (fn [req]
    (let [token (ru/parse-header req "Token")]
      (user-token-db/disable-user-token db token)
      (resp/redirect "/login"))))