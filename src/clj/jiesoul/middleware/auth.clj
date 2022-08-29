(ns jiesoul.middleware.auth 
  (:require [buddy.auth.backends.token :refer [token-backend]]
            [buddy.auth.middleware :as buddy-auth-middleware]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.sign.jwt :as jwt]
            [jiesoul.models.token :as token-model]
            [ring.util.response :as resp]))

;; 盐
(def private-key "soul")

(defn random-token
  []
  (let [randomdata (nonce/random-bytes 32)]
    (codecs/bytes->hex randomdata)))

(defn create-token
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
  [request token-auth-middleware]
  (-> (resp/response "Unauthorized request")
      (assoc :status 403)))

(defn my-authfn
  [db requeset token]
  (println (str "token: " token)))

(def auth-backend
  (token-backend {:realm "admin"
                  :authfn my-authfn
                  :unauthorized-handler my-unauthorized-handler}))

(defn auth-middleware [handler id]
  (fn [request]
    (buddy-auth-middleware/wrap-authentication handler auth-backend)
    (handler (update request ::acc (fnil conj []) id))))