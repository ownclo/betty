(ns app.db-schema
  (use [korma.core :rename {belongs-to korma-belongs-to}]))

(defn default-schema-if-none [ent]
  (if-not (ent :schema)
    (assoc ent :schema [[(ent :pk) "bigserial primary key"]])
    ent))

(defn schema [ent & columns]
  (let [names (map first columns)]
    (-> ent
        (default-schema-if-none)
        ;((partial apply entity-fields) names)
        (update-in [:schema] concat columns))))

;; Override korma's belongs-to macro in order to add
;; description to the :schema.
(defmacro belongs-to [ent sub-ent & [opts]]
  `(let [sub-name# ~(name sub-ent)
         fk# (or (:fk ~opts)
                 (str sub-name# "_id"))
         desc# (str "int not null references " sub-name# "(id)")
        ]
     (-> ~ent
         (default-schema-if-none)
         (update-in [:schema] concat [[fk# desc#]])
         (korma-belongs-to ~sub-ent ~opts))))

