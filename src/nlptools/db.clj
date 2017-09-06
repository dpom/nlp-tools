(ns nlptools.db
  (:require
   [clojure.java.jdbc :as j]
   [hikari-cp.core :as hik]
   [integrant.core :as ig]
  ))

(defrecord Boundary [spec])

(defmethod ig/init-key :nlptools/db [_ spec]
  (->Boundary {:datasource (hik/make-datasource (dissoc spec :logger))}))


(defmethod ig/halt-key! :nlptools/db [_ spec]
  (hik/close-datasource (:datasource spec)))
