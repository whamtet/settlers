(ns ctmx.catan.web.views.board.inventory
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]))

(defn- send-form [color resource name count]
  (let [input-class (str "mr-3 count" resource)
        hx-include (str ".count" resource)]
    [:div
     [:input {:type "number" :name "count" :value 1 :min 1 :max count :class input-class}]
     (for [other-color (-> state/valid-color? (disj color) seq (conj "bank"))]
       [:button.btn.btn-primary.mr-3
        {:hx-post "inventory:send"
         :hx-vals {:from color :resource resource :to other-color}
         :hx-include hx-include
         :hx-confirm (format "Send %s to %s?" name other-color)} "Send to " other-color])]))

(defn disp-inventory [game-name color]
  (let [inv (state/get-inventory game-name color)
        prices (state/trading-privileges game-name color)]
    [:div#inventory
     [:h2 "Inventory"]
     [:table
      [:tbody
       (for [[resource name] state/inv->name
             :let [count (inv resource 0)]]
         [:tr
          [:td name]
          [:td (inv resource 0)]
          [:td
           (when (pos? count)
                 (send-form color resource name count))]])]]
     [:hr]
     [:h4 "Buy from abroad"]
     [:div
      [:select.to-select.mr-3 {:name "to"}
       (for [[resource name] state/inv->name]
         [:option {:value resource} name])]
      (for [[resource name] state/inv->name
            :let [price (prices resource 4)
                  available (inv resource 0)]]
        [:button.btn.btn-primary.mr-3
         {:disabled (< available price)
          :hx-post "inventory:buy"
          :hx-vals {:from resource}
          :hx-include ".to-select"
          :hx-target "#inventory"}
          (format "Buy with %s (%s)" name price)])]
     ]))

(defcomponent ^:endpoint inventory [req from resource to ^:long count command]
  (case command
        "send" (do
                 (prn (java.util.Date.) 'send from resource to count)
                 (state/send-inv! game-name from resource to count)
                 (sse/send-color!
                   game-name
                  (partial disp-inventory game-name)
                  [from to]))
        "buy"
        (when (and from (not= from to))
              (state/buy! game-name color from to)
              (disp-inventory game-name color))
        (disp-inventory game-name color)))
