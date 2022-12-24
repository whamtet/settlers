(ns ctmx.catan.web.views.board
    (:require
      [ctmx.catan.state :as state]
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.response :as response]))

;<rect fill="black" width="300" height="300" />
;<polygon
;points="150 0, 300 86.6, 300 213.4, 150 300, 0 213.4, 0 86.6"
;fill="white"
;/>
;<circle cx="150" cy="150" r="40" fill="black" />
(defn tile [x y]
  (list
    []))

(defcomponent ^:endpoint board [req command]
  (case command
        "main-menu" (assoc response/hx-refresh :session {})
        "delete" (do
                   (state/delete-game game-name)
                   (assoc response/hx-refresh :session {}))
        [:div.container-flex
         [:div.mt-1.ml-1
          [:a.btn.btn-primary.mr-3
           {:href "https://www.catan.com/sites/default/files/2021-06/catan_base_rules_2020_200707.pdf"
            :target "_blank"} "Rules"]
          [:button.btn.btn-primary.mr-3
           {:hx-post "board:main-menu"} "Main Menu"]
          [:button.btn.btn-primary.mr-3
           {:hx-delete "board:delete"
            :hx-confirm "Delete game?"} "Delete Game"]]]))
