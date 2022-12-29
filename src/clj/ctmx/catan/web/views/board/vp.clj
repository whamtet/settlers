(ns ctmx.catan.web.views.board.vp
    (:require
      [ctmx.catan.component :refer [defcomponent]]
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]))

(defn disp-vp [game-name color]
  (let [{:strs [knight road settlements cities] :as points} (state/victory-points game-name color)
        cards (concat ["settlements" "cities"] state/vp-card?)
        _ (prn 'points points)
        total (apply +
                     (if knight 2 0)
                     (if road 2 0)
                     (->> cards (map points) (filter identity)))]
    [:div#vp.p-5
     [:h3 "Victory Points"]
     (for [card cards
           :let [points (points card)]
           :when points]
       [:div points " points for " card "."])
     (when knight
           [:div "Two points for using knight cards " knight " times."])
     (when road
           [:div "Two points for the longest road (" road ")."])
     [:h5.mt-3 "Total points: " total]]))

(defn update-vp [game-name]
  (sse/send-color! game-name (partial disp-vp game-name)))
