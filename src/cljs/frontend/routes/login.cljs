(ns frontend.routes.login 
  (:require [frontend.http :as f-http]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [frontend.util :as f-util]
            [reagent.core :as r]))

(defn empty-creds []
  {:username "" :password ""})

(re-frame/reg-event-db 
 ::login-ret-ok
 (fn [db [_ res-body]]
   (-> db 
       (assoc-in [:login :response] {:ret :ok :msg (:msg res-body)})
       (assoc-in [:token] (:token res-body))
       (assoc-in [:login-status] :logged-in))))

(re-frame/reg-event-db
 ::login-ret-failed
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db failed" res-body)
   (assoc-in db [:login :response] {:ret :failed 
                                    :msg (get-in res-body {:response "msg"})})))

(re-frame/reg-event-db
 ::save-username
 (fn [db [_ username]]
   (-> db 
       (assoc-in [:username] username))))

(re-frame/reg-event-db
 ::save-password
 (fn [db [_ password]]
   (-> db
       (assoc-in [:password] password))))

(re-frame/reg-sub
 ::login-response
 (fn [db]
   (f-util/clog "reg-sub" db)
   (:response (:login db))))

(re-frame/reg-event-fx
 ::login-user 
 (fn [{:keys [db]} [_ user-data]]
   (f-util/clog "login-user, user-data" user-data)
   (f-http/http-post db "http://localhost:3000/api/v1/login" user-data ::login-ret-ok ::login-ret-failed)))

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

(defn login []
  (let [login-data (r/atom (empty-creds))]
    (fn []
      (let [_ (f-util/clog "Enter login")
            title "You need to login"
            {:keys [ret _msg]} @(re-frame/subscribe [::login-response])
            _ (when (= ret :ok) (re-frame/dispatch [::f-state/navigate ::f-state/product-group]))] 
        [:div {:class "relative flex flex-col rounded-xl bg-transparent bg-clip-border text-gray-700 shadow-none"}
         (when (= ret :failed)
           [:div {:class "bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"}
            (re-frame/dispatch [::save-username nil])
            [f-util/error-message "Login failed!" "Username or password is wrong."]])
         
         [:h4 {:class "block font-sans text-2xl font-semibold leading-snug tracking-normal text-blue-gray-900 antialiased"} 
          title]
         [:from {:class "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4"}
          [:div {:class "mb-4 flex flex-col gap-6"}
           [:div {:class "flex relative h-11"}
            [:div {:class "w-1/3"}
             [:label {:class "block text-gray-700 text-sm font-bold mb-2" :for "username"} "username: "]]
            [:div {:class "w-2/3"}
             [:input {:class "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                      :label "username"
                      :name "username" 
                      :on-change #(swap! login-data assoc :username (.. % -target -value))}]]]
           [:div {:class "flex md:items-center mb-6"}
            [:div {:class "w-1/3"}
             [:label {:class "block text-gray-700 text-sm font-bold mb-2" :for "password"} "Password"]]
            [:div {:class "w-2/3"}
             [:input {:class "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                      :label "Password"
                      :name "password"
                      :type "password"
                      :on-change #(swap! login-data assoc :password (.. % -target -value))}]]]]
          [:div {:class "md:flex md:items-center"}
           [:div {:class "md:w-1/3"}]
           [:div {:class "md:w-2/3"}
            [:button {:class "shadow bg-purple-500 hover:bg-purple-400 focus:shadow-outline focus:outline-none text-white font-bold py-2 px-4 rounded"
                      :on-click (fn [e]
                                  (.preventDefault e)
                                  (re-frame/dispatch [::login-user @login-data]))}
             "Login"]]]]]))))
