(ns jiesoul.user.handler
  (:require [jiesoul.user.db :as db]
            [jiesoul.req-uitls :as ru]
            [ring.util.response :as resp]
            [taoensso.timbre :as log]
            [buddy.hashers :as buddy-hashers]))

(defn get-users [db]
  (fn [req]
    (log/debug "request params: " (:parameters req))
    (let [opt (ru/parse-query req)
          users (db/get-users db opt)]
      (resp/response {:status :ok
                      :data {:users users}}))))

(defn create-user! [db]
  (fn [req] 
    (let [user (ru/parse-body req :user)
          create-time (java.time.Instant/now)
          password (:password user)
          new-user (db/create-user! db (assoc user 
                                                      :create_time create-time
                                                      :password (buddy-hashers/derive password)))]
      (log/debug "new user: " new-user)
      (resp/response {:message "成功创建用户"
                      :data {:user new-user}}))))

(defn update-user-info! [db]
  (fn [req]
    (let [user (ru/parse-body req :user)
          db-user (db/get-user-by-id db (:id user))]
      (if db-user
        (do
          (db/update-user! db user)
          (resp/response {:message "更新成功"}))
        (resp/bad-request {:message "无效的用户ID"})))))

(defn get-user [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          user (db/get-user-by-id db id)]
      (if user
        (resp/response {:data user})
        (resp/bad-request {:message "无效的用户ID"})))))

(defn delete-user! [db]
  (fn [req]
    (let [id (ru/parse-path req :id)
          user ((db/get-user-by-id db id))]
      (if user
        (do 
          (db/delete-user! db id)
          (resp/response {:message "成功删除用户"}))
        (resp/bad-request {:message "无效的用户ID"})))))

(defn update-password! [db]
  (fn [req]
    (let [{:keys [id old-password new-password confirm-password]} (ru/parse-body req :user)]
      (if (not= new-password confirm-password)
        (resp/bad-request {:message "新密码与确认密码不一致"})
        (let [user (db/get-user-by-id db id)]
          (if (and user (buddy-hashers/check old-password (:password user)))
            (do 
              (db/update-user-password! db {:id id :password new-password})
              (resp/response {:messsage "密码修改成功"}))
            (resp/bad-request {:message "旧密码错误或用户不存在"})))))))


