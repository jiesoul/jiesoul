(ns frontend.routes.login 
  (:require [frontend.http :as f-http]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [frontend.util :as f-uitl]
            [reagent.core :as r]
            [frontend.util :as f-util]))

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
   (f-uitl/clog "reg-event-db failed" db)
   (assoc-in db [:login :response] {:ret :failed 
                                    :msg (get-in res-body {:response :msg})})))


(re-frame/reg-event-db
 ::save-username
 (fn [db [_ username]]
   (-> db 
       (assoc-in [:username] username))))

(re-frame/reg-sub
 ::login-response
 (fn [db]
   (f-uitl/clog "reg-sub" db)
   (:response (:login db))))

(re-frame/reg-event-fx
 ::login-user 
 (fn [{:keys [db]} [_ user-data]]
   (f-uitl/clog "login-user, user-data" user-data)
   (f-http/http-post db "/api/login" user-data ::login-ret-ok ::login-reg-failed)))

(defn login []
  (let [login-data (r/atom (empty-creds))]
    (fn []
      (let [_ (f-uitl/clog "Enter login")
            title "You need to login"
            {:keys [ret _msg]} @(re-frame/subscribe [::login-response])
            _ (when (= ret :ok) (re-frame/dispatch [::f-state/navigate ::f-state/product-group]))
            ]
        [:div.app
         [:div.p-4
          [:p.text-left.text-lg.font-bold.p-4 title]
          (when (= ret :failed)
            [:div {:classname "flex grow w-3/4 p-4"}
             (re-frame/dispatch [::save-username nil])
             [f-uitl/error-message "Login failed!" "Username or password is wrong."]])]
         [:div.flex.grow.justify-center.items-center
          [:div {:classname "flex grow w-1/2 p-4"}
           [:from 
            [:div.mt-3
             (f-util/input "Username" :username "text" login-data)
             (f-util/input "Password" :password "password" login-data)
             [:div.flex.flex-col.justify-center.items-center.mt-5
              [:button {:classname "login-button"
                        :on-click (fn [e]
                                    (.preventDefault e)
                                    (re-frame/dispatch [::login-user @login-data])
                                    (re-frame/dispatch [::save-username (:username @login-data)])
                                    )}
               "Login"]]]]]]]))))
