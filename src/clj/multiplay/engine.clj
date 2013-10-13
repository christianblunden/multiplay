(ns multiplay.engine
  (:require [clojure.core.async :refer [go alts! <! >! timeout]]
            [multiplay.game.params :as params]
            [multiplay.game.core   :as game-core]))

(defn spawn-engine-process!
  [command-chan game-state-channel]
  (go (loop [game-state game-core/initial-game-state
             commands   []
             timer      (timeout (long params/tick-ms))]
        (let [[v c] (alts! [timer command-chan] :priority true)]
          (condp = c
            command-chan (when v
                           (recur game-state (conj commands v) timer))
            timer        (do (let [updated-game-state (game-core/advance game-state commands)]
                               (>! game-state-channel updated-game-state)
                               (recur updated-game-state [] (timeout (long params/tick-ms))))))))

      (println "Exiting engine process")))


(defn game-state-emitter
  [game-state-channel clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (doseq [[player-id client-chan] @clients-atom]
         (>! client-chan (-> game-state
                             pr-str)))
       (recur (<! game-state-channel))))
   (println "Exiting game state emitter loop")))
