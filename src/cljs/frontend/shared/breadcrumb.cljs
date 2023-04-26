(ns frontend.shared.breadcrumb)

(defn breadcrumb-dash [b-data]
  [:nav {:class "flex border-b"}
   [:ol {:class "inline-flex items-center space-x-1 md:space-x-2"}
    (for [d b-data]
      [:li {:class "inline-flex items-center"}
       [:a {:href "#"
            :class "text-6xl font-bold inline-flex items-center text-gray-700 
                  hover:text-blue-600 dark:text-gray-400 dark:hover:text-white"}
        d]])]])