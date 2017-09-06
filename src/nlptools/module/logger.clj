(ns nlptools.module.logger
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   ))

(defmethod ig/init-key :nlptools.module/logger [_ params]
  (timbre/merge-config! params)
  timbre/*config*)

