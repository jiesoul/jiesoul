(ns frontend.shared.tables)

(defn table-dash [thead tbody page]
  [:div {:class "relative overflow-x-auto shadow-md sm:rounded-lg"}
   [:table {:class "w-full text-sm text-center text-gray-500 dark:text-gray-400"}
    [:thead {:class "text-xs text-gray-700 uppercase dark:text-gray-400"}
     thead]
    [:tbody {:class "bg-white"}
     tbody]] 
   page])