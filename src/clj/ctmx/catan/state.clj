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

(def node-rows [7 9 11 11 9 7])
(defn node-index [i j]
  (apply + j (take i node-rows)))

(def nodes {(node-index 1 3) ["red" "settlement"]
            (node-index 1 6) ["orange" "settlement"]
            (node-index 2 2) ["white" "settlement"]
            (node-index 3 2) ["red" "settlement"]
            (node-index 3 8) ["white" "settlement"]
            (node-index 4 2) ["blue" "settlement"]
            (node-index 4 4) ["orange" "settlement"]
            (node-index 4 6) ["blue" "settlement"]})

(def edge-rows [6 4 8 5 10 6 10 5 8 4 6])
(defn edge-index [i j]
  (apply + j (take i edge-rows)))

(def edges {(edge-index 2 3) "red"
            (edge-index 2 5) "orange"
            (edge-index 4 2) "white"
            (edge-index 5 4) "white"
            (edge-index 6 2) "red"
            (edge-index 7 3) "blue"
            (edge-index 8 1) "blue"
            (edge-index 8 3) "orange"})

(defn new-game [random?]
  (let [outputs (if random? (shuffle outputs) outputs)
        terrains (if random? (shuffle terrains) terrains)]
    {:cards (cards)
     :outputs outputs
     :terrains (outputs->terrains outputs terrains)
     :nodes nodes
     :edges edges}))

(defonce state (atom {}))

(defn add-game [game-name random?]
  (swap! state assoc game-name (new-game random?)))
(defn delete-game [game-name]
  (swap! state dissoc game-name))

(defn games [] (keys @state))
