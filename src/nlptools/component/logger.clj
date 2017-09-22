(ns nlptools.component.logger
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   ))

(defmethod ig/init-key :nlptools.component/logger [_ params]
  (timbre/merge-config! params)
  timbre/*config*)

