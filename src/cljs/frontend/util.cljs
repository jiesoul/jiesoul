(ns frontend.util
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
            [frontend.state :as sf-state]))


(defn clog 
  ([msg] (clog msg nil))
  ([msg data] 
   (let [buf (if data 
               (str msg ": " data)
               msg)]
     (js/console.log buf))))