(ns testapi.db.core
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [testapi.config :refer [env]]))


(defn startdb []
  (println (:database-url env))
  (-> env :database-url d/connect)
)

(defstate conn
          :start (startdb)                  ;(-> env :database-url d/connect)
          :stop (-> conn .release))

(defn create-schema []
  (let [schema [{:db/ident              :user/id
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/first-name
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/last-name
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/email
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}]]
    @(d/transact conn schema)))

(defn entity [conn id]
  (d/entity (d/db conn) id))

(defn touch [conn results]
  "takes 'entity ids' results from a query
    e.g. '#{[272678883689461] [272678883689462] [272678883689459] [272678883689457]}'"
  (let [e (partial entity conn)]
    (map #(-> % first e d/touch) results)))

(defn add-user [conn {:keys [id first-name last-name email]}]
  @(d/transact conn [{:db/id           id
                      :user/first-name first-name
                      :user/last-name  last-name
                      :user/email      email}]))


;; (defn q_search_employee [field text]
;;   '[:find ?p
;;      :in $ 
;;      :where
;;      [?p field text]
;;      ] )

(defmacro q_search_employee [field text]
  `''[:find ~'?p  :where [~'?p ~field ~text] ]
)

(defn find-employee2 [name]
  (let [employees (d/q (eval (q_search_employee :employee/english "Nacho"))  
                       (d/db conn))
                    ]
   
    (touch conn employees)
  )
)



(defn find-employee [name]
  (let [employees (d/q '[:find ?employee
                         :in $ ?reference ?name
                         :where
                         [?employee ?reference ?name]
]
                      (d/db conn)
                      :employee/english
                      name
                      )
                    ]
    (touch conn employees)
  )
)


(defn find-user [login]
  (let [users (d/q '[:find ?login ?e ?dm ?tm ?lang
                      :in $ ?login
                      :where
                      [?u :user/code ?login]
                      [?u :user/employee ?e]
                      [?u :user/datemask ?dm]
                      [?u :user/timemask ?tm]
                      [?u :user/language ?lang]
                     ]
                     ;; [:find ?p
                     ;;  :in $ ?login
                     ;;  :where
                     ;;  [?p :user/code ?login]
                     ;; ]
                     (d/db conn) login)
    ]
    ;(touch conn users)
    users
  )
)


