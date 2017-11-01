(ns nlptools.classification
  (:require
   [integrant.core :as ig]
   [opennlp.nlp :as onlp]
   [opennlp.tools.train :as train]
   [duct.logger :refer [log]]
   [nlptools.command :as cmd]
   )
  (:import
   (opennlp.tools.tokenize Tokenizer)
   (opennlp.tools.doccat DoccatModel
                         DocumentCategorizerME)))

(defn parse-categories [outcomes-string outcomes]
  "Given a string that represents the opennlp outcomes and an array of
  probability outcomes, zip them into a map of category-probability pairs"
  (zipmap
   (map first (map rest (re-seq #"(\w+)\[.*?\]" outcomes-string)))
   outcomes))

(defn make-document-classifier
  [^DoccatModel model ^Tokenizer tokenizer]
  (fn document-classifier
    [text]
    {:pre [(string? text)]}
    (let [classifier (DocumentCategorizerME. model)
          tokens (.tokenize tokenizer  ^String text)
          outcomes (.categorize classifier tokens)]
      (with-meta
        {:best-category (.getBestCategory classifier outcomes)}
        {:probabilities (parse-categories
                         (.getAllResults classifier outcomes)
                         outcomes)}))))

(defprotocol Classif
  (get-category [this text]))

(defrecord ClassifBoundary [classifier logger]
  Classif
  (get-category [this text]
    (let [resp (classifier text)]
      (log logger :debug ::get-category {:category resp :probabilities (meta resp)})
      (get resp :best-category "necunoscut"))))

(defmethod ig/init-key :nlptools/classification [_ spec]
  (let [{:keys [model tokenizer logger]} spec]
    (->ClassifBoundary model  logger)))



(defmethod cmd/help :classification [_]
  "classification - classify a text")


(defmethod cmd/run :classification [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {:nlptools/classification {:model (ig/ref :nlptools.model.tokenizer/simple)
                                         :corpus {:filepath (:in opts)}
                                         :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        intent (:nlptools/intent system)
        text (get opts :text "")]
    (printf "text: %s,\nintent: %s\n" text (.get-intent intent text))
    (ig/halt! system)
    0))
