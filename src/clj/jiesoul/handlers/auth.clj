(ns jiesoul.handlers.auth
  (:require [inertia.middleware :as inertia]))

(defn login []
  (inertia/render "Auth/Login"))

(defn login-auth [db]
  )

(defn logout []
  )