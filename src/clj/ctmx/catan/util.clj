(ns ctmx.catan.util)

(defn keymap [f m]
  (zipmap (map f (keys m)) (vals m)))
