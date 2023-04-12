(ns frontend.shared.sidebar)

(defn sidebar-dash []
  [:<>
   [:div {:class "fixed inset-0 z-20 transition-opacity bg-black opacity-50 lg:hidden"}]
   
   [:div {:class "fixed inset-y-0 left-0 z-30 w-64 overflow-y-auto transition duration-300
                  transform bg-gray-900 lg:translate-x-0 lg:static lg:inset-0"}
    [:div {:class "flex items-center justify-center mt-8"}
     [:div {:class "flex items-center"}
      [:span {:class "mx-2 text-2xl font-semibold text-white"} "Dashboard"]]]
    
    [:nav {:class "mt-10"}
     [:a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
          :href "/"}
      [:span {:class "mx-3"} "Dashboard"]]
     
     [:a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
          :href "/"}
      [:span {:class "mx-3"} "UI Elements"]]
     
     [:a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
          :href "/"}
      [:span {:class "mx-3"} "Tables"]]
     ]]])