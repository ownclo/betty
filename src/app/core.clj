(ns app.core
  (use [korma.db :only [default-connection]])
  (use app.db)
  (use korma.core))


(defn select-account [account_id]
  (first (select account
                 (where {:id account_id}))))

(defn insert-account
  ([amount] (insert account (values {:amount amount})))
  ([] (insert account (values {:amount 0}))))

; TODO: handle nonexisting account
(defn withdraw [account_id amount]
  (let [acc (select-account account_id)
        new-amount (- (acc :amount) amount)]
    (when (< new-amount 0)
      (throw (Exception. "no money - no honey")))
    (update account (set-fields {:amount new-amount})
            (where {:id account_id}))))

; NOTE: subject to raw-exec optimisation
; TODO: handle nonexisting account
(defn deposit [account_id amount]
  (withdraw account_id (* -1 amount)))

;; XXX: Remember to do-bet within a transaction!
(defn do-bet [rate amount account_id selection_id]
  ; (assert (every? pos? [rate amount account_id selection_id]))
  (withdraw account_id amount)
  (insert bet (values {:rate rate
                       :amount amount
                       :account_id account_id
                       :selection_id selection_id})))

(defn get-risk [bet-record]
  (* (bet-record :amount)
     (bet-record :rate)))

(defn risk [selection_id]
  (let [s (select selection (with bet)
                  (where {:id selection_id}))]
    (->> s first :bet
         (map get-risk)
         (reduce +))))

(defn populate-db []
  (insert-sample-type-tree)
  (insert event (values {:name "Grand Finale" :event_type_id 1}))
  ; betting for win in grand finale
  (insert selection (values {:event_id 1 :selection_type_id 1}))
  (dotimes [_ 3] (insert-account 100.0)))

(defn -main []
  (try (println "DROPPING TABLES... " (drop-tables)) (catch Exception _))
  (println "CREATING TABLES... " (create-tables))
  (println "POPULATING DATABASE... " (populate-db))
  (println "OK"))
