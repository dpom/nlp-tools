(ns nlptools.intent
  (:require
   [integrant.core :as ig]
   [opennlp.nlp :as onlp]
   [opennlp.tools.train :as train]
   ))

(defprotocol Intent
  (get-intent [this text]))


(defrecord Boundary [cat-model get-category]
  Intent
  (get-intent [this text] (get (get-category text) :best-category "necunoscut")))

(defmethod ig/init-key :nlptools/intent [_ spec]
  (let [{:keys [language trainfile]} spec
        cat-model (train/train-document-categorization language trainfile)
        get-category (onlp/make-document-categorizer cat-model)]
    (->Boundary cat-model get-category)))


