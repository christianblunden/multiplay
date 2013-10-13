(ns multiplay.main
  (:require [multiplay.system :as system])
  (:gen-class))

(defn -main
  [& args]
  (system/start! (system/init)))
