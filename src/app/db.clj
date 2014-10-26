(ns app.db)
(use 'korma.db 'korma.core)
(require '[clojure.string :as str])

(defdb betty (postgres {:db "betty"
                        :user "betty"
                        :password "betty"}))

(declare account bet selection event selection_type market_type event_type)

;; Account, Bet and Selection are dynamically-changing entities.
(defentity account
  (entity-fields :amount)
  (has-many bet))  ; bet.account_id = account.id

(defentity bet
  (entity-fields :amount :rate :risk)
  (belongs-to account)  ; has account_id field
  (belongs-to selection)) ; or just have 'selection_type' string?

(defentity selection
  (has-many bet)
  (belongs-to event)
  (belongs-to selection_type))

(defentity event
  (belongs-to event_type)) ; or just 'event_type' string?

;; Market_Type, Selection_Type, Event_Type are representing
;; a static tree of selection possibilities. Could be replaced
;; with a nested JSON object so that text strings are used as
;; identifiers instead of ids.
;; Another possibility is to support a mapping from selection
;; to the set of bets. This would be a more NoSQL-ish solution.
(defentity selection_type
  (belongs-to market_type))

(defentity market_type
  (has-many selection_type)
  (belongs-to event_type))

(defentity event_type
  (has-many market_type))

;; CORRESPONDING DATABASE SCHEMAS (get them automatically?)
