(ns nlptools.module.mongo
  (:require
   [monger.core :as mg]
   [monger.collection :as mc]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
  ))

(defprotocol Mongo
  (query [this coll] [this coll where] [this coll where fields]))


(defrecord Boundary [conn db]
  Mongo
  (query [this coll] (mc/find-maps db coll))
  (query [this coll where] (mc/find-maps db coll where))
  (query [this coll where fields] (mc/find-maps db coll where fields)))

(defmethod ig/init-key :nlptools.module/mongo [_ {:keys [:server-name :port-number :database-name :logger]}]
  (let [conn (mg/connect {:host server-name :port port-number})
        db (mg/get-db conn database-name)]
    (log logger :info ::connect {:dbname database-name})
    (->Boundary conn db)))


(defmethod ig/halt-key! :nlptools.module/mongo [_ mongodb]
  (mg/disconnect (:conn mongodb)))
