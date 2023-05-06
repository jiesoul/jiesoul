(ns frontend.shared.toasts 
  (:require [frontend.shared.svg :as svg]
            [frontend.shared.toasts :as toasts]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def css-toast-base "flex items-center w-full max-w-xs p-4 mb-1 text-gray-500 
                 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")

(def css-toast-info-content "inline-flex items-center justify-center flex-shrink-0 w-10 max-w-xs h-6 
                  text-blue-500 bg-blue-100 rounded-lg dark:bg-blue-800 dark:text-blue-200")

(def css-toast-success-content "inline-flex items-center justify-center flex-shrink-0 w-10 h-6
                  text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200")

(def css-toast-drange-content "inline-flex items-center justify-center flex-shrink-0 w-10 max-w-xs h-6
                  text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200")

(def css-toast-warning-content "inline-flex items-center justify-center flex-shrink-0 w-10 h-6 
                  text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-800 dark:text-orange-200")

(def css-toast-close-button "ml-auto -mx-2 -my-1.5 bg-white text-red-700 hover:text-red-900 
                       rounded focus:ring-2 focus:ring-red-300 p-1.5 hover:bg-red-100 
                       inline-flex h-8 w-8 dark:text-red-500 dark:hover:text-white dark:bg-red-800 
                       dark:hover:bg-red-700")


(def MAX-TOASTS 5)
(def MAX-TIMEOUT 5000)
(def TIMEOUT 4)

(re-frame/reg-sub
 ::toasts
 (fn [db]
   (get-in db [:toasts])))

(re-frame/reg-event-db
 ::pop
 (fn [db _]
   (update-in db [:toasts] #(-> % rest vec))))

(defonce do-timer (js/setInterval (re-frame/dispatch [::pop]) 3000))

(re-frame/reg-event-db
 ::push
 (fn [db [_ t]]
   (let [toasts (:toasts db)
         toasts (if (>= (count toasts) MAX-TOASTS)
                  (vec (rest toasts)) 
                  toasts)]
     (assoc db :toasts (conj toasts (assoc t 
                                           :id (str (random-uuid))
                                           :timeout MAX-TIMEOUT))))))

(re-frame/reg-event-db
 ::remove 
 (fn [db [_ id]]
   (f-util/clog "remove id: " id)
   (let [toasts (:toasts db)]
     (assoc db :toasts (->> toasts
                           (remove #(= (:id %) id))
                            vec)))))

(defn timer-toasts [id]
  (let [seconds-elapsed (r/atom TIMEOUT)]
    (fn [id]
      (js/setTimeout #(if (pos-int? @seconds-elapsed) 
                       (swap! seconds-elapsed dec)
                        (re-frame/dispatch [::pop id])) MAX-TIMEOUT)
      [:div {:class "hidden"} @seconds-elapsed])))

(defn toasts []
  (let [toasts @(re-frame/subscribe [::toasts])]
    (when toasts
      [:div {:class "fixed top-5 right-5 z-50 w-full max-w-xs"} 
       (for [{:keys [id type content timeout]} toasts] 
         [:div {:id (str "toast-" id)
                :class css-toast-base
                :role "alert"}
          [:div {:class (case type
                          :success css-toast-success-content
                          :warning css-toast-warning-content
                          :error css-toast-drange-content
                          css-toast-info-content)}
           (case type
             :success (svg/success)
             :warning (svg/warning)
             :error (svg/danger)
             (svg/info))]
          [:div {:class "ml-3 text-sm font-normal w-auto h-auto overflow-y-hidden"} content]
          [timer-toasts id]
          [:button {:type "button"
                    :class css-toast-close-button
                    :on-click #(re-frame/dispatch [::remove id])}
           (svg/close)]])])))