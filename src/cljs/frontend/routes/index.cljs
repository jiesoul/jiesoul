(ns frontend.routes.index
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.shared.layout :refer [layout-home]]))



(defn home-page 
  []  
  [layout-home
   [:<>
    [:section {:class "bg-center bg-no-repeat bg-[url('https://flowbite.s3.amazonaws.com/docs/jumbotron/conference.jpg')] 
                       bg-gray-700 bg-blend-multiply"}
     [:div {:class "px-4 mx-auto max-w-screen-xl text-center py-24 lg:py-56"}
      [:h1 {:class "mb-4 text-4xl font-extrabold tracking-tight leading-none text-white md:text-5xl lg:text-6xl"}
       "We invest in the world"]
      [:p {:class "mb-8 text-lg font-normal text-gray-300 lg:text-xl sm:px-16 lg:px-48"}
       "Here at Flowbite we focus on markets where technology, innovation, and capital can unlock long-term value and drive economic growth."]]] 
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
      [:li "Tailwind"]]]]])
