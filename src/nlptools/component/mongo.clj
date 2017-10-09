(ns nlptools.component.mongo
  (:require
   [monger.core :as mg]
   [monger.collection :as mc]
   [integrant.core :as ig]
  ))

(defprotocol Mongo
  (query [this coll] [this coll where] [this coll where fields]))


(defrecord Boundary [conn db]
  Mongo
  (query [this coll] (mc/find-maps db coll))
  (query [this coll where] (mc/find-maps db coll where))
  (query [this coll where fields] (mc/find-maps db coll where fields)))

(defmethod ig/init-key :nlptools.component/mongo [_ spec]
  (let [conn (mg/connect {:host (:server-name spec) :port (:port-number spec)})
        db (mg/get-db conn (:database-name spec))]
    (->Boundary conn db)))


(defmethod ig/halt-key! :nlptools.component/mongo [_ mongodb]
  (mg/disconnect (:conn mongodb)))
