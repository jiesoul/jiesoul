(ns frontend.shared.tables)

(defn table-dash [thead tbody page]
  [:div {:class "align-middle inline-block min-w-full shadow overflow-hidden sm:rounded-lg border-b border-gray-200"}
   [:table {:class "min-w-full text-center"}
    [:thead 
     thead]
    [:tbody {:class "bg-white"}
     tbody]] 
   page])