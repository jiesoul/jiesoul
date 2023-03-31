(ns backend.handler.auth-handler
  (:require [backend.db.user-db :as user-db]
            [backend.db.user-token-db :as user-token-db]
            [backend.middleware.auth-middleware :refer [create-user-token]]
            [backend.util.req-uitl :as req-util]
            [backend.util.resp-util :as resp-util]
            [buddy.hashers :as buddy-hashers]
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
        (resp-util/ok   {:token token
                         :user (dissoc user :users/password)}))
      (resp-util/failed "用户名或密码错误"))))

(defn logout [env]
  (fn [req]
    (let [db (:db env)
          token (req-util/parse-header req "Token")]
      (user-token-db/disable-user-token db token)
      (resp-util/ok {}))))