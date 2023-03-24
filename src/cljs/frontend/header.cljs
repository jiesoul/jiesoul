(ns frontend.header 
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
            [frontend.state :as f-state]))

(defn header []
  (fn []
    (let [:login-status @(re-frame/subscribe [::f-state/login-status])
          :username @(re-frame/subscribe [::f-state/username])]
      [:div])))