(ns frontend.routes.login 
  (:require [frontend.http :as f-http]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [frontend.util :as f-util]
            [reagent.core :as r]))

(defn empty-creds []
  {:username "" :password ""})

(re-frame/reg-event-db
 ::save-username
 (fn [db [_ username]]
   (-> db 
       (assoc-in [:username] username))))

(re-frame/reg-sub
 ::login-response
 (fn [db]
   (f-util/clog "reg-sub" db)
   (:response (:login db))))

(re-frame/reg-event-db
 ::login-ret-ok
 (fn [db [_ res-body]]
   (f-util/clog "login ok res-body" res-body)
   (-> db
       (assoc-in [:login :response] res-body)
       (assoc-in [:token] (:token (:data res-body)))
       (assoc-in [:login-user] (:user (:data res-body)))
       (assoc-in [:login-status] :logged-in))))

(re-frame/reg-event-db
 ::login-ret-failed
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db failed" (:response res-body))
   (assoc-in db [:login :response] (:response res-body))))

(re-frame/reg-event-fx
 ::login-user 
 (fn [{:keys [db]} [_ user-data]]
   (f-util/clog "login-user, user-data" user-data)
   (f-http/http-post db (f-http/api-uri "/login") user-data ::login-ret-ok ::login-ret-failed)))

(re-frame/reg-event-fx
 ::f-state/logout
 (fn [cofx [_]]
   (let [db (:db cofx)]
     {:db (-> db
              (assoc-in [:login] nil)
              (assoc-in [:username] nil)
              (assoc-in [:login-status] nil)
              (assoc-in [:token] nil))
      :fx [[:dispatch [::f-state/navigate ::f-state/login]]]})))

(def css-input "form-input mt-1 block w-full rounded-md focus:border-indigo-600")

(defn form-input [{:keys [label name type on-change]}]
  [:label {:class "block mt-3"}
   [:span {:class "text-gray-700"} label]
   [:input {:class css-input
            :type type
            :name name
            :on-change on-change}]])

(defn login []
  (let [login-data (r/atom (empty-creds))]
    (fn []
      (let [_ (f-util/clog "Enter login")
            title "Login"
            {:keys [status msg error]} @(re-frame/subscribe [::login-response])
            _ (when-not error (re-frame/dispatch [::f-state/navigate ::f-state/dashboard]))] 
        [:div {:class "flex justify-center items-center h-screen bg-gray-200 px-6"}
         [:div {:class "p-6 max-w-sm w-full bg-white shadow-md rounded-md"}
          [:div {:class "flex justify-center items-center"}
           [:span {:class "text-gray-700 font-semibold text-2xl"} title]] 
          (when (= status "failed")
            [:div {:class "bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"}
             (re-frame/dispatch [::save-username nil])
             [f-util/error-message "Login failed!" msg]])
          [:form {:class "mt-4"}  
           (form-input {:label "Username"  
                        :type "text"
                        :name "username" 
                        :on-change #(swap! login-data assoc :username (.. % -target -value))})
           (form-input {:label "Password"
                        :type "password"
                        :name "password"
                        :on-change #(swap! login-data assoc :password (.. % -target -value))})
           [:div {:class "flex justify-center items-center mt-4"}]
           [:div {:class "mt-6"}
            [:button {:class "py-2 px-4 text-center bg-indigo-600 rounded-md w-full text-white text-sm hover:bg-indigo-500"
                      :on-click (fn [e]
                                  (.preventDefault e)
                                  (re-frame/dispatch [::login-user @login-data]))}
             "Login"]]]]]))))
