(ns ctmx.catan.svg
    (:require
      [clojure.string :as string]))

(defn pstring
  ([vs] (pstring [0 0] vs))
  ([[x0 y0] vs]
   (string/join ", "
                (for [[x y] vs]
                  (str (+ x0 x) " " (+ y0 y))))))

(defn vec+ [& vs]
  [(->> vs (map first) (apply +))
   (->> vs (map second) (apply +))])
