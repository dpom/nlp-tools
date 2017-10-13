(ns nlptools.intent
  (:require
   [integrant.core :as ig]
   [opennlp.nlp :as onlp]
   [opennlp.tools.train :as train]
   [duct.logger :refer [log]]
   ))

(defprotocol Intent
  (init [this corpus logger])
  (get-intent [this text]))


(defrecord Boundary [cat-model get-category lang logger]
  Intent
  (init [this corpus newlogger]
    (let [filepath (:filepath corpus)]
      (reset! logger newlogger)
      (log @logger :info ::init-intent {:lang lang :file filepath})
      (reset! cat-model (train/train-document-categorization lang filepath))
      (reset! get-category (onlp/make-document-categorizer @cat-model))
      this))
  (get-intent [this text] (get (@get-category text) :best-category "necunoscut")))

(defmethod ig/init-key :nlptools/intent [_ spec]
  (let [{:keys [language corpus logger]} spec]
    (.init (->Boundary (atom nil) (atom nil) language (atom nil)) corpus logger)))
