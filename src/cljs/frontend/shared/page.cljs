(ns frontend.shared.page
  (:require [frontend.shared.svg :as svg]
            [re-frame.core :as re-frame]
            [frontend.util :as f-util]))

(def css-page-no-current
  "z-10 px-3 py-2 leading-tight text-blue-600 border border-blue-300 bg-blue-50 
   hover:bg-blue-100 hover:text-blue-700 dark:border-gray-700 dark:bg-gray-700 dark:text-white")

(def css-page-no 
  "block px-3 py-2 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg 
   hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 
   dark:hover:bg-gray-700 dark:hover:text-white")


(defn page-dash 
  "params: page page-size total query-params url" 
  [{:keys [page page-size total query-params url]}]
  (when (pos-int? total)
   (let [total-pages (quot (dec (+ total page-size)) page-size)
         start (inc (* (dec page) page-size))
         end (dec (+ start page-size))
         page-no 4
         start-page (let [p (- page page-no)] (if (> p 0) p 1))
         end-page (let [p (+ page page-no)] (if (> p total-pages) total-pages p))
         prev-page (if (<= page 1) 1 (dec page))
         next-page (if (< page total-pages) (inc page) total-pages)]
     [:div 
      [:nav {:class "flex items-center justify-between pt-4"}
       [:span {:class "text-sm font-normal text-gray-500 dark:text-gray-400"}
        "Showing "
        [:span {:class "font-semibold text-gray-900 dark:text-white"}
         (str start "-" (if (< end total) end total))]
        " of "
        [:span {:class "font-semibold text-gray-900 dark:text-white"}
         total]]
       [:ul {:class "inline-flex items-center -space-x-px"}
        [:li {:key "prev-page"}
         [:button {:on-click #(re-frame/dispatch [url (assoc query-params :page prev-page)])
                   :disabled (if (<= page 1) true false)
                   :class css-page-no}
          (svg/chevron-left)]]
        (when (> start-page 1)
          [:li {:key 1}
           [:button {:on-click #(re-frame/dispatch [url (assoc query-params :page 1)])
                     :class css-page-no} "1"]])
        (for [p (range start-page (inc end-page))]
          [:li {:key p}
           [:button {:on-click #(re-frame/dispatch [url (assoc query-params :page p)])
                     :disabled (if (= page p) true false)
                     :class (if (= page p) css-page-no-current css-page-no)} p]])
        (when (< end-page total-pages)
          [:li {:key total-pages}
           [:button {:on-click #(re-frame/dispatch [url (assoc query-params :page total-pages)])
                       :class css-page-no} total-pages]])
        [:li {:key "next-page"}
         [:button {:on-click #(re-frame/dispatch [url (assoc query-params :page next-page)])
                     :class css-page-no
                     :disabled (if (>= page total-pages) true false)}
         (svg/chevron-right)]]]]])))