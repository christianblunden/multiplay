(ns multiplay.system
  (:require [clojure.core.async :refer [chan close!]]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [multiplay.server :as server]))

(defn init
  []
  {;; channel for new websocket connections
   :connection-chan (chan)})

(defn jetty-configurator
  [system]
  (ws/configurator (system :connection-chan)))

(defn start!
  [system]
  (println "Starting system")

  (server/spawn-connection-process! (:connection-chan system))

  (assoc system
    :server (run-jetty server/handler
                       {:join? false
                        :port 8080
                        :configurator (jetty-configurator system)})))


(defn stop!
  [system]
  (println "Stopping system")
  (when-let [server (:server system)]
    (.stop server))
  (close! (:connection-chan system))

  (dissoc system :server))
