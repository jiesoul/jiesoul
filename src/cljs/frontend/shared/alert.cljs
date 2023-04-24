(ns frontend.shared.alert
  (:require [frontend.shared.svg :as svg]))

(defn drange-alert [{:keys [title content on-confirm on-cancel]}]
  [:div {:class "inset-x-0 top-10 mx-auto text-red-800 border border-red-300 rounded-lg bg-red-50 
                 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
   [:div {:class "flex items-center"}
    (svg/info)
    [:h3 title]]
   [:div {:class "mt-2 mb-4 text-sm"}
    content]
   [:div {:class "flex"}
    [:button {:class "text-white bg-red-800 hover:bg-red-900 focus:ring-4 focus:outline-none 
                      focus:ring-red-200 font-medium rounded-lg text-xs px-3 py-1.5 mr-2 text-center 
                      inline-flex items-center dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-800"
              :on-click on-confirm}]]
   [:button {:class "text-red-800 bg-transparent border border-red-800 hover:bg-red-900 hover:text-white 
                     focus:ring-4 focus:outline-none focus:ring-red-200 font-medium rounded-lg text-xs px-3 
                     py-1.5 text-center dark:hover:bg-red-600 dark:border-red-600 dark:text-red-400 
                     dark:hover:text-white dark:focus:ring-red-800"
             :on-click on-cancel}]])