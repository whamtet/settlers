(ns ctmx.catan.component
    (:require
      [ctmx.core :as ctmx]))

(defmacro defcomponent [name [req :as args] & body]
  `(ctmx/defcomponent ~name ~args
    (let [{:keys [~'game-name ~'color]} (:session ~req)]
      ~@body)))
