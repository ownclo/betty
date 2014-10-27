(ns app.core
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

; TODO: handle nonexisting account
(defn deposit [account_id amount]
  (let [new-amount (raw (str "amount +" amount))]
    (update account (set-fields {:amount new-amount}))))

;; XXX: Remember to do-bet within a transaction!
(defn do-bet [rate amount account_id selection_id]
  (withdraw account_id amount)
  (insert bet (values {:rate rate
                       :amount amount
                       :account_id account_id
                       :selection_id selection_id})))

(defn get-risk [bet-record]
  (* (bet-record :amount)
     (bet-record :rate)))

(defn risk-for-seq [bets]
  (->> bets
       (map get-risk)
       (reduce +)))

(defn risk [selection_id]
  (->> (where {:selection_id selection_id})
       (select bet)
       (risk-for-seq)))


(defn insert-sample-type-tree []
  (let [eid (:id (insert event_type (values {:name "football"})))
        mid (:id (insert market_type (values {:name "winloss"
                                              :event_type_id eid})))]
    (insert selection_type (values [{:name "win"  :market_type_id mid}
                                    {:name "x"    :market_type_id mid}
                                    {:name "loss" :market_type_id mid}]))))

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
