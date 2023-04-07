(ns frontend.routes.index
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]))

(defn home-page []
  [:div {:class "min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8"}
   [:div {:class "sm:mx-auto sm:w-full sm:max-w-md"}
    [:div
     [:p.text-center.p-4
      "This is a demo built for learning the following technologies:"]
     [:div.p-4
      [:p.text-left.font-bold "Backend:"]
      [:ul.list-disc.list-inside
       [:li "Clojure"]
       [:li "JVM"]]]
     [:div.p-4
      [:p.text-left.font-bold "Frontend:"]
      [:ul.list-disc.list-inside
       [:li "Clojurescript"]
       [:li "React with Reagent"]
       [:li "Re-frame"]
       [:li "Tailwind"]]]
     [:div.p-4.text-center
      [:button
       {:on-click (fn [e]
                    (.preventDefault e)
                    (re-frame/dispatch [::f-state/navigate ::f-state/product-groups]))}
       "Enter"]]]]])
