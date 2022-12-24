(ns ctmx.catan.svg
    (:require
      [hiccup.core :as h]))

(defn vec+ [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])
(defn offset [vs offset]
  (map #(vec+ % offset) vs))

(comment
(defn vec+ [v inc]
  (map #(+ % inc) v))
(defn xy [r t]
  [(* r (Math/cos t))
   (* r (Math/sin t))])

(defn line [t]
  (let [[x1 y1] (vec+ (xy 50 t) 150)
        [x2 y2] (vec+ (xy 125 t) 150)]
    [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :stroke-width 10 :stroke "white"}]))

(defn svg [t]
  [:svg {:width 300 :height 300 :viewBox "0 0 300 300" :xmlns"http://www.w3.org/2000/svg"}
   [:rect {:fill "black" :width 300 :height 300}]
   (line t)
   (line (+ t (/ Math/PI 3)))])

#_(defn generate []
  (doseq [i (range 6)]
    (let [t (* i Math/PI 1/3)]
      (spit
       (format "convert/hex%s.svg" i)
       (h/html (svg t)))))))
