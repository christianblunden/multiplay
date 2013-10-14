(ns multiplay.async.websocket
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan put!]]
            [goog.events]
            [goog.net.WebSocket]
            [goog.net.WebSocket.EventType :as Events]))

(defn connect!
  [url]
  (let [ws  (goog.net.WebSocket.)
        ws-send  (chan)
        ws-receive (chan)]
    (goog.events.listen ws Events/OPENED (fn [e] (put! ws-receive [:opened e])))
    (goog.events.listen ws Events/CLOSED (fn [e] (put! ws-receive [:closed e])))
    (goog.events.listen ws Events/MESSAGE (fn [e] (put! ws-receive [:message (.-message e)])))
    (goog.events.listen ws Events/ERROR (fn [e] (put! ws-receive [:error e])))
    (.open ws url)
    (go (loop [msg (<! ws-send)]
          (when msg
            (.send ws msg)
            (recur (<! ws-send)))))
    {:ws-send ws-send :ws-receive ws-receive}))


