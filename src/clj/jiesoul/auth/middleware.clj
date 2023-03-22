(ns jiesoul.auth.middleware
  (:require [buddy.auth.backends.token :as backends]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [jiesoul.req-uitls :as req-utils]
            [jiesoul.auth.user-token-db :as user-token-db]
            [jiesoul.user.db :as user-db]
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
  (-> (resp/bad-request {:message message})
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
          token (req-utils/parse-header request "Token")
          user-token (user-token-db/get-user-token-by-token db token)
          now (java.time.Instant/now)]
      (log/debug "user-token: " user-token)
      (if (and user-token (.isAfter (java.time.Instant/parse (:user_token/expires_time user-token)) now))
        (let [user-id (:user_token/user_id user-token)
              user (user-db/get-user-by-id db user-id)
              _ (log/debug "auth user: " user)
              roles (-> (:users/roles user) (str/split #",") (set))]
          (if (contains? roles role)
            (do
              (user-token-db/update-user-token-expires-time db (-> user-token
                                                                   (assoc :user_token/expires_time (.plusSeconds now defautlt-valid-seconds))))
              (log/debug "user-token expires-time was updated!.")
              (handler request))
            (my-unauthorized-handler request "用户无权限！")))
        (my-unauthorized-handler request "Token 已过期！！")))))