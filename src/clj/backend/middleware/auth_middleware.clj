(ns backend.middleware.auth-middleware
  (:require [buddy.auth.backends.token :as backends]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [backend.util.req-uitl :as req-util]
            [backend.db.user-token-db :as user-token-db]
            [backend.db.user-db :as user-db]
            [ring.util.response :as resp]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

;; 盐
(def private-key "soul")

(defn random-token
  []
  (let [randomdata (nonce/random-bytes 32)]
    (codecs/bytes->hex randomdata)))

(def defautlt-valid-seconds 3600)

(defn create-user-token
  "创建 Token"
  [db user-id & {:keys [valid-seconds] :or {valid-seconds defautlt-valid-seconds}}]
  (let [create-time (java.time.Instant/now)
        expires-time (.plusSeconds create-time valid-seconds)
        token (random-token)
        _ (user-token-db/save-user-token db {:user_id user-id
                                             :token token
                                             :create_time create-time
                                             :expires_time expires-time})]
    token))

(defn my-unauthorized-handler
  [request message]
  (-> (resp/bad-request {:status :failed
                         :message message})
      (assoc :status 403)))

(defn my-authfn
  [requeset token]
  (println (str "token: " token)))

(def auth-backend
  (backends/token-backend {:authfn my-authfn
                           :unauthorized-handler my-unauthorized-handler}))

(defn wrap-auth [handler env role]
  (fn [request]
    (let [db (:db env)
          token (req-util/parse-header request "Token")
          user-token (user-token-db/get-user-token-by-token db token)
          now (java.time.Instant/now)]
      (log/debug "user-token: " user-token)
      (if (and user-token (.isAfter (java.time.Instant/parse (:user_token/expires_time user-token)) now))
        (let [user-id (:user_token/user_id user-token)
              user (user-db/get-user-by-id db user-id)
              _ (log/debug "auth user: " user)
              roles (-> (:users/roles user) (str/split #",") (set))]
          (if (contains? roles role)
            (let [id (:user_token/id user-token)
                  _ (user-token-db/update-user-token-expires-time db id (.plusSeconds now defautlt-valid-seconds))
                  _ (log/debug "update user token expires time " id)]
              (handler request))
            (my-unauthorized-handler request "用户无权限！")))
        (my-unauthorized-handler request "Token 已过期！！")))))