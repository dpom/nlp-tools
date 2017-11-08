(ns nlptools.tool.classification
  (:require
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool]]
   [nlptools.command :as cmd])
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

(defprotocol Classification
  (get-category [this text]))

(defrecord ClassifierTool [model tokenizer classifier logger]
  Tool
  (build-tool [this]
    (log @logger :debug ::building-tool)
    (reset! classifier (make-document-classifier (.get-model model) (.get-model tokenizer))))
  (set-logger [this newlogger]
    (reset! logger newlogger))
  Classification
  (get-category [this text]
    (let [resp (@classifier text)]
      (log @logger :debug ::get-category {:category resp :probabilities (meta resp)})
      (get resp :best-category "necunoscut"))))

(defmethod ig/init-key :nlptools.tool/classification [_ spec]
  (let [{:keys [model tokenizer logger]} spec]
    (log logger :debug ::init)
    (let [classif (->ClassifierTool model tokenizer (atom nil) (atom nil))]
      (.set-logger classif logger)
      (.build-tool classif)
      classif)))



(defmethod cmd/help :tool.classification [_]
  "tool.classification - classify a text")

(defmethod cmd/syntax :tool.classification [_]
  "nlptools tool.classification -t TEXT -i MODEL_FILE")

(defmethod cmd/run :tool.classification [_ options summary]
  (let [opts  (cmd/set-config options)
        k :nlptools.tool/classification
        {:keys [in text]} opts
        config (merge (cmd/make-logger opts)
                      {k {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                          :model (ig/ref :nlptools.model/classification)
                          :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/classification {:binfile in
                                                       :loadbin? true
                                                       :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        classifier (get system k)
        text (get opts :text "")]
    (printf "text: %s,\ncategory: %s\n" text (.get-category classifier text))
    (ig/halt! system)
    0))
