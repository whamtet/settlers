(ns ctmx.catan.web.views.board.vp
    (:require
      [ctmx.catan.sse :as sse]
      [ctmx.catan.state :as state]))

(defn disp-vp [game-name color]
  (let [{:strs [knight road settlements cities] :as points} (state/victory-points game-name color)
        cards (concat ["settlements" "cities"] state/vp-card?)
        total (apply +
                     (if knight 2 0)
                     (if road 2 0)
                     (->> cards (map points) (filter identity)))]
    [:div#vp.p-5
     [:h3 "Victory Points"]
     (for [card cards
           :let [points (points card)
                 point-str (if (= 1 points) "point" "points")]
           :when points]
       [:div (format "%s %s for %s." points point-str card)])
     (when knight
           [:div "Two points for using knight cards " knight " times."])
     (when road
           [:div "Two points for the longest road (" road " long)."])
     [:h5.mt-3 "Total points: " total]]))

(defn update-vp [game-name]
  (sse/send-color! game-name (partial disp-vp game-name)))
