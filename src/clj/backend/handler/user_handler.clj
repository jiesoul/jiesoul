(ns backend.handler.user-handler
  (:require [backend.db.user-db :as user-db]
            [backend.util.resp-util :as resp-util]
            [taoensso.timbre :as log]
            [buddy.hashers :as buddy-hashers]))

(defn query-users [env query]
  (log/debug "query users request params: "  query)
  (let [db (:db env)
        result (user-db/query-users db query)]
    (resp-util/ok result)))

(defn create-user! [env user]
  (log/debug "Create user " user)
  (let [db (:db env)
        create-time (java.time.Instant/now)
        password (:password user)
        new-user (user-db/create-user! db (assoc user
                                                 :create_time create-time
                                                 :password (buddy-hashers/derive password)))]
    (resp-util/ok {:user new-user})))

(defn update-user! [env user]
  (log/debug "update user " user)
  (let [db (:db env)
        db-user (user-db/get-user-by-id db (:id user))
        password (:passowrd user)]
    (if db-user
      (let [_ (user-db/update-user! db (assoc user :password (buddy-hashers/derive password)))]
        (resp-util/ok {}))
      (resp-util/not-found "无效的用户ID"))))

(defn get-user [env id]
  (log/debug "get user id" id)
  (let [db (:db env)
        user (user-db/get-user-by-id db id)]
    (if user
      (resp-util/ok (dissoc user :password))
      (resp-util/not-found "无效的用户ID"))))

(defn delete-user! [env id]
  (log/debug "Delete user id " id)
  (let [db (:db env)
        user ((user-db/get-user-by-id db id))]
    (if user
      (do
        (user-db/delete-user! db id)
        (resp-util/ok {}))
      (resp-util/not-found "无效的用户ID"))))

(defn update-user-password! [env {:keys [id old-password new-password confirm-password] :as update-password}]
  (log/debug "Update user password " update-password)
  (let [db (:db env)]
    (if (not= new-password confirm-password)
      (resp-util/bad-request "新密码与确认密码不一致")
      (if (= old-password new-password)
        (resp-util/bad-request "new password and old password is same")
        (if-let [user (user-db/get-user-by-id db id)]
          (if (buddy-hashers/check old-password (:password user))
            (do
              (user-db/update-user-password! db id (buddy-hashers/derive new-password))
              (resp-util/ok {}))
            (resp-util/bad-request "旧密码错误"))
          (resp-util/not-found "用户不存在"))))))


