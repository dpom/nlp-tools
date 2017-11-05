(ns nlptools.classification
  (:require
   [integrant.core :as ig]
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
    (log logger :debug ::init)
    (->ClassifBoundary (make-document-classifier model tokenizer)  logger)))



(defmethod cmd/help :classification [_]
  "classification - classify a text")

(defmethod cmd/syntax :classification [_]
  "nlptools classification -t TEXT -i MODEL_FILE")

(defmethod cmd/run :classification [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {:nlptools/classification {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                                                 :model (ig/ref :nlptools.model.classification/binary)
                                                 :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.classification/binary {:file (:in opts)}})
        system (ig/init (cmd/prep-igconfig config))
        classifier (:nlptools/intent system)
        text (get opts :text "")]
    (printf "text: %s,\ncategory: %s\n" text (.get-category classifier text))
    (ig/halt! system)
    0))
