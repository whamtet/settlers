(ns ctmx.catan.util)

(defn keymap [f m]
  (zipmap (map f (keys m)) (vals m)))
(defn valmap [f m]
  (zipmap (keys m) (map f (vals m))))
