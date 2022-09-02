(ns jiesoul.middleware.auth 
  (:require [buddy.auth.backends.token :as backends]
            [buddy.auth.middleware :as buddy-auth-middleware]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.sign.jwt :as jwt]
            [jiesoul.req-uitls :as req-utils]
            [jiesoul.models.token :as token-model]
            [jiesoul.models.users :as user-model]
            [ring.util.response :as resp]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

;; 盐
(def private-key "soul")

(defn random-token
  []
  (let [randomdata (nonce/random-bytes 32)]
    (codecs/bytes->hex randomdata)))

(defn create-user-token
  "创建 Token"
  [db user & {:keys [valid-seconds] :or {valid-seconds 3600}}]
  (let [create-time (java.time.Instant/now)
        expires-time (.plusSeconds create-time valid-seconds)
        payload (-> user
                    (select-keys [:id :roles])
                    (assoc :exp expires-time))
        token (random-token)
        _ (token-model/save-user-token db {:user_id (:id user)
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

(defn wrap-auth [handler db role]
  (fn [request]
    (let [token (req-utils/parse-header request "Token")
          user-token (token-model/get-user-token-by-token db token)
          now (java.time.Instant/now)]
      (log/debug "user-token: " user-token)
      (if (and user-token (.isAfter (java.time.Instant/parse (:expires_time user-token)) now))
        (let [user-id (:user_id user-token)
              user (user-model/get-user-by-id db user-id)
              roles (-> (:roles user) (str/split #",") (set))]
          (log/debug "roles: " roles)
          (if (contains? roles role)
            (handler request)
            (my-unauthorized-handler request "用户无权限！")))
        (my-unauthorized-handler request "Token 已过期！！")))))