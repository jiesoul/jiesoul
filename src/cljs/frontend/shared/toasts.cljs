(ns frontend.shared.toasts 
  (:require [frontend.shared.svg :as svg]
            [re-frame.core :as re-frame]))

(def css-toast-base "flex items-center w-full max-w-xs p-4 mb-4 text-gray-500 
                 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")

(def css-toast-info-content "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 
                  text-blue-500 bg-blue-100 rounded-lg dark:bg-blue-800 dark:text-blue-200")

(def css-toast-success-content "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 
                  text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200")

(def css-toast-drange-content "inline-flex items-center overflow-x-auto justify-center flex-shrink-0 w-8 h-8 
                  text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200")

(def css-toast-warning-content "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 
                  text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200")

(def css-toast-close-button "ml-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 
                       rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 
                       inline-flex h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 
                       dark:hover:bg-gray-700")

(def SUM-TOASTS 5)

(re-frame/reg-sub
 ::toasts
 (fn [db]
   (get-in db [:toasts])))

(re-frame/reg-event-db
 ::put
 (fn [db [_ toast]]
   (let [toasts (:toasts db)
         toasts (if toasts toasts #queue [])]
     (if (< (count toasts) SUM-TOASTS)
       (conj toasts toast)
       (-> toasts 
           pop
           (conj toast))))))

(re-frame/reg-event-db
 ::pop
 (fn [db [_ this]]))

(defn toasts []
  (let [toasts @(re-frame/subscribe [::toasts])]
    (when toasts
      [:div {:class "fixed top-5 right-5 z-50"}
       (for [{:keys [type content]} toasts]
         [:div {:id (str "toast-")
                :class css-toast-base
                :role "alert"}
          [:div {:class (case type
                          :success css-toast-success-content
                          :warning css-toast-warning-content
                          :error css-toast-drange-content
                          :into css-toast-info-content
                          css-toast-info-content)}
           (case type
             :success (svg/success)
             :warning (svg/warning)
             :error (svg/danger)
             (svg/info))]
          [:div {:class "ml-3 text-sm font-normal"} content]
          [:button {:type "button"
                    :class css-toast-close-button
                    :on-click #()}
           (svg/close)]])])))