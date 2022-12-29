(ns ctmx.catan.web.views.board.cards
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.web.views.board.inventory :as inventory]
      [ctmx.catan.web.views.board.vp :as vp]))

(def playable? #{"Monopoly" "Road Building" "Year of Plenty" "Knight"})
(defn- card [i {:keys [title body]}]
  [:div.p-3
   [:h5 title]
   [:p body]
   (when (playable? title)
         [:div.mb-2
          [:button.btn.btn-primary
           {:hx-post "cards:play"
            :hx-target "#cards"
            :hx-vals {:i i}} "Play"]])
   [:div
    [:button.btn.btn-primary
     {:hx-post "cards:return"
      :hx-confirm (format "Return %s card to deck?" title)
      :hx-target "#cards"
      :hx-vals {:i i}} "Return to deck"]]])

(defn- disp-thiever [game-name color]
  [:span
   (for [[player count] (state/card-counts game-name)
         :when (not= player color)]
     [:button.btn.btn-primary.mr-3
      {:hx-post "cards:steal"
       :hx-vals {:from player}
       :hx-confirm (format "Steal from %s?" player)
       :disabled (zero? count)}
      (format "Steal from %s (%s)" player count)])
   [:button.btn.btn-primary.mr-3
    {:hx-post "cards:steal"
     :hx-confirm "Play without stealing?"}
    "Play without stealing"]])

(defn- public-area [game-name color]
  (if-let [{:keys [player title body partial]} (state/get-card game-name)]
    [:div#public-card
     [:div.p-3
      [:span {:style {:color player}} player] " is playing " title ": " body]
     (when partial
           [:div.p-3 "Already took 1 " (state/inv->name partial)])
     (when (= player color)
           [:div.mb-2
            [:button.btn.btn-primary.mr-3
             {:hx-post "cards:retrieve"
              :hx-target "#cards"} "Put back card"]
            (case title
                  "Monopoly"
                  (for [[resource name] state/inv->name]
                    [:button.btn.btn-primary.mr-3
                     {:hx-post "cards:monopolize"
                      :hx-vals {:resource resource}}
                     "Grab " name])
                  "Road Building"
                  [:button.btn.btn-primary.mr-3
                   {:hx-post "cards:road"} "Play"]
                  "Year of Plenty"
                  (for [[resource name] state/inv->name]
                    [:button.btn.btn-primary.mr-3
                     {:hx-post "cards:plenty"
                      :hx-vals {:resource resource}}
                     "Take " name])
                  "Knight" (disp-thiever game-name color)
                  nil)])]
    [:div#public-card]))

(defn update-public-area [game-name]
  (sse/send-color! game-name (partial public-area game-name)))

(defcomponent ^:endpoint cards [req command ^:long i resource from]
  (case command
        "pick-up" (do
                    (state/pick-up! game-name color)
                    (vp/update-vp game-name)
                    (inventory/update-inventory game-name))
        "play" (do
                 (state/play! game-name color i)
                 (update-public-area game-name))
        "retrieve" (do
                     (state/retrieve! game-name color)
                     (update-public-area game-name))
        "return" (do
                   (state/return! game-name color i)
                   (vp/update-vp game-name))
        "monopolize" (do
                       (state/monopolize! game-name color resource)
                       (update-public-area game-name)
                       (inventory/update-inventory game-name))
        "road" (do
                 (state/road! game-name color)
                 (update-public-area game-name)
                 (inventory/update-inventory game-name))
        "plenty" (do
                   (state/plenty! game-name color resource)
                   (update-public-area game-name)
                   (inventory/update-inventory game-name))
        "steal" (do
                  (state/steal-knight! game-name from color)
                  (update-public-area game-name)
                  (vp/update-vp game-name)
                  (inventory/update-inventory game-name))
        nil)
  (when-not (#{"monopolize" "road" "plenty" "steal"} command)
            [:div#cards.border
             (public-area game-name color)
             [:div.d-flex
              [:img {:src "/dev.png"
                     :hx-patch "cards:pick-up"
                     :hx-target "#cards"
                     :style {:width "180px"}}]
              (map-indexed card (state/get-cards game-name color))]]))
