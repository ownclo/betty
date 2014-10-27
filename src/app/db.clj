(ns app.db
  (use [korma.core :rename {belongs-to korma-belongs-to}]
       app.db-schema
       korma.db)
  (require [clojure.java.jdbc :as jdbc])
  (require [clojure.string :as str]))

(declare account bet selection event selection_type market_type event_type)

(defdb betty (postgres
               {:db "betty"
                :user "betty"
                :password "betty"}))

;; SCHEMA DESCRIPTION
;; Account, Bet and Selection are dynamically-changing entities.
(defentity account
  (schema [:amount "real not null"])
  (has-many bet))  ; bet.account_id refers to account.id

(defentity bet
  (schema [:amount "real not null"]
          [:rate "real not null"])
  (belongs-to account)  ; has account_id field
  (belongs-to selection))

(defentity selection
  (has-many bet)
  (belongs-to event)
  (belongs-to selection_type))

(defentity event
  (schema [:name "varchar(255)"])
  (belongs-to event_type)) ; or just 'event_type' string?

;; Market_Type, Selection_Type, Event_Type are representing
;; a static tree of selection possibilities. Could be replaced
;; with a nested JSON object so that text strings are used as
;; identifiers instead of ids.
;; Another possibility is to support a mapping from selection
;; to the set of bets. This would be a more NoSQL-ish solution.
(defentity selection_type
  (schema [:name "varchar(255)"])
  (belongs-to market_type)
  (has-many selection))

(defentity market_type
  (schema [:name "varchar(255)"])
  (has-many selection_type)
  (belongs-to event_type))

(defentity event_type
  (schema [:name "varchar(255)"])
  (has-many market_type)
  (has-many event))


;; DB initialization helpers
; XXX: order is important!
(def tables [event_type
             market_type
             selection_type
             event
             selection
             account
             bet])

(defn create-table-sql [entity]
  (let [ename (:name entity)
        schema (:schema entity)]
     (apply (partial jdbc/create-table-ddl ename) schema)))

(defn create-table [entity]
  (exec-raw (create-table-sql entity)))

(defn create-tables []
  (map create-table tables))

(defn drop-tables []
  (map (comp exec-raw jdbc/drop-table-ddl :name)
       (reverse tables)))

(defn insert-sample-type-tree []
  (let [eid (:id (insert event_type (values {:name "football"})))
        mid (:id (insert market_type (values {:name "winloss"
                                              :event_type_id eid})))]
    (insert selection_type (values [{:name "win"  :market_type_id mid}
                                    {:name "x"    :market_type_id mid}
                                    {:name "loss" :market_type_id mid}]))))
