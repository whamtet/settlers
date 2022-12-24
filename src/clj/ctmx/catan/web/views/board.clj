(ns ctmx.catan.web.views.board
    (:require
      [ctmx.catan.svg :refer [offset]]
      [ctmx.catan.component :refer [defcomponent]]))

;<rect fill="black" width="300" height="300" />
;<polygon
;points="150 0, 300 86.6, 300 213.4, 150 300, 0 213.4, 0 86.6"
;fill="white"
;/>
;<circle cx="150" cy="150" r="40" fill="black" />
(defn tile [x y]
  (list
    []))

(defcomponent board [req]
  [:div#board "Board " game-name ": " color])
