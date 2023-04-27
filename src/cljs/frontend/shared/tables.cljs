(ns frontend.shared.tables)

(def css-list "relative overflow-x-auto shadow-md sm:rounded-lg w-full")
(def css-list-table "w-full p-1 border-gray-100 text-sm text-center text-gray-500 dark:text-gray-400")
(def css-list-table-thead "text-base text-gray-700 dark:text-gray-400")
(def css-list-table-thead-tr "")
(def css-list-table-thead-tr-th "px-1 py-1 border-b border-gray-500 bg-gray-50 text-base leading-4 
                                 font-blod text-gray-500 tracking-wider")

(def css-list-table-tbody "bg-white")
(def css-list-table-tbody-tr "bg-white border dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-200")
(def css-list-table-tbody-tr-td "px-1 py-1 whitespace-no-wrap border-b border-gray-200")

(defn th-dash [children]
  [:th {:class css-list-table-thead-tr-th} children])

(defn tbody-tr [children]
  [:tr {:class css-list-table-tbody-tr} children])

(defn td-dash [children]
  [:td {:class css-list-table-tbody-tr-td} children])

(defn table-dash [thead tbody & page]
  [:div {:class css-list}
   [:table {:class css-list-table}
    [:thead {:class css-list-table-thead}
     thead]
    [:tbody {:class css-list-table-tbody}
     tbody]]
   page])

