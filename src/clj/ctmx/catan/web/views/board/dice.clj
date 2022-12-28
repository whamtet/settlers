(ns ctmx.catan.web.views.board.dice
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.web.views.board.inventory :as inventory]))

(defn- disp-thiever [game-name color]
  [:div.mb-2
   (for [[player count] (state/card-counts game-name)
         :when (not= player color)]
     [:button.btn.btn-primary.mr-3
      {:hx-post "dice:steal"
       :hx-vals {:from player}
       :hx-confirm (format "Steal from %s?" player)
       :hx-target "#dice"
       :disabled (zero? count)}
      (format "Steal from %s (%s)" player count)])])

(defn- disp-dice [game-name color]
  (let [dice (state/get-dice game-name)
        sum (apply + dice)]
    [:div#dice.mb-3
     [:h3 "Dice"]
     (when (= 7 sum)
           [:div [:h3 "Robber!"]
            (when color (disp-thiever game-name color))])
     [:button.btn.btn-primary.mr-3 {:hx-post "dice:roll"
                                    :hx-confirm "Roll?"
                                    :hx-target "#dice"} "Roll!"]
     (for [die dice]
       [:img {:src (format "/d%s.jpg" die) :style {:width "50px"}}])]))

(defcomponent ^:endpoint dice [req command from]
  (case command
        "roll" (do
                 (state/roll! game-name)
                 (sse/send-others! game-name color (disp-dice game-name nil))
                 (inventory/update-inventory game-name)
                 (disp-dice game-name color))
        "steal" (do
                  (state/steal-board! game-name from color)
                  (inventory/update-inventory game-name)
                  (disp-dice game-name nil))
        (disp-dice game-name color)))
