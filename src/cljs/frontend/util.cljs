(ns frontend.util
  (:require [cljs.pprint]
            [clojure.string :as str]))

(def Q-SIZE 5)

(defn q-pop [q]
  (pop q))

(defn q-push [q d]
  (if (< (count q) Q-SIZE)
    (conj q d)
    (do 
      (q-pop q)
      (conj q d))))

(defn get-value [d]
  (-> d .-target .-value))

(defn blank? [v]
  (or (nil? v) (str/blank? v)))

(defn valid?
  [[_ v]]
  (and (string? v) (seq v)))

(defn save!
  [a k]
  #(swap! a assoc k (-> % .-target .-value)))

(defn input [label k type state]
  [:div.flex.flex.flex-wrap.gap-2.itemc-center.mt-1
   [:label {:htmpFor (name k) :className "login-label"} label]
   [:div 
    [:input {:type type
             :id (name k)
             :name (name k)
             :className "login-input"
             :placeholder (name k)
             :value (k @state)
             :on-change (save! state k)
             :required true}]]])

(defn clog
  ([msg] (clog msg nil))
  ([msg data] 
   (let [buf (if data 
               (str msg ": " data)
               msg)]
     (js/console.log buf))))

(defn error-message
  [title msg]
  [:<>
   [:div.bg-red-100.border.border-red-400.text-red-700.px-4.py-3.rounded.relative
    {:role "alert"}
    [:strong.font-bold.mr-2 title]
    [:span.block.sm:inline msg]]])

(defn my-parseInt
  [s]
  (js/parseInt s 10))