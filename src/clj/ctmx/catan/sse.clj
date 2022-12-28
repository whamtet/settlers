(ns ctmx.catan.sse
    (:require
      [clojure.set :as set]
      [ctmx.catan.state :as state]
      [ctmx.render :as render]
      [ring.adapter.undertow.websocket :as ws]))

(defn- dissoc-in [m ks]
  (update-in m (pop ks) dissoc (peek ks)))

(defonce connections (atom {}))

(defn add-connection [game-name color]
  (fn [{:keys [channel]}]
    (swap! connections assoc-in [game-name color] channel)))

(defn remove-connection [game-name color]
  (fn [_]
    (swap! connections dissoc-in [game-name color])))

(defn- send-retry
  ([game-name e recipients]
   (future (send-retry game-name e (set recipients) 19)))
  ([game-name e recipients retries]
   (let [available (get @connections game-name)
         leftovers (set/difference recipients (set (keys available)))]
     (doseq [[user connection] available :when (recipients user)]
       (ws/send e connection))
     (when (and (pos? retries) (not-empty leftovers))))))

(defn send!
  ([game-name html]
   (send! game-name html state/valid-color?))
  ([game-name html recipients]
   (send-retry game-name (render/html html) recipients)
   nil))

(defn send-color!
  ([game-name f]
   (send-color! game-name f state/valid-color?))
  ([game-name f colors]
    (doseq [color colors]
      (send! game-name (f color) #{color}))))

(defn send-others! [game-name color html]
  (send! game-name html (disj state/valid-color? color)))
