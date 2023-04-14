(ns frontend.shared.form-input)

(def css-form-errors "")
(def css-form-label "block mb-2 text-sm font-medium text-gray-900 dark:text-white")
(def css-form-input "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg 
                    focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 
                    dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 
                    dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(defn select-input
  [{:keys [label name class errors] :as props} & children]
  [:div {:class class}
   (when label
     [:label.form-label {:html-for name} label ":"])
   (into [:select (merge props {:id name
                                :name name
                                :class (str "form-select" (when (seq errors) " error"))})]
         children)
   (when errors
     [:div.form-error errors])])

(defn text-input [{:keys [label name class errors] :as props}]
  [:div {:class class}
   (when label
     [:label.form-label {:html-for name} label ":"])
   [:input (merge props {:id name
                         :name name
                         :class (str "form-input" (when (seq errors) " error"))})]
   (when errors [:div.form-error errors])])

(defn text-input-backend [{:keys [label name class errors on-change]}]
  [:div {:class class}
   (when label
     [:label {:class css-form-label
              :for "name"} label])
   [:input {:class css-form-input
            :type "text"
            :id name
            :on-change on-change}]
   (when errors [:div {:class css-form-errors}])])