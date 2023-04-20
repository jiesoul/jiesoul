(ns frontend.shared.msg)

(defn resp-message [{:keys [status message] :as m}]
  (when m
    [:div {:class "flex justify-center items-center space-y-4"}
     [:p {:class (case status 
                   "ok" "text-green-500"
                   "failed" "text-red-600"
                   "text-red-600")} message]]))