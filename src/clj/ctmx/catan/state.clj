(ns ctmx.catan.state
    (:require
      [ctmx.catan.env :as env]
      [ctmx.catan.util :as util])
    (:import
      java.io.File))

(defonce state (atom {}))

(def colors ["red" "orange" "white" "blue"])
(def valid-color? (set colors))
(def valid-inv? (conj valid-color? "bank"))

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

(def vp-card? (->> cards-raw (take-last 5) (map first) set))

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
(def inv->name {"hills" "Brick"
                "forest" "Lumber"
                "mountains" "Ore"
                "fields" "Grain"
                "pasture" "Wool"})

(defn outputs->terrains [outputs terrains]
  (assert (= 1 (- (count outputs) (count terrains))))
  (let [head-count (count (take-while pos? outputs))
        [head tail] (split-at head-count terrains)]
    (concat head ["desert"] tail)))
(defn outputs->robber [outputs]
  (count (take-while pos? outputs)))

(def node-downgrade*
  {
  ;; inner ring
  ;; forward
    [1 2] [0 0]
    [2 3] [0 1]
    [3 4] [0 2]
    [4 5] [0 3]
    [5 0] [0 4]
    [6 1] [0 5]
    ;; middle
    [1 3] [0 5]
    [2 4] [0 0]
    [3 5] [0 1]
    [4 6] [0 2]
    [5 7] [0 3]
    [6 8] [0 4]
    ;; trailing
    [2 5] [1 1]
    [3 0] [2 2]
    [4 1] [3 3]
    [5 2] [4 4]
    [6 3] [5 5]

    ;; outer ring
    ;; forward (not all)
    [8 2] [2 0]
    [10 3] [3 1]
    [12 4] [4 2]
    [14 5] [5 3]
    [16 0] [6 4]
    [18 1] [1 5]
    ;; middle1
    [7 2] [1 0]
    [8 3] [1 1]
    [9 3] [2 1]
    [10 4] [2 2]
    [11 4] [3 2]
    [12 5] [3 3]
    [13 5] [4 3]
    [14 0] [4 4]
    [15 0] [5 4]
    [16 1] [5 5]
    [17 1] [6 5]
    [18 2] [6 0]
    ;; middle2
    [7 3] [1 5]
    [8 4] [1 0]
    [9 4] [2 0]
    [10 5] [2 1]
    [11 5] [3 1]
    [12 0] [3 2]
    [13 0] [4 2]
    [14 1] [4 3]
    [15 1] [5 3]
    [16 2] [5 4]
    [17 2] [6 4]
    [18 3] [6 5]
    ;; trailing
    [8 5] [7 1]
    [9 5] [8 1]
    [10 0] [9 2]
    [11 0] [10 2]
    [12 1] [11 3]
    [13 1] [12 3]
    [14 2] [13 4]
    [15 2] [14 4]
    [16 3] [15 5]
    [17 3] [16 5]
    [18 4] [17 0]})
(def node-downgrade #(node-downgrade* % %))

(def nodes {[1 0] ["white" "settlement"]
            [2 0] ["blue" "settlement"]
            [2 2] ["orange" "settlement"]
            [3 2] ["blue" "settlement"]
            [4 2] ["red" "settlement"]
            [4 4] ["white" "settlement"]
            [5 4] ["red" "settlement"]
            [6 5] ["orange" "settlement"]})

(def edge-downgrade*
  {
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

    ;; outer ring
    ;; forward (not all)
    [8 3] [2 0]
    [10 4] [3 1]
    [12 5] [4 2]
    [14 0] [5 3]
    [16 1] [6 4]
    [18 2] [1 5]
    ;; middle
    [7 3] [1 0]
    [8 4] [1 1]
    [9 4] [2 1]
    [10 5] [2 2]
    [11 5] [3 2]
    [12 0] [3 3]
    [13 0] [4 3]
    [14 1] [4 4]
    [15 1] [5 4]
    [16 2] [5 5]
    [17 2] [6 5]
    [18 3] [6 0]
    ;; trailing
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
    [18 4] [17 1]})
(def edge-upgrade* (util/invert edge-downgrade*))
(def edge-downgrade #(edge-downgrade* % %))

(def edges {[1 0] "white"
            [2 0] "blue"
            [2 2] "orange"
            [3 2] "blue"
            [4 2] "red"
            [4 4] "white"
            [5 5] "red"
            [6 5] "orange"})

(defn get-edges [game-name tile]
  (let [m (get-in @state [game-name :edges])]
    (for [i (range 6)]
      (if (edge-downgrade* [tile i])
        :skip
        (m [tile i])))))
(defn get-nodes [game-name tile]
  (let [m (get-in @state [game-name :nodes])]
    (for [i (range 6)]
      (if (node-downgrade* [tile i])
        [:skip]
        (m [tile i])))))
(defn nodes-for-player [game-name player]
  (for [[node [color]] (get-in @state [game-name :nodes])
        :when (= player color)]
    node))

(def materials
  {"road" {"hills" 1
           "forest" 1}
   "settlement" {"hills" 1
                 "forest" 1
                 "pasture" 1
                 "fields" 1}
   "city" {"mountains" 3
           "fields" 2}
   "card" {"mountains" 1
           "pasture" 1
           "fields" 1}})

(def materials-start
  (->> ["road" "road" "settlement" "settlement"]
       (map materials)
       (apply merge-with +)))

(defn inventory-start [tp?]
  {"red" (if tp? {} {"fields" 1})
   "white" {"fields" 1}
   "blue" {"forest" 1}
   "orange" {"mountains" 1}
   "bank" {"fields" (if tp? 18 17)
           "forest" 18
           "mountains" 18
           "hills" 19
           "pasture" 19}})

(defn new-game [random? tp?]
  (let [outputs (if random? (shuffle outputs) outputs)
        terrains (if random? (shuffle terrains) terrains)
        colors-to-allocate (if tp? (rest colors) colors)
        inventory-start (inventory-start tp?)]
    {:cards (cards)
     :outputs (vec outputs)
     :terrains (vec (outputs->terrains outputs terrains))
     :robber (outputs->robber outputs)
     :nodes (if random? {} nodes)
     :edges (if random? {} edges)
     :cities (zipmap colors (repeat 4))
     :settlements (zipmap colors (repeat (if random? 5 3)))
     :roads (zipmap colors (repeat (if random? 15 13)))
     :inventory (if random?
                  (reduce
                   (fn [inv color]
                     (-> inv
                         (update "bank" #(merge-with - % materials-start))
                         (update color #(merge-with + % materials-start))))
                   inventory-start
                   colors-to-allocate)
                  inventory-start)
     :tp? tp?
     :knights {}
     :hands {}
     :dice [1 1]}))

(defn tp? [game-name]
  (get-in @state [game-name :tp?]))

(defn add-game [game-name random? tp?]
  (swap! state assoc game-name (new-game random? tp?)))

(defn delete-game [game-name]
  (swap! state dissoc game-name))

(defn games []
  (for [[game-name {:keys [tp?]}] @state]
    [game-name tp?]))

(defn get-terrain [game-name]
  (let [{:keys [terrains outputs robber]} (@state game-name)]
    [terrains (assoc outputs robber -1)]))

(defn assoc-robber [game-name robber]
  (swap! state assoc-in [game-name :robber] robber))

(defn get-inventory [game-name color]
  (get-in @state [game-name :inventory color]))

(defn- safe+ [a b]
  (+ (or a 0) b))
(defn send-inv [m from resource to quantity]
  (assert (valid-inv? from))
  (assert (valid-inv? to))
  (assert (inv->name resource))
  (let [actual-quantity (-> quantity
                            (max 0)
                            (min (get-in m [from resource])))]
    (-> m
        (update-in [from resource] - actual-quantity)
        (update-in [to resource] safe+ actual-quantity))))

(defn send-inv! [game-name from resource to quantity]
  (swap! state update-in [game-name :inventory] send-inv from resource to quantity))

(def port-order ["pasture" "hills" "forest" "fields" "mountains"])
(def wildport (zipmap port-order (repeat 3)))
(def port #(hash-map (port-order %) 2))

(def ports
  (util/keymap node-downgrade
               {[7 0] wildport
                [7 5] wildport
                [8 0] (port 0)
                [8 1] (port 0)
                [10 0] wildport
                [10 1] wildport
                [11 1] wildport
                [11 2] wildport
                [12 2] (port 1)
                [12 3] (port 1)
                [14 2] (port 2)
                [14 3] (port 2)
                [15 3] wildport
                [15 4] wildport
                [16 4] (port 3)
                [16 5] (port 3)
                [18 4] (port 4)
                [18 5] (port 4)}))

(defn trading-privileges [game-name player]
  (->> (nodes-for-player game-name player)
       (map ports)
       (apply merge-with min {})))

(defn- buy [m player from to]
  (let [{:keys [nodes inventory]} m
        inventory (inventory player)
        nodes-for-player (for [[node [color]] nodes :when (= color player)] node)
        prices (->> nodes-for-player
                    (map ports)
                    (apply merge-with min {}))
        price (prices from 4)
        available (inventory from 0)
        inventory (if (>= available price)
                    (-> inventory
                        (update from - price)
                        (update to safe+ 1))
                    inventory)]
    (assoc-in m [:inventory player] inventory)))
(defn buy! [game-name player from to]
  (swap! state update game-name buy player from to))

(defn get-dice [game-name]
  (get-in @state [game-name :dice]))

(defn card-counts [game-name]
  (let [{:keys [tp? inventory]} (@state game-name)
        exclude (if tp? #{"bank" "red"} #{"bank"})]
    (for [[player inventory] inventory :when (not (exclude player))]
      [player (->> inventory vals (apply +))])))

(defn enough? [required available]
  (every?
   (fn [[resource count]]
     (>= (available resource 0) count))
   required))

(defn transfer [inventory from to items]
  (let [items-total (->> items (map materials) (apply merge-with +))]
    (-> inventory
        (update from #(merge-with - % items-total))
        (update to #(merge-with + % items-total)))))

(defn- build-edge [m player v]
  (let [{:keys [edges inventory roads]} m
        roads (roads player)
        v (edge-downgrade v)
        existing (edges v)
        new (and
             (not existing)
             (enough? (materials "road") (inventory player))
             (pos? roads))
        roads (cond
               new (dec roads)
               existing (inc roads)
               :else roads)
        inventory (cond
                   new (transfer inventory player "bank" ["road"])
                   existing (transfer inventory "bank" player ["road"])
                   :else inventory)
        edge (when new player)]
    (-> m
        (assoc-in [:roads player] roads)
        (assoc :inventory inventory)
        (assoc-in [:edges v] edge))))
(defn build-edge! [game-name player i j]
  (swap! state update game-name build-edge player [i j]))

(defn- build-node [m player v]
  (let [{:keys [nodes inventory cities settlements]} m
        cities (cities player)
        settlements (settlements player)
        v (node-downgrade v)
        [_ existing] (nodes v)
        new (case existing
                  nil (when (and
                             (enough? (materials "settlement") (inventory player))
                             (pos? settlements))
                            "settlement")
                  "settlement" (when (and
                                      (enough? (materials "city") (inventory player))
                                      (pos? cities))
                                     "city")
                  "city" nil)
        [cities settlements]
        (case [existing new]
              [nil "settlement"] [cities (dec settlements)]
              ["settlement" "city"] [(dec cities) (inc settlements)]
              ["city" nil] [(inc cities) settlements]
              ["settlement" nil] [cities (inc settlements)]
              [cities settlements])
        inventory
        (case [existing new]
              [nil "settlement"] (transfer inventory player "bank" ["settlement"])
              ["settlement" "city"] (transfer inventory player "bank" ["city"])
              ["city" nil] (transfer inventory "bank" player ["city" "settlement"])
              ["settlement" nil] (transfer inventory "bank" player ["settlement"])
              inventory)
        node (when new [player new])]
    (-> m
        (assoc-in [:cities player] cities)
        (assoc-in [:settlements player] settlements)
        (assoc :inventory inventory)
        (assoc-in [:nodes v] node))))
(defn build-node! [game-name player i j]
  (swap! state update game-name build-node player [i j]))

(def dump-file (File. "dump.edn"))
(defn dump-state []
  (spit dump-file (pr-str @state)))

(defn get-infrastructure [game-name player]
  (let [d (@state game-name)]
    (for [k [:cities :settlements :roads]]
      (get-in d [k player]))))

(defn- pick-up [m player]
  (let [{:keys [cards inventory]} m
        inventory (inventory player)
        [card & cards] cards]
    (if (and card (enough? (materials "card") inventory))
      (-> m
          (update-in [:hands player] conj card)
          (update :inventory #(transfer % player "bank" ["card"]))
          (assoc :cards cards))
      m)))
(defn pick-up! [game-name player]
  (swap! state update game-name pick-up player))

(defn- pick-up-specific [m player title]
  (let [{:keys [cards]} m
        [pre [card & cards]] (split-with #(-> % :title (not= title)) cards)]
    (if card
      (-> m
          (update-in [:hands player] conj card)
          (assoc :cards (concat pre cards)))
      m)))
(defn pick-up-specific! [game-name player title]
  (swap! state update game-name pick-up-specific player title))

(defn get-cards [game-name player]
  (get-in @state [game-name :hands player]))

(defn- remove-seq [s i]
  (let [[pre [card & rest]] (split-at i s)]
    [card (concat pre rest)]))
(defn- play [m player i]
  (if (:playing m)
    m
    (let [[card cards] (remove-seq (get-in m [:hands player]) i)]
      (if card
        (-> m
            (assoc :playing (assoc card :player player))
            (assoc-in [:hands player] cards))
        m))))
(defn play! [game-name player i]
  (swap! state update game-name play player i))

(defn return [m player i]
  (let [[card cards] (remove-seq (get-in m [:hands player]) i)]
    (if card
      (-> m
          (update :cards #(-> % (conj card) shuffle))
          (update :inventory #(transfer % "bank" player ["card"]))
          (assoc-in [:hands player] cards))
      m)))
(defn return! [game-name player i]
  (swap! state update game-name return player i))

(defn get-card [game-name]
  (get-in @state [game-name :playing]))

(defn- retrieve [m player]
  (if-let [card (:playing m)]
    (-> m
        (dissoc :playing)
        (update-in [:hands player] conj (select-keys card [:title :body])))
    m))
(defn retrieve! [game-name player]
  (swap! state update game-name retrieve player))

(defn- monopolize [{:keys [inventory] :as m} player resource]
  (let [total (->> inventory
                   vals
                   (map #(% resource))
                   (filter identity)
                   (apply +))
        inventory (-> (util/valmap #(assoc % resource 0) inventory)
                      (assoc-in [player resource] total))]
    (-> m
        (dissoc :playing)
        (assoc :inventory inventory))))
(defn monopolize! [game-name player resource]
  (swap! state update game-name monopolize player resource))

(defn road [m player]
  (-> m
      (dissoc :playing)
      (update :inventory #(transfer % "bank" player ["road" "road"]))))
(defn road! [game-name player]
  (swap! state update game-name road player))

(defn plenty [m player resource]
  (if (-> m :playing :partial)
    (-> m
        (dissoc :playing)
        (update-in [:inventory "bank" resource] dec)
        (update-in [:inventory player resource] safe+ 1))
    (-> m
        (assoc-in [:playing :partial] resource)
        (update-in [:inventory "bank" resource] dec)
        (update-in [:inventory player resource] safe+ 1))))
(defn plenty! [game-name player resource]
  (swap! state update game-name plenty player resource))
(defn get-plenty [game-name]
  (for [[resource count] (get-in @state [game-name :inventory "bank"])
        :when (pos? count)] [resource (inv->name resource)]))

(defn- steal [m from to knight?]
  (let [available (for [[resource quantity] (get-in m [:inventory from])
                        _ (range quantity)] resource)
        m (if knight?
            (-> m (dissoc :playing) (update-in [:knights to] safe+ 1))
            m)]
    (if (not-empty available)
      (let [stolen (rand-nth available)]
        (-> m
            (update-in [:inventory from stolen] dec)
            (update-in [:inventory to stolen] safe+ 1)))
      m)))
(defn steal-knight! [game-name from to]
  (swap! state update game-name steal from to true))
(defn steal-board! [game-name from to]
  (swap! state update game-name steal from to false))

(defn dec-edge [v]
  (edge-downgrade (update v 1 #(mod (dec %) 6))))
(defn inc-edge [v]
  (edge-downgrade (update v 1 #(mod (inc %) 6))))
(def e->e
  (into {}
        (for [i (range 19) j (range 6)
              :let [edge [i j]]
              :when (not (edge-downgrade* edge))]
          [edge
           (concat
            [(dec-edge edge) (inc-edge edge)]
            (when-let [edge (edge-upgrade* edge)]
              [(dec-edge edge) (inc-edge edge)]))])))

(defn road-length
  ([edges]
   (->> (for [[edge color] edges :when color]
          [color (road-length edges edge color #{})])
        (util/group-by-map first #(->> % (map second) (apply max)))
        (sort-by second >)))
  ([edges edge color done]
   (or
    (some->> edge
             e->e
             (filter #(-> % edges (= color)))
             (remove done)
             not-empty
             (map #(road-length edges % color (conj done edge)))
             (apply max)
             inc)
    1)))

(defn- allocate-resource [inventory [resource data]]
  (let [available (get-in inventory ["bank" resource])
        required (->> data (map :quantity) (apply +))
        num-required (->> data (map :player) distinct count)]
    (if (or (>= available required) (= 1 num-required))
      (reduce (fn [inventory {:keys [player quantity]}]
                (-> inventory
                    (update-in ["bank" resource] - quantity)
                    (update-in [player resource] safe+ quantity)))
              inventory
              data)
      inventory)))

(defn- safe> [a b]
  (or (not b) (> a b)))
(defn- roll [m]
  (let [{:keys [nodes edges knight knights longest-road outputs terrains robber inventory tp?]} m
        outputs (assoc outputs robber -1)
        dice [(inc (rand-int 6)) (inc (rand-int 6))]
        sum (apply + dice)
        yield (for [[i output terrain] (map list (range) outputs terrains)
                    :when (= output sum)
                    j (range 6)
                    :let [[color settlement] (-> [i j] node-downgrade nodes)]
                    :when (and settlement
                               (or (not tp?) (not= "red" color)))]
                {:player color
                 :resource terrain
                 :quantity (if (= "city" settlement) 2 1)})
        inventory (->> yield
                       (group-by :resource)
                       (reduce allocate-resource inventory))
        ;; next need to calculate knights and road points
        [[first-color first-knight]
         [_ second-knight]] (sort-by second > knights)
        knight (if (and
                    first-knight
                    (>= first-knight 3)
                    (safe> first-knight second-knight)
                    (safe> first-knight (second knight)))
                 [first-color first-knight]
                 knight)
        [[longest-color road-length]
         [_ second-road-length]] (road-length edges)
        longest-road (if (and
                          road-length
                          (>= road-length 5)
                          (safe> road-length second-road-length)
                          (safe> road-length (second longest-road)))
                       [longest-color road-length]
                       longest-road)]
    (assoc m :dice dice :inventory inventory :knight knight :longest-road longest-road)))

(defn roll! [game-name]
  (swap! state update game-name roll))

(defn victory-points [game-name color]
  (let [{:keys [nodes knight longest-road hands]} (@state game-name)
        [knight-color knight] knight
        [road-color road-length] longest-road
        infrastructure-points (->> nodes
                                   vals
                                   (filter (fn [[c]] (= c color)))
                                   (mapcat (fn [[_ type]]
                                             (case type
                                              "settlement" ["settlements"]
                                              "city" ["cities" "cities"])))
                                   frequencies)
        card-points (->> color
                         hands
                         (map :title)
                         (filter vp-card?)
                         frequencies)]
    (merge
     infrastructure-points
     card-points
     {"knight" (when (= color knight-color) knight)
      "road" (when (= color road-color) road-length)})))

(defonce _
  (if (.exists dump-file)
    (->> dump-file slurp read-string (reset! state))
    (do
      (add-game "asdf" false true)
      (doseq [terrain terrains]
        (send-inv! "asdf" "bank" terrain "red" 1)))))
