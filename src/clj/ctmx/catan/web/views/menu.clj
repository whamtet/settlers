(ns ctmx.catan.web.views.menu
    (:require
      [ctmx.catan.state :as state]
      [ctmx.core :as ctmx :refer [defcomponent]]
      [ctmx.response :as response]))

(defcomponent ^:endpoint menu [req command game-name ^:boolean random color]
  (case command
        "create"
        (do
          (assert game-name)
          (state/add-game game-name random)
          response/hx-refresh)
        "join"
        (do
          (assert game-name)
          (assert (state/valid-color? color))
          (assoc response/hx-refresh :session {:game-name game-name :color color}))
        [:div.p-3
         (for [game (state/games)]
           [:div
            [:h3 game]
            (for [color state/colors]
              [:button.btn.btn-primary.mr-3
               {:hx-confirm "Remember to delete game when you are finished"
                :hx-post "menu:join"
                :hx-vals {:color color :game-name game}}
               "Join as " color])])
         [:hr]
         [:form {:hx-post "menu:create" :hx-confirm "Create game?"}
          [:label.mr-3 "Game name"]
          [:input.mr-3 {:type "text" :required true :name "game-name"}]
          [:input.mr-3 {:type "checkbox" :checked true :name "random"}] "Random"
          [:input.btn.btn-primary.ml-3 {:type "submit"}]]]))
