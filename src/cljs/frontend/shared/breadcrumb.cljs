(ns frontend.shared.breadcrumb)

(defn breadcrumb-dash
  [b-data]
  [:nav {:class "flex"
         :aria-label "Breadcrumb-Dash"}
   [:ol {:class "inline-flex items-center space-x-1 md:space-x-3"}
    (for [d b-data]
      [:li {:class "inline-flex items-center"}
       [:a {:href "#"
            :class "inline-flex items-center text-sm font-medium text-gray-700 
                  hover:text-blue-600 dark:text-gray-400 dark:hover:text-white"}
        d]])]])