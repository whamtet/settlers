(ns ctmx.catan.web.views.board.dice
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.web.views.board.inventory :as inventory]))

(defn disp-dice [game-name]
  (let [dice (state/get-dice game-name)]
    [:div#dice
     [:h3 "Dice"]
     [:button.btn.btn-primary.mr-3 {:hx-post "dice:roll"} "Roll!"]
     (for [die dice]
       [:img {:src (format "/d%s.jpg" (inc die)) :style {:width "50px"}}])]))

(defcomponent ^:endpoint dice [req command]
  (case command
        "roll" (do
                 (state/roll! game-name)
                 (sse/send! game-name (disp-dice game-name))
                 (inventory/update-inventory game-name))
        (disp-dice game-name)))
