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

(def outputs [0
              3 4 3 11 6 4
              8 5
              11 6
              5 8
              9 12
              10 2
              9 11])

(def terrains [#_"desert"
               "forest" "fields" "mountains" "forest" "hills" "pasture"
               "mountains" "pasture"
               "pasture" "fields"
               "hills" "forest"
               "fields" "fields"
               "mountains" "pasture"
               "forest" "hills"])

(defn outputs->terrains [outputs terrains]
  (assert (= 1 (- (count outputs) (count terrains))))
  (let [head-count (count (take-while pos? outputs))
        [head tail] (split-at head-count terrains)]
    (concat head ["desert"] tail)))
(defn outputs->robber [outputs]
  (count (take-while pos? outputs)))

#_
(defn node-downgrade [v]
  (case v
        ;; inner
        [1 3] [0 5]
        [1 2] [0 0]
        [2 4] [0 0]
        [2 3] [0 1]
        [3 5] [0 1]
        [3 4] [0 2]
        [4 0] [0 2]
        [4 5] [0 3]
        [5 1] [0 3]
        [5 0] [0 4]
        [6 2] [0 4]
        [6 1] [0 5]
        ;; trailing
        [2 5] [1 1]
        [3 0] [2 2]
        [4 1] [3 3]
        [5 2] [4 4]
        [6 3] [5 5]
        ;; outer inner
        [7 3] [1 5]
        [7 2] [1 0]
        [8 4] [1 0]
        [8 3] [1 1]
        [8 2] [2 0]
        [9 4] [2 0]
        [9 3] [2 1]))

(def nodes-raw ["white"
                nil
                "blue"
                nil
                "orange"
                nil
                "blue"
                nil
                "red"
                nil
                "white"
                nil
                "red"
                nil
                nil
                "orange"])
(def nodes (into {}
                 (for [[i color] (map-indexed list nodes-raw)
                       :when color]
                   [(node-index [1 i]) [color "settlement"]])))

#_
(def edge-downgrade [v]
  (case v
        ;; inner
        [1 3] [0 0]
        [2 4] [0 1]
        [3 5] [0 2]
        [4 0] [0 3]
        [5 1] [0 4]
        [6 2] [0 5]
        ;; trailing
        [2 5] [1 2]
        [3 0] [2 3]
        [4 1] [3 4]
        [5 2] [4 5]
        [6 3] [5 0]
        ;; outer inner
        [7 3] [1 0]
        [8 4] [1 1]
        [8 3] [2 0]
        [9 4] [2 1]
        [10 5] [2 2]
        [10 4] [3 1]
        [11 5] [3 2]
        [12 0] [3 3]
        [12 5] [4 2]
        [13 0] [4 3]
        [14 1] [4 4]
        [14 2] [5 3]
        [15 1] [5 4]
        [16 2] [5 5]
        [16 1] [6 4]
        [17 2] [6 5]
        [18 2] [1 5]
        [18 3] [6 0]
        ;; outer trailing
        [8 5] [7 2]
        [9 5] [8 2]
        [10 0] [9 3]
        [11 0] [10 3]
        [12 1] [11 4]
        [13 1] [12 4]
        [14 2] [13 5]
        [15 2] [14 5]
        [16 3] [15 0]
        [17 3] [16 0]
        [18 4] [17 1]
        v))


(def edges-raw ["white"
                nil
                "blue"
                nil
                "orange"
                nil
                "blue"
                nil
                "red"
                nil
                "white"
                nil
                nil
                "red"
                nil
                "orange"])

(def edges (into {}
                 (for [[i color] (map-indexed list edges-raw)
                       :when color]
                   [(edge-index [2 i]) color])))

(defn new-game [random?]
  (let [outputs (if random? (shuffle outputs) outputs)
        terrains (if random? (shuffle terrains) terrains)]
    {:cards (cards)
     :outputs (vec outputs)
     :terrains (vec (outputs->terrains outputs terrains))
     :robber (outputs->robber outputs)
     :nodes nodes
     :edges edges}))

(defonce state (atom {}))

(defn add-game [game-name random?]
  (swap! state assoc game-name (new-game random?)))
(defn delete-game [game-name]
  (swap! state dissoc game-name))

(defn games [] (keys @state))

(defn get-terrain [game-name]
  (let [{:keys [terrains outputs robber]} (@state game-name)]
    [terrains (assoc outputs robber -1)]))

(defn assoc-robber [game-name robber]
  (swap! state assoc-in [game-name :robber] robber))
