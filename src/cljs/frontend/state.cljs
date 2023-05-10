(ns frontend.state 
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub 
 ::current-route 
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 ::current-route-result
 (fn [db]
   (get-in db [:current-route :result])))

(re-frame/reg-sub
 ::current-route-edit
 (fn [db]
   (get-in db [:current-route :edit])))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))

(re-frame/reg-sub
 ::modal-backdrop-show?
 (fn [db]
   (:modal-backdrop-show? db)))

(re-frame/reg-sub
 ::modal-back-show?
 (fn [db] 
   (:modal-show? db)))

(re-frame/reg-sub
 ::login-user
 (fn [db]
   (:login-user db)))

(re-frame/reg-sub
 ::login-status
 (fn [db]
   (:login-status db)))

(re-frame/reg-sub
 ::debug
 (fn [db]
   (:debug db)))

