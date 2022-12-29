(ns ctmx.catan.util)

(defn keymap [f m]
  (zipmap (map f (keys m)) (vals m)))
(defn valmap [f m]
  (zipmap (keys m) (map f (vals m))))

(def invert #(zipmap (vals %) (keys %)))

#_
(defn max-by [f [x & rest]]
  (first
   (reduce
    (fn [[x1 y1] x2]
      (let [y2 (f x2)]
        (if (> y2 y1)
          [x2 y2]
          [x1 y1])))
    [x (f x)]
    rest)))
