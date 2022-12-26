(ns ctmx.catan.web.views.board.settlement
    (:require
      [ctmx.catan.svg :as svg]))

(defn- scale [c vs]
  (for [[x y] vs]
    [(* c x) (* c y)]))
(defn- scale+ [c x0 vs]
  (for [[x y] vs]
    [(+ x0 (* c x)) (* c y)]))

(def house
  (partition 2 [50 0
                100 50
                80 50
                80 100
                20 100
                20 50
                0 50]))

(defn settlement [offset color]
  [:polygon {:points (svg/pstring offset (scale 0.5 house))
             :stroke "black"
             :fill color}])

(defn city [offset color]
  (list
   [:polygon {:points (svg/pstring offset (scale+ 0.4 -20 house))
              :stroke "black"
              :fill color}]
   [:polygon {:points (svg/pstring offset (scale+ 0.4 20 house))
              :stroke "black"
              :fill color}]))
