(ns multiplay.views.arena
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async
             :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [multiplay.utils :refer [log]]
            [multiplay.game.params :as game-params]
            [goog.events]
            [goog.dom]))

; Canvas reference: http://www.w3schools.com/tags/ref_canvas.asp

;;; TODO - complete you client side rendering game play here!!

(defn draw-players 
  [context {:keys [players]}]
  (set! (.-font context) "12px Arial")
  (set! (.-textAlign context) "start")
  (set! (.-fillStyle context) "#000")
  (doseq [{id :id name :name [x y] :position :as player} players]
    (log ["draw-player" player])
    (doto context
      (.fillText (str "Player " id " : " name) x y))))

(defn update-view
  [canvas game-state]
  (let [context (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)]
    (doto context
      (.clearRect 0 0 w h)
      (draw-players game-state))))

(defn create!
  []
  (let [canvas (.getElementById js/document "canvas")
        c (chan (sliding-buffer 1))]
    (go (loop [game-state (<! c)]
          (update-view canvas game-state)
          (recur (<! c))))
    c))
