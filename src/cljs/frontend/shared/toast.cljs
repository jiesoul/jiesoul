(ns frontend.shared.toast 
  (:require [frontend.shared.svg :as svg]
            [re-frame.core :as re-frame]
            [frontend.state :as f-state]))

(defn toast-success []
  (let [toast-success (re-frame/subscribe [::f-state/toast-success])]
    (when @toast-success
      [:div {:id "toast-sucess"
             :class "flex items-center w-full max-w-xs p-4 mb-4 text-gray-500 
                 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800"
             :role "alert"}
       [:div {:class "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 
                  text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200"}
        (svg/success)]
       [:div {:class "ml-3 text-sm font-normal"} @toast-success]
       [:button {:type "button"
                 :class "ml-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 
                       rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 
                       inline-flex h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 
                       dark:hover:bg-gray-700"
                 :on-click #(re-frame/dispatch [::f-state/set-toast-success nil])}
        (svg/close)]])))

(defn toast-dranger []
  (let [toast-dranger (re-frame/subscribe [::f-state/toast-dranger])]
    (when @toast-dranger
      [:div {:id "toast-sucess"
             :class "flex items-center w-full max-w-xs p-4 mb-4 text-gray-500 
                 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800"
             :role "alert"}
       [:div {:class "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-red-500 
                      bg-red-100 rounded-lg dark:bg-red-800 dark:text-red-200"}
        (svg/success)]
       [:div {:class "ml-3 text-sm font-normal"} @toast-dranger]
       [:button {:type "button"
                 :class "ml-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 
                       rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 
                       inline-flex h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 
                       dark:hover:bg-gray-700"
                 :on-click #(re-frame/dispatch [::f-state/set-toast-dranger nil])}
        (svg/close)]])))

(defn toast-warning []
  (let [toast-warning (re-frame/subscribe [::f-state/toast-warning])]
    (when @toast-warning
      [:div {:id "toast-sucess"
             :class "flex items-center w-full max-w-xs p-4 mb-4 text-gray-500 
                 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800"
             :role "alert"}
       [:div {:class "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 
                  text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200"}
        (svg/success)]
       [:div {:class "ml-3 text-sm font-normal"} @toast-warning]
       [:button {:type "button"
                 :class "ml-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 
                       rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 
                       inline-flex h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 
                       dark:hover:bg-gray-700"
                 :on-click #(re-frame/dispatch [::f-state/set-toast-warning nil])}
        (svg/close)]])))

(defn toast []
  [:<> 
   [toast-success]
   [toast-dranger]
   [toast-warning]])