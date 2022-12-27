(ns ctmx.catan.web.views.board.cards
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.state :as state]))

(defn- card [i {:keys [title body]}]
  [:div.p-3
   [:h5 title]
   [:p body]
   [:div.mb-2
    [:button.btn.btn-primary
     {:hx-post "cards:play"
      :hx-vals {:i i}} "Play"]]
   [:div
    [:button.btn.btn-primary
     {:hx-post "cards:return"
      :hx-confirm "Return card?"
      :hx-vals {:i i}} "Return"]]])

(defcomponent ^:endpoint cards [req command ^:long i]
  (case command
        "pick-up" (state/pick-up! game-name color)
        "play" (prn 'play i)
        nil)
  [:div#cards.border.d-flex
   [:img {:src "/dev.png"
          :hx-patch "cards:pick-up"
          :hx-target "#cards"
          :style {:width "180px"}}]
   (map-indexed card (state/get-cards game-name color))])
