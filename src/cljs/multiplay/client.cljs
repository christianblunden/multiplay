(ns multiplay.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close! chan]]
            [cljs.reader :as reader]
            [multiplay.utils :refer [log host]]
            [multiplay.async.websocket :as websocket]
            [multiplay.views.arena]
            [goog.events]
            [goog.dom]))

;; Google Closure reference
;; http://docs.closure-library.googlecode.com/git/index.html
;; http://docs.closure-library.googlecode.com/git/closure_goog_dom_dom.js.html
;; http://docs.closure-library.googlecode.com/git/closure_goog_events_events.js.html

;; keyboard event bindings

(def key-down (atom nil))

(defn key-event->command
  [e]
  (let [code (.-keyCode e)]
    (case code
      38 :up
      40 :down
      87 :up
      83 :down
      nil)))

(defn bind-key-observer
  [command-chan]
  (go (while true
        (<! (timeout 50))
        (case @key-down
          :up   (>! command-chan [:player/up])
          :down (>! command-chan [:player/down])
          :not-matched)))
  (.addEventListener js/window "keydown"
                     (fn [e]
                       (.log js/console e)
                       (reset! key-down (key-event->command e))))
  (.addEventListener js/window "keyup"
                     (fn [e]
                       (reset! key-down nil))))

;; button click event bindings

(def clicked (atom nil))

(defn bind-arrow-click
  [command-chan]
  (go (while true
        (<! (timeout 50))
        (case @clicked
          :up   (>! command-chan [:player/up])
          :down (>! command-chan [:player/down])
          :not-matched)))
  (goog.events/listen (goog.dom/getElement "up-arrow") "touchstart"
                      #(reset! clicked :up))
  (goog.events/listen (goog.dom/getElement "up-arrow") "touchend"
                      #(reset! clicked nil))

  (goog.events/listen (goog.dom/getElement "down-arrow") "touchstart"
                      #(reset! clicked :down))
  (goog.events/listen (goog.dom/getElement "down-arrow") "touchend"
                      #(reset! clicked nil)))

;; client process
(defn receive-websocket
  [[type data :as event] render-channel]
  (case type
    :message (go (>! render-channel (reader/read-string data))) ; put game-state to rendering channel

    (log ["Silently ignoring" event])))

(defn spawn-client-process!
  [ws-send ws-receive command-chan render-channel]
  (go (while true
        ; block for input on either websocket or command channel
        (let [[event channel] (alts! [ws-receive command-chan])]
          (log ["event received" event])
          (condp = channel
            ws-receive (receive-websocket event render-channel)
            command-chan (>! ws-send event)
            )))))

;; main js entry point

(defn ^:export run
  []
  (.log js/console "pong!")
  (let [render-channel (multiplay.views.arena/create!)
        {:keys [ws-send ws-receive]} (websocket/connect! (str "ws://" host))
        command-channel (chan)]
    (spawn-client-process! ws-send ws-receive command-channel render-channel)
    (bind-key-observer command-channel)
    (bind-arrow-click command-channel)))
