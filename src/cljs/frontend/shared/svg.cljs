(ns frontend.shared.svg)

(defn svg-gen [{:keys [d class]}]
  [:svg {:class (if class class "w-5 h-5 ")
         :aria-hidden "true"
         :fill "currentColor"
         :view-box "0 0 20 20"
         :xmlns "http://www.w3.org/2000/svg"}
   [:path {:fill-rule "evenodd"
           :d d
           :clip-rule "evenodd"}]])

(defn search []
  (svg-gen {:class "w-5 h-5 mr-2 -ml-1"
            :d "M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 
                1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 
                7 0 012 9z"}))

(defn user-avatar []
  (svg-gen {:class "absolute w-12 h-12 text-gray-400 -left-1"
            :d "M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"}))

(defn chevron-up []
  (svg-gen {:d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 
                111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"}))

(defn chevron-left []
  (svg-gen {:d "M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 
                01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"}))

(defn chevron-right []
  (svg-gen {:d "M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 
                011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"}))

(defn close []
  (svg-gen {:d "M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 
                111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 
                11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 
                1 0 010-1.414z"}))