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
(def blank-shape
  (partition 2 [0 0
                100 0
                100 100
                0 100]))

(defn settlement [player i j offset color]
  [:polygon {:points (svg/pstring offset (scale 0.5 house))
             :stroke "black"
             :hx-post (when (= player color) "board:node")
             :hx-vals {:i i :j j}
             :fill color}])

(defn city [player i j offset color]
  [:g {:hx-post (when (= player color) "board:node")
       :hx-vals {:i i :j j}}
   [:polygon {:points (svg/pstring offset (scale+ 0.4 -20 house))
              :stroke "black"
              :fill color}]
   [:polygon {:points (svg/pstring offset (scale+ 0.4 20 house))
              :stroke "black"
              :fill color}]])

(defn blank [player i j offset color]
  [:polygon {:points (svg/pstring offset (scale 0.5 blank-shape))
             :hx-post "board:node"
             :hx-vals {:i i :j j}
             :fill "rgba(0, 0, 0, 0)"}])
