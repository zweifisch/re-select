(ns example.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-select.core :refer [selectize]])
  (:import goog.History))

(def us-states
  {"Alabama" 	 "AL"
   "Alaska" 	 "AK"
   "Arizona" 	 "AZ"
   "Arkansas" 	 "AR"
   "California"  "CA"
   "Colorado" 	 "CO"
   "Connecticut" "CT"
   "Delaware" 	 "DE"
   "Florida" 	 "FL"
   "Georgia" 	 "GA"
   "Hawaii" 	 "HI"
   "Idaho"    	 "ID"
   "Illinois" 	 "IL"
   "Indiana" 	 "IN"
   "Iowa"        "IA"
   "Kansas" 	 "KS"
   "Kentucky" 	 "KY"
   "Louisiana" 	 "LA"
   "Maine" 	     "ME"
   "Maryland" 	 "MD"
   "Massachusetts" "MA"
   "Michigan" 	 "MI"
   "Minnesota" 	 "MN"
   "Mississippi" "MS"
   "Missouri" 	 "MO"
   "Montana" 	 "MT"
   "Nebraska" 	 "NE"
   "Nevada" 	 "NV"
   "New Hampshire" "NH"
   "New Jersey"  "NJ"
   "New Mexico"  "NM"
   "New York"    "NY"
   "North Carolina" "NC"
   "North Dakota" "ND"
   "Ohio" 	      "OH"
   "Oklahoma" 	  "OK"
   "Oregon" 	  "OR"
   "Pennsylvania" "PA"
   "Rhode Island" "RI"
   "South Carolina" "SC"
   "South Dakota" "SD"
   "Tennessee" 	"TN"
   "Texas" 	    "TX"
   "Utah" 	    "UT"
   "Vermont" 	"VT"
   "Virginia" 	"VA"
   "Washington" "WA"
   "West Virginia" "WV"
   "Wisconsin" 	"WI"
   "Wyoming" 	"WY"})

;; -------------------------
;; Views

(defn home-page []
  (let [state (atom nil)
        states (atom [])]
    (fn []
      [:div
       [:div "Multiple"
        [selectize {:options (for [[name abbr] us-states] {:value abbr :label name})
                    :multi true
                    :max-items 3
                    :on-change #(reset! states %)}]
        (for [{:keys [value]} @states]
          [:span {:style {:font-size "0.7em" :margin-right "0.5em"}} value])]
       [:div {:style {:margin-top "2em"}} "Single"
        [selectize {:options (for [[name abbr] us-states] {:value abbr :label name})
                    :on-change #(reset! state %)}]
        [:span {:style {:font-size "0.7em"}} (:value @state)]]])))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
