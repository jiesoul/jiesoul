(ns frontend.shared.buttons
  (:require [frontend.shared.svg :as svg]))

(defn loading-button [{:keys [loading class]} & children]
  (let [class (str class " flex items-center focus:outline-none"
                   (when loading " pointer-events-none bg-opacity-75 select-none"))]
    [:button {:class class
              :disabled loading}
     (when loading [:div.mr-2.btn-spinner])
     children]))

(defn delete-button [{:keys [on-delete]} & children]
  (into
   [:button {:class "text-red-600 focus:outline-none hover:underline"
             :tab-index -1
             :type "button"
             :on-click on-delete}]
   children))

(defn new-button [{:keys [on-click]} & children]
  (into
   [:button {:type "button"
             :class "text-red-700 hover:text-white border border-red-700 
                    hover:bg-red-800 focus:ring-4 focus:outline-none 
                    focus:ring-red-300 font-medium rounded-lg text-sm 
                    px-5 py-2.5 text-center mr-2 mb-2 dark:border-red-500 
                    dark:text-red-500 dark:hover:text-white dark:hover:bg-red-600 
                    dark:focus:ring-red-900"
             :on-click on-click}
    children]))

(defn query-button [{:keys [on-query]} & children]
  (into
   [:button {:type "button"
             :class "text-blue-700 hover:text-white border border-blue-700 hover:bg-blue-800 
                     focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg 
                     text-sm px-5 py-2.5 text-center inline-flex item-center mr-2 mb-2 dark:border-blue-500 
                     dark:text-blue-500 dark:hover:text-white dark:hover:bg-blue-500 
                     dark:focus:ring-blue-800"
             :on-click on-query}
    (svg/search)
    children]))

