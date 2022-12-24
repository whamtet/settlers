(ns ctmx.catan.web.views.board
    (:require
      [clojure.string :as string]
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
                          [700 700])
        points (for [i (range 6)]
                 (let [t (+ p6 (* i p3))]
                   (vec+ offset (rt->xy 150 t))))
        fill (format "url(#%s)" pattern)]
    (list
     [:polygon {:points (pstring points) :fill fill}]
     [:circle {:cx x :cy y :r 40 :fill "white"}]
     (if (neg? output)
       [:image {:x (- x 20) :y (- y 20) :xlink:href"/robber.png" :width 60 :height 72}]
       [:text {:x (- x 10) :y (+ y 10) :fill "black" :font-size "2em"} output]))))

(defn svg [& children]
  [:svg {:width 1400 :height 1400 :viewBox "0 0 2000 2000"}
   (map pattern ["desert" "forest" "fields" "mountains" "hills" "pasture"])
   children])

(defcomponent ^:endpoint board [req command]
  (case command
        (let [[terrains outputs] (state/get-terrain game-name)]
          [:div
           (svg
            (map hex (range) terrains outputs))])))
