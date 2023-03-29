(ns backend.handler.user-handler
  (:require [backend.db.user-db :as user-db]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]
            [buddy.hashers :as buddy-hashers]))

(defn query-users [env opt]
  (log/debug "query users request params: "  opt)
  (let [db (:db env)
        users (user-db/query-users db opt)]
    (resp/response {:status :ok
                    :data {:users users}})))

(defn create-user! [env user]
  (log/debug "Create user " user)
  (let [db (:db env)
        create-time (java.time.Instant/now)
        password (:password user)
        new-user (user-db/create-user! db (assoc user
                                                 :create_time create-time
                                                 :password (buddy-hashers/derive password)))]
    (resp/response {:status :ok
                    :data {:user new-user}})))

(defn update-user! [env user]
  (log/debug "update user " user)
  (let [db (:db env)
        db-user (user-db/get-user-by-id db (:id user))]
    (if db-user
      (do
        (user-db/update-user! db user)
        (resp/response {:status :ok}))
      (resp/bad-request {:status :failed
                         :message "无效的用户ID"}))))

(defn get-user [env id]
  (log/debug "get user id" id)
  (let [db (:db env)
        user (user-db/get-user-by-id db id)]
    (if user
      (resp/response {:status :ok
                      :data user})
      (resp/bad-request {:status :failed
                         :message "无效的用户ID"}))))

(defn delete-user! [env id]
  (log/debug "Delete user id " id)
  (let [db (:db env)
        user ((user-db/get-user-by-id db id))]
    (if user
      (do
        (user-db/delete-user! db id)
        (resp/response {:status :failed
                        :message "成功删除用户"}))
      (resp/bad-request {:status :failed
                         :message "无效的用户ID"}))))

(defn update-user-password! [env {:keys [id old-password new-password confirm-password]}]
  (log/debug "Update user password " id)
  (let [db (:db env)]
    (if (not= new-password confirm-password)
      (resp/bad-request {:status :failed
                         :message "新密码与确认密码不一致"})
      (let [user (user-db/get-user-by-id db id)]
        (if (and user (buddy-hashers/check old-password (:password user)))
          (do
            (user-db/update-user-password! db {:id id :password new-password})
            (resp/response {:status :ok
                            :messsage "密码修改成功"}))
          (resp/bad-request {:status :failed
                             :message "旧密码错误或用户不存在"}))))))


