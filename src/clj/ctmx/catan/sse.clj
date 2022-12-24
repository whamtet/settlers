(ns ctmx.catan.sse
    (:require
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
