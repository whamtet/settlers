(ns ctmx.catan.state)

(def colors ["red" "orange" "white" "blue"])
(def valid-color? (set colors))

;; cards
(def cards-raw
  [["Knight" 14 "Move the robber. Steal one resource from the owner of a settlement or city adjacent to the robber’s new hex."]
  ["Road Building" 2 "Place 2 new roads as if you had just built them."]
  ["Year of Plenty" 2 "Take any two resources from the bank. Add them to your hand. They can be two of the same resource or two different resources."]
  ["Monopoly" 2 "When you play this card, announce one type of resource. All other players must give you all of their resources of that type."]
  ["University" 1 "One victory point. Reveal this card on your turn if, with it, you reach the number of points required for victory."]
  ["Market" 1 "One victory point. Reveal this card on your turn if, with it, you reach the number of points required for victory."]
  ["Great Hall" 1 "One victory point. Reveal this card on your turn if, with it, you reach the number of points required for victory."]
  ["Chapel" 1 "One victory point. Reveal this card on your turn if, with it, you reach the number of points required for victory."]
  ["Library" 1 "One victory point. Reveal this card on your turn if, with it, you reach the number of points required for victory."]])

(defn cards []
  (shuffle
   (for [[title repeats body] cards-raw
         _ (range repeats)]
     {:title title :body body})))

(def outputs [10 2 9
              12 6 4 10
              9 11 0 3 8
              8 3 4 5
              5 6 11])

(def terrains [:mountains :pasture :forest
               :fields :hills :pasture :hills
               :fields :forest #_:desert :forest :mountains
               :forest :mountains :fields :pasture
               :hills :fields :pasture])

(defn outputs->terrains [outputs terrains]
  (assert (= 1 (- (count outputs) (count terrains))))
  (let [head-count (count (take-while pos? outputs))
        [head tail] (split-at head-count terrains)]
    (concat head [:desert] tail)))

(defn new-game [random?]
  (let [outputs (if random? (shuffle outputs) outputs)
        terrains (if random? (shuffle terrains) terrains)]
    {:cards (cards)
     :outputs outputs
     :terrains (outputs->terrains outputs terrains)}))

(defonce state (atom {}))

(defn add-game [game-name random?]
  (swap! state assoc game-name (new-game random?)))

(defn games [] (keys @state))
