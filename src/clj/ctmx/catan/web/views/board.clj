(ns ctmx.catan.web.views.board
    (:require
      [clojure.string :as string]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.response :as response]))

;<rect fill="black" width="300" height="300" />
;<polygon
;points="150 0, 300 86.6, 300 213.4, 150 300, 0 213.4, 0 86.6"
;fill="white"
;/>
;<circle cx="150" cy="150" r="40" fill="black" />
(defn- vec+ [[x1 y1] [x2 y2]]
  [(+ x1 x2)
   (+ y1 y2)])
(defn- pstring [vs]
  (string/join ", "
               (for [[x y] vs]
                 (str x " " y))))

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

(defn hex [i pattern output]
  (let [[x y :as offset] (vec+
                          (rt->xy (i->r i) (i->t i))
                          [807 750])
        points (for [i (range 6)]
                 (let [t (+ p6 (* i p3))]
                   (vec+ offset (rt->xy 150 t))))
        fill (format "url(#%s)" pattern)
        robber-action {:hx-post "board:robber"
                       :hx-vals {:robber i}
                       :hx-trigger "dblclick"}]
    (list
     [:polygon {:points (pstring points) :fill fill}]
     [:circle (assoc robber-action :cx x :cy y :r 40 :fill "white")]
     (if (neg? output)
       [:image {:x (- x 20) :y (- y 20) :xlink:href"/robber.png" :width 60 :height 72}]
       [:text (assoc robber-action :x (- x 10) :y (+ y 10) :fill "black" :font-size "2em") output]))))

(defn svg [& children]
  [:svg {:width 1300 :height 1220 :viewBox "0 0 1500 1500"
         :style {:position "absolute" :top 80 :left 10}}
   (map pattern ["desert" "forest" "fields" "mountains" "hills" "pasture"])
   children])

(defn board-disp [game-name]
  (let [[terrains outputs] (state/get-terrain game-name)]
    [:div#board {:position "relative"}
     [:img {:src "/background.png"}]
     (svg
      (map hex (range) terrains outputs))]))

(defcomponent ^:endpoint board [req command ^:long robber]
  (case command
        "robber"
        (do
          (assert robber)
          (state/assoc-robber game-name robber)
          (sse/send! game-name (board-disp game-name)))
        (board-disp game-name)))
