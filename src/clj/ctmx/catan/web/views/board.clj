(ns ctmx.catan.web.views.board
    (:require
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.svg :as svg :refer [vec+]]
      [ctmx.catan.web.views.board.inventory :as inventory]
      [ctmx.catan.web.views.board.settlement :as settlement]
      [ctmx.catan.web.views.board.vp :as vp]
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.response :as response]))

(def rot (Math/pow 3 0.5))
(def p3 (/ Math/PI 3))
(def p6 (/ Math/PI 6))

(defn i->r [i]
  (cond
   (zero? i) 0
   (< i 7) (* 150 rot)
   (odd? i) (* 300 rot)
   :else 450))

(defn i->t [i]
  (condp > i
         1 0
         7 (* (dec i) p3)
         (* (- i 7) p6)))

(defn- rt->xy [r t]
  [(* r (Math/cos t))
   (* r (Math/sin t))])

;; patterns
(defn- pattern [id]
  [:pattern {:id id :x 0 :y 0 :width 1 :height 1}
   [:image {:xlink:href (str "/" id ".jpg")}]])

(def p 0.15)
(defn- combine [x1 x2]
  (+
   (* (- 1 p) x1)
   (* p x2)))
(defn- contract [x1 y1 x2 y2]
  [(combine x1 x2)
   (combine y1 y2)
   (combine x2 x1)
   (combine y2 y1)])
(defn road [player offset i j color]
  (when (not= :skip color)
        (let [t1 (- (* j p3) p6)
              t2 (+ t1 p3)
              [x1 y1] (vec+ offset (rt->xy 150 t1))
              [x2 y2] (vec+ offset (rt->xy 150 t2))
              [x1 y1 x2 y2] (contract x1 y1 x2 y2)]
          [:line {:x1 x1
                  :y1 y1
                  :x2 x2
                  :y2 y2
                  :stroke-width 20
                  :hx-post (when (or (not color) (= player color)) "board:edge")
                  :hx-vals {:i i :j j}
                  :stroke (or color "rgba(0, 0, 0, 0)")}])))

(def hex-center [807 750])
(defn hex [game-name i pattern output]
  (let [[x y :as offset] (vec+
                          (rt->xy (i->r i) (i->t i))
                          hex-center)
        points (for [i (range 6)]
                 (let [t (+ p6 (* i p3))]
                   (vec+ offset (rt->xy 150 t))))
        fill (format "url(#%s)" pattern)
        robber-action {:hx-post "board:robber"
                       :hx-vals {:robber i}
                       :hx-trigger "dblclick"}]
    (list
     [:polygon {:points (svg/pstring points) :fill fill}]
     [:circle (assoc robber-action :cx x :cy y :r 40 :fill "white")]
     (if (neg? output)
       [:image {:x (- x 20) :y (- y 20) :xlink:href"/robber.png" :width 60 :height 72}]
       [:text (assoc robber-action :x (- x 10) :y (+ y 10) :fill "black" :font-size "2em") output]))))

(defn infrastructure [game-name player i]
  (let [offset (vec+
                (rt->xy (i->r i) (i->t i))
                hex-center)
        nodes (state/get-nodes game-name i)
        edges (state/get-edges game-name i)]
    (list
     (map-indexed (partial road player offset i) edges)
     (map-indexed
      (fn [j [color settlement]]
        (when (not= :skip color)
              (let [t (+ p6 (* j p3))
                    offset (vec+ offset [-25 -25] (rt->xy 150 t))
                    f (case settlement
                            "settlement" settlement/settlement
                            "city" settlement/city
                            settlement/blank)]
                (f player i j offset color)))) nodes))))

(defn svg [& children]
  [:svg {:width 1300 :height 1220 :viewBox "0 0 1500 1500"
         :style {:position "absolute" :top 80 :left 10}}
   (map pattern ["desert" "forest" "fields" "mountains" "hills" "pasture"])
   children])

(defn board-disp [game-name color]
  (let [[terrains outputs] (state/get-terrain game-name)]
    [:div#board {:position "relative"}
     [:img {:src "/background.png"}]
     (svg
      (map hex (repeat game-name) (range) terrains outputs)
      (for [i (range 19)] (infrastructure game-name color i)))]))

(defcomponent ^:endpoint board [req command ^:long robber ^:long i ^:long j]
  (case command
        "robber"
        (do
          (assert robber)
          (state/assoc-robber game-name robber)
          (sse/send! game-name (board-disp game-name color)))
        "node" (do
                 (state/build-node! game-name color i j)
                 (sse/send! game-name (board-disp game-name color))
                 (vp/update-vp game-name)
                 (inventory/update-inventory game-name))
        "edge" (do
                 (state/build-edge! game-name color i j)
                 (sse/send! game-name (board-disp game-name color))
                 (vp/update-vp game-name)
                 (inventory/update-inventory game-name))
        (board-disp game-name color)))
