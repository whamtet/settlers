(ns ctmx.catan.svg
    (:require
      [clojure.string :as string]))

(defn pstring
  ([vs] (pstring [0 0] vs))
  ([[x0 y0] vs]
   (string/join ", "
                (for [[x y] vs]
                  (str (+ x0 x) " " (+ y0 y))))))

(defn vec+ [[x1 y1] [x2 y2]]
  [(+ x1 x2)
   (+ y1 y2)])
