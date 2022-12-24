(ns ctmx.catan.web.views.display
    (:require
      [ctmx.catan.state :as state]
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.web.views.board :as board]
      [ctmx.response :as response]))

(def menu-bar
  [:div
   [:a.btn.btn-primary.mr-3
    {:href "https://www.catan.com/sites/default/files/2021-06/catan_base_rules_2020_200707.pdf"
     :target "_blank"} "Rules"]
   [:button.btn.btn-primary.mr-3
    {:hx-post "display:main-menu"} "Main Menu"]
   [:button.btn.btn-primary.mr-3
    {:hx-delete "display:delete"
     :hx-confirm "Delete game?"} "Delete Game"]])

(defcomponent ^:endpoint display [req command]
  (case command
        "main-menu" (assoc response/hx-refresh :session {})
        "delete" (do
                   (state/delete-game game-name)
                   (assoc response/hx-refresh :session {}))
        [:div.container-flex.p-1 {:hx-ws "connect:/api/sse"}
         [:h5 "Welcome " [:span {:style {:color color}} color] " player."]
         menu-bar
         (board/board req)]))
