(ns frontend.shared.modals 
  (:require [frontend.shared.svg :as svg]
            [frontend.util :as f-util]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::f-state/init-modal
 (fn [db [_ _]]
   (assoc-in db [:current-route :modal] nil)))

(re-frame/reg-sub
 ::f-state/modal
 (fn [db _]
   (get-in db [:modal])))

(re-frame/reg-sub
 ::f-state/modal-back-show? 
 (fn [db _]
   (get-in db [:current-route :modal :backdrop-show?])))

(re-frame/reg-event-db
 ::f-state/set-modal-backdrop-show?
 (fn [db [_ state]]
   (assoc-in db [:current-route :modal :backdrop-show?] state)))

(re-frame/reg-sub
 ::f-state/new-modal-show?
 (fn [db]
   (get-in db [:current-route :modal :new-modal-show?])))

(re-frame/reg-event-fx
 ::f-state/show-new-modal
 (fn [{:keys [db]} [_ show?]]
   {:fx [[:dispatch [::f-state/set-modal-backdrop-show? show?]]]
    :db (assoc-in db [:current-route :modal :new-modal-show?] show?)}))

(re-frame/reg-sub
 ::f-state/edit-modal-show?
 (fn [db]
   (get-in db [:current-route :modal :edit-modal-show?])))

(re-frame/reg-event-fx
 ::f-state/show-edit-modal
 (fn [{:keys [db]} [_ show?]]
   {:fx [[:dispatch [::f-state/set-modal-backdrop-show? show?]]]
    :db (-> db
            (assoc-in [:current-route :modal :edit-modal-show?] show?))}))

(re-frame/reg-sub
 ::f-state/delete-modal-show?
 (fn [db]
   (get-in db [:current-route :modal :delete-modal-show?])))

(re-frame/reg-event-fx
 ::f-state/show-delete-modal
 (fn [{:keys [db]} [_ show?]]
   {:fx [[:dispatch [::f-state/set-modal-backdrop-show? show?]]]
    :db (-> db
            (assoc-in [:current-route :modal :delete-modal-show?] show?))}))

(defn modal [props & children] 
  (let [{:keys [id title on-close show?]} props]
    [:div {:id id
           :tab-index "-1"
           :class (str (if show? "" "hidden ") "flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-40 
                 justify-center items-center w-full inset-0 h-[calc(100%-1rem)] max-h-full")
           :role "dialog"}
     [:div {:class "relative p-4 w-full justify-center items-center max-w-2xl max-h-full"} 
        ;; Modal content
      [:div {:class "relative p-4 bg-white rounded-lg shadow dark:bg-gray-800 sm:p-5"}
         ;; Modal header
       [:div {:class "flex justify-between items-center pb-4 mb-4 rounded-t border-b sm:mb-5 dark:border-gray-600"} 
        [:h3 {:class "text-lg font-semibold text-gray-900 dark:text-white"}
         title]
        [:button {:type "button"
                  :class "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm 
                        p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
                  :data-modal-toggle id
                  :on-click (if on-close on-close #(re-frame/dispatch [::set-modal nil]))}
         (svg/close)]]
        ;; Modal body
       children]]]))

(defn default-modal [props & child] 
  (let [{:keys [show? id title on-close]} @(re-frame/subscribe [::f-state/modal])] 
    [:div {:id id
           :tab-index "-1"
           :class (str (if show? "" "hidden ") "flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-40 
                 justify-center items-center w-full inset-0 h-[calc(100%-1rem)] max-h-full")
           :role "dialog"}
     [:div {:class "relative p-4 w-full justify-center items-center max-w-2xl max-h-full"}
        ;; Modal content
      [:div {:class "relative p-4 bg-white rounded-lg shadow dark:bg-gray-800 sm:p-5"}
         ;; Modal header
       [:div {:class "flex justify-between items-center pb-4 mb-4 rounded-t border-b sm:mb-5 dark:border-gray-600"}
        [:h3 {:class "text-lg font-semibold text-gray-900 dark:text-white"}
         title]
        [:button {:type "button"
                  :class "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm 
                        p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
                  :data-modal-toggle id
                  :on-click (if on-close on-close #(re-frame/dispatch [::f-state/init-modal nil]))}
         (svg/close)]]
        ;; Modal body 
       child]]]))

(defn modal-back []
  (let [modal-show? @(re-frame/subscribe [::f-state/modal-back-show?])]
    [:div {:class (str (if modal-show? "" "hidden ") 
                       "bg-gray-900 bg-opacity-50 dark:bg-opacity-80 fixed inset-0 z-30")}]))