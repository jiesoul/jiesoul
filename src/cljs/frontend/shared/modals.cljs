(ns frontend.shared.modals 
  (:require [frontend.shared.svg :as svg]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::set-modal-show
 (fn [db modal]
   (f-util/clog "set modal show")
   (-> db 
       (assoc :modal-show? true)
       (assoc-in [:modal] modal))))

(re-frame/reg-event-db
 ::set-modal-hidden
 (fn [db _]
   (-> db
       (assoc-in [:modal-show?] false)
       (assoc :modal nil))))

(defn modal [show? props & children] 
  (let [{:keys [id title on-close]} props] 
    [:div {:id id
           :tab-index "-1"
           :class (str (if show? "" "hidden ") "flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 
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
                  :on-click (if on-close on-close #(re-frame/dispatch [::set-modal-hidden]))}
         (svg/close)]]
        ;; Modal body
       children]]]))