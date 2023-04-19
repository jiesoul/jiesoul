(ns frontend.shared.buttons)

(defn btn [props & children]
  (into 
   [:button (merge {:type "button"} props) children]))

(defn loading-button [{:keys [loading class]} & children]
  (let [class (str class " flex items-center focus:outline-none"
                   (when loading " pointer-events-none bg-opacity-75 select-none"))]
    [:button {:class class
              :disabled loading}
     (when loading [:div.mr-2.btn-spinner])
     children]))

(defn delete-button [{:keys [on-delete]} & children]
  (into
   [:button {:class "text-red-600 focus:outline-none hover:underline"
             :tab-index -1
             :type "button"
             :on-click on-delete}]
   children))

(defn yellow-button [{:keys [on-click]} & children]
  [:button {:type "button"
             :class "text-yellow-400 hover:text-white border border-yellow-400 
                     hover:bg-yellow-500 focus:ring-4 focus:outline-none 
                     focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 
                     text-center mr-2 mb-2 dark:border-yellow-300 dark:text-yellow-300 
                     dark:hover:text-white dark:hover:bg-yellow-400 dark:focus:ring-yellow-900"
            :on-click on-click}
   children])

(defn red-button [props & children]
  (into
   [:button (merge {:type "button"
                    :class "text-red-700 hover:text-white border border-red-700 
                    hover:bg-red-800 focus:ring-4 focus:outline-none 
                    focus:ring-red-300 font-medium rounded-lg text-sm 
                    px-5 py-2.5 text-center mr-2 mb-2 dark:border-red-500 
                    dark:text-red-500 dark:hover:text-white dark:hover:bg-red-600 
                    dark:focus:ring-red-900"}
                   props)
    children]))

(defn default-button [props & children]
  (into
   [:button (merge {:type "button"
                    :class "text-blue-700 hover:text-white border border-blue-700 hover:bg-blue-800 
                     focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg 
                     text-sm px-5 py-2.5 text-center inline-flex item-center mr-2 mb-2 dark:border-blue-500 
                     dark:text-blue-500 dark:hover:text-white dark:hover:bg-blue-500 
                     dark:focus:ring-blue-800"} 
                   props)
    ;; (svg/search)
    children]))

