(ns re-select.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn filter-options [typed exclude options]
  (filter (fn [{:keys [value label] :as option}]
            (and (not (some #{option} exclude))
                 (-> (.toLowerCase label) (.indexOf typed) (> -1))))
          options))

(defn highlight [line hl]
  (let [idx (-> (.toLowerCase line) (.indexOf hl))
        p1 (subs line 0 idx)
        p2 (subs line idx (+ (count hl) idx))
        p3 (subs line (+ (count hl) idx))]
    [:div p1 [:span.highlight p2] p3]))

(defn selectize-input [model multi filtered-options update-model full on-change state]
  (let [timer (atom 0)]
    [:input {:value (:typed @state)
             :on-blur (fn []
                        (js/clearTimeout @timer)
                        (reset! timer (js/setTimeout #(swap! state assoc :dropdown false) 100))
                        true)
             :on-focus (fn [e] (swap! state assoc :dropdown-width (-> e .-target .-parentNode .-offsetWidth))
                         (js/clearTimeout @timer)
                         (swap! state assoc :dropdown true))
             :on-change (fn [e] (swap! state assoc :typed (-> e .-target .-value))
                          (swap! state assoc :option-index 0))
             :on-key-down (fn [e]
                            (case (.-which e)
                              13 (when-let [option (nth filtered-options (:option-index @state))]
                                   (update-model option)
                                   (swap! state assoc :typed "")
                                   (when-not multi (-> e .-target .blur)))
                              8 (when (empty? (:typed @state)) 
                                  (on-change (if multi (swap! model #(or (butlast %) []))
                                                 (reset! model nil))))
                              46 (when-let [item (:selected-item @state)]
                                   (on-change (swap! model (fn [items] (or (remove #(= % item) items) [])))))
                              38 (when (> (:option-index @state) 0)
                                   (swap! state update-in [:option-index] dec))
                              40 (when (< (:option-index @state) (dec (count filtered-options)))
                                   (swap! state update-in [:option-index] inc))
                              (if (or (full) (and (not multi) @model))
                                (.preventDefault e)
                                true)))}]))

(defn selectize [{:keys [options multi max-items on-change value]}]
  (let [model (or value (atom (if multi [])))
        state (atom {:typed "" :dropdown false :dropdown-width 0 :option-index 0 :selected-item nil})
        full #(and multi max-items (= (count @model) max-items))
        on-change (or on-change (fn [_]))
        update-model (fn [option]
                       (if multi
                         (when-not (full) (on-change (swap! model conj option)))
                         (on-change (reset! model option))))]
    (fn []
      (let [filtered-options (filter-options (:typed @state) @model options)]
        [:div.selectize-control {:class (if multi "multi" "single")}
         [:div.selectize-input.itmes.not-full.has-options
          {:class (when (not-empty @model) "has-items")
           :on-click #(-> % .-currentTarget .-lastChild .focus)}
          (if multi
            (doall (for [{:keys [value label] :as option} @model]
                     ^{:key value}
                     [:div.item {:class (when (= (:selected-item @state) option) "active")
                                 :on-click #(swap! state assoc :selected-item option)}
                      label]))
            [:div.item (:label @model)])
          [selectize-input model multi filtered-options update-model full on-change state]]
         [:div.selectize-dropdown {:style {:width (str (:dropdown-width @state) "px")
                                           :display (if (:dropdown @state) "block" "none")}
                                   :class (if multi "multi" "single")}
          [:div.selectize-dropdown-content
           (doall
            (for [{:keys [value label] :as option} filtered-options]
              ^{:key value}
              [:div.option {:on-click (fn [e] (update-model option) (when multi (-> e .-currentTarget .-parentNode .-parentNode .-previousSibling .-lastChild .focus)))
                            :data-selectable ""
                            :class (when (= option (nth filtered-options (:option-index @state))) "active")}
               (highlight label (:typed @state))]))]]]))))
