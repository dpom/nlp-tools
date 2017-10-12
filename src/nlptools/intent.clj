(ns nlptools.intent
  (:require
   [integrant.core :as ig]
   [opennlp.nlp :as onlp]
   [opennlp.tools.train :as train]
   [duct.logger :refer [log]]
   ))

(defprotocol Intent
  (get-intent [this text]))


(defrecord Boundary [cat-model get-category]
  Intent
  (get-intent [this text] (get (get-category text) :best-category "necunoscut")))

(defmethod ig/init-key :nlptools/intent [_ spec]
  (let [{:keys [language corpus logger]} spec
        filepath (:filepath corpus)
        _ (log logger :info ::init-intent {:lang language :file filepath})
        cat-model (train/train-document-categorization language filepath)
        get-category (onlp/make-document-categorizer cat-model)]
    (->Boundary cat-model get-category)))


