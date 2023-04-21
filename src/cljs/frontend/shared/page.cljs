(ns frontend.shared.page
  (:require [frontend.shared.svg :as svg]
            [re-frame.core :as re-frame]))


(def css-page-no-current
  "z-10 px-3 py-2 leading-tight text-blue-600 border border-blue-300 bg-blue-50 
   hover:bg-blue-100 hover:text-blue-700 dark:border-gray-700 dark:bg-gray-700 dark:text-white")

(def css-page-no 
  "block px-3 py-2 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg 
   hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 
   dark:hover:bg-gray-700 dark:hover:text-white")


(defn page-backend [{:keys [page per-page total]}]
  (let []
    [:nav {:class "flex items-center justify-between pt-4"
           :aria-label "Table navigation"}
     [:span {:class "text-sm font-normal text-gray-500 dark:text-gray-400"}
      "Showing "
      [:span {:class "font-semibold text-gray-900 dark:text-white"}
       "1-10"]
      " of "
      [:span {:class "font-semibold text-gray-900 dark:text-white"}
       (:total total)]]
     [:ul {:class "inline-flex items-center -space-x-px"}
      [:li>a {:href "#"
              :class css-page-no}
       (svg/chevron-left)]
      [:li>a {:href "#"
              :class css-page-no-current} "1"]
      [:li>a {:href "#"
              :class css-page-no} "2"]
      [:li>a {:href "#"
              :class css-page-no}
       (svg/chevron-right)]]]))