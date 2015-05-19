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

(defn selectize [{:keys [options multi max-items on-change value]}]
  (let [model (or value (atom (if multi [])))
        typed (atom "")
        dropdown (atom false)
        dropdown-width (atom 0)
        timer (atom nil)
        option-index (atom 0)
        full #(and multi max-items (= (count @model) max-items))
        hide-later (fn []
                     (js/clearTimeout @timer)
                     (reset! timer (js/setTimeout #(reset! dropdown false) 100))
                     true)
        on-change (or on-change (fn [_]))
        update-model (fn [option]
                       (if multi
                         (when-not (full) (on-change (swap! model conj option)))
                         (on-change (reset! model option))))]
    (fn []
      (let [filtered-options (filter-options @typed @model options)]
        [:div.selectize-control {:class (if multi "multi" "single")}
         [:div.selectize-input.itmes.not-full.has-options
          {:class (when (not-empty @model) "has-items")
           :on-click #(-> % .-currentTarget .-lastChild .focus)}
          (if multi
            (for [{:keys [value label]} @model]
              ^{:key value}
              [:div.item label])
            [:div.item (:label @model)])
          [:input {:value @typed
                   :on-blur hide-later
                   :on-focus (fn [e] (reset! dropdown-width (-> e .-target .-parentNode .-offsetWidth))
                               (js/clearTimeout @timer)
                               (reset! dropdown true))
                   :on-change (fn [e] (reset! typed (-> e .-target .-value))
                                (reset! option-index 0))
                   :on-key-down (fn [e]
                                  (case (.-which e)
                                    13 (when-let [option (nth filtered-options @option-index)]
                                         (update-model option)
                                         (reset! typed "")
                                         (when-not multi (-> e .-target .blur)))
                                    8 (when (empty? @typed) 
                                        (on-change (if multi (swap! model #(or (butlast %) []))
                                                       (reset! model nil))))
                                    38 (when (> @option-index 0)
                                         (swap! option-index dec))
                                    40 (when (< @option-index (dec (count filtered-options)))
                                         (swap! option-index inc))
                                    (if (or (full) (and (not multi) @model))
                                      (.preventDefault e)
                                      true)))}]]
         [:div.selectize-dropdown {:style {:width (str @dropdown-width "px")
                                           :display (if @dropdown "block" "none")}
                                   :class (if multi "multi" "single")}
          [:div.selectize-dropdown-content
           (doall
            (for [{:keys [value label] :as option} filtered-options]
              ^{:key value}
              [:div.option {:on-click (fn [e] (update-model option) (when multi (-> e .-currentTarget .-parentNode .-parentNode .-previousSibling .-lastChild .focus)))
                            :data-selectable ""
                            :class (when (= option (nth filtered-options @option-index)) "active")}
               (highlight label @typed)]))]]]))))
