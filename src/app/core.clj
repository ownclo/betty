(ns app.core)
(require '[app.db :as db])

(def greeting "Hello World")

(defn -main []
  (println greeting)
  (println "DUAH! Ai'we greeted you")
  42)
