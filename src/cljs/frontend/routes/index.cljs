(ns frontend.routes.index
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.shared.layout :refer [layout-home]]))

(defn home-page 
  []  
  (layout-home
   [:<>
    [:div {:id "default-carousel"
           :class "relative w-full"
           :data-carousel "slide"} 
     [ :div {:class "relative h-56 overflow-hidden rounded-lg md:h-96"}
      [:div {:class "hidden duration-700 ease-in-out"
             :data-carousel-item true} 
       [:img {:src "img/carousel-1.svg"
              :class "absolute block w-full -translate-x-1/2 -translate-y-1/2 top-1/2 left-1/2"
              :alt "..."}]]
      [:div {:class "hidden duration-700 ease-in-out"
             :data-carousel-item true} 
       [:img {:src "img/carousel-1.svg"
              :class "absolute block w-full -translate-x-1/2 -translate-y-1/2 top-1/2 left-1/2"
              :alt "..."}]]
      [:div {:class "hidden duration-700 ease-in-out"
             :data-carousel-item true}
       [:img {:src "img/carousel-1.svg"
              :class "absolute block w-full -translate-x-1/2 -translate-y-1/2 top-1/2 left-1/2"
              :alt "..."}]]]]
    [:div {:class "relative"}
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center text-white"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]
     [:div {:class "sticky top-0 h-screen flex flex-col items-center justify-center text-white"}
      [:h2 {:class "text-4xl"} "The First Tile"]
      {:p "Scroll Down"}]]
    [:div {:class "w-24 md:w-auto h-screen p-6 m-8"}
     [:p.text-left.font-bold "Backend:"]
     [:ul.list-disc.list-inside
      [:li "Clojure"]
      [:li "JVM"]]]
    [:div {:class "text-center sm:text-left p-6 m-8"}
     [:p.text-left.font-bold "Frontend:"]
     [:ul.list-disc.list-inside
      [:li "Clojurescript"]
      [:li "React with Reagent"]
      [:li "Re-frame"]
      [:li "Tailwind"]]]]))
