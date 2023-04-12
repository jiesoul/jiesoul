(ns frontend.routes.index
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.shared.layout :refer [layout-home]]))

(defn home-page 
  []  
  (layout-home
   [:<>
    [:div {:class "relative"}
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center text-white"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center text-white"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center text-white"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]]
    [:div {:class "w-24 md:w-auto h-screen p-6"}
     [:p.text-left.font-bold "Backend:"]
     [:ul.list-disc.list-inside
      [:li "Clojure"]
      [:li "JVM"]]]
    [:div {:class "text-center sm:text-left"}
     [:p.text-left.font-bold "Frontend:"]
     [:ul.list-disc.list-inside
      [:li "Clojurescript"]
      [:li "React with Reagent"]
      [:li "Re-frame"]
      [:li "Tailwind"]]]
    [:div.text-center
     [:button
      {:class "btn-indigo"
       :on-click (fn [e]
                   (.preventDefault e)
                   (re-frame/dispatch [::f-state/navigate ::f-state/login]))}
      "Login"]]]))
