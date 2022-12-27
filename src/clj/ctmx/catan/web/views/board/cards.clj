(ns ctmx.catan.web.views.board.cards
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.web.views.board.inventory :as inventory]))

(defn- card [i {:keys [title body]}]
  [:div.p-3
   [:h5 title]
   [:p body]
   [:div.mb-2
    [:button.btn.btn-primary
     {:hx-post "cards:play"
      :hx-target "#cards"
      :hx-vals {:i i}} "Play"]]
   [:div
    [:button.btn.btn-primary
     {:hx-post "cards:return"
      :hx-confirm "Return card?"
      :hx-vals {:i i}} "Return"]]])

(defn- public-area [game-name color]
  (if-let [{:keys [player title body]} (state/get-card game-name)]
    [:div#public-card
     [:div.p-3
      [:span {:style {:color player}} player] " is playing " title ": " body]
     (when (= player color)
           [:div.mb-2
            [:button.btn.btn-primary.mr-3
             {:hx-post "cards:retrieve"
              :hx-target "#cards"} "Take back card"]
            (case title
                  "Monopoly"
                  (for [[resource name] state/inv->name]
                    [:button.btn.btn-primary.mr-3
                     {:hx-post "cards:monopolize"
                      :hx-vals {:resource resource}}
                     "Grab " name])
                  nil)])]
    [:div#public-card]))

(defn update-public-area [game-name]
  (sse/send-color! game-name (partial public-area game-name)))

(defcomponent ^:endpoint cards [req command ^:long i resource]
  (case command
        "pick-up" (do
                    (state/pick-up! game-name color)
                    (inventory/update-inventory game-name))
        "play" (do
                 (state/play! game-name color i)
                 (update-public-area game-name))
        "retrieve" (do
                     (state/retrieve! game-name color)
                     (update-public-area game-name))
        "monopolize" (do
                       (state/monopolize! game-name color resource)
                       (update-public-area game-name)
                       (inventory/update-inventory game-name))
        nil)
  (when-not (#{"monopolize"} command)
            [:div#cards.border
             (public-area game-name color)
             [:div.d-flex
              [:img {:src "/dev.png"
                     :hx-patch "cards:pick-up"
                     :hx-target "#cards"
                     :style {:width "180px"}}]
              (map-indexed card (state/get-cards game-name color))]]))
