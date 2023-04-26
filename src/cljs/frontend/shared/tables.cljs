(ns frontend.shared.tables)

(def list-table-thead-tr-th "px-1 py-1 border-b border-gray-500 bg-gray-50
                      text-base leading-4 font-blod text-gray-500 tracking-wider")

(def list-table-tbody-tr "bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-200")
(def list-table-tbody-tr-td "px-1 py-1 whitespace-no-wrap border-b border-gray-200")

(defn table-dash [thead tbody page]
  [:div {:class "relative overflow-x-auto shadow-md sm:rounded-lg"}
   [:table {:class "w-full p-1 border-gray-100 text-sm text-center text-gray-500 dark:text-gray-400"}
    [:thead {:class "text-base text-gray-700 dark:text-gray-400"}
     thead]
    [:tbody {:class "bg-white"}
     tbody]] 
   page])

(defn th-dash [children & props]
  [:th {:class list-table-thead-tr-th} children])

(defn td-dash [children & props]
  [:td {:class list-table-tbody-tr-td}
   children])