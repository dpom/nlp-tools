(ns nlptools.tool.classification
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool corekey]]
   [nlptools.spec :as spec]
   [nlptools.command :as cmd])
  (:import
   (opennlp.tools.tokenize Tokenizer)
   (opennlp.tools.doccat DoccatModel
                         DocumentCategorizerME)))


(def ukey
  "this unit key"
  :nlptools.tool/classification)

(def cmdkey
  "the command key for this unit"
  :tool.classification)

(derive ukey corekey)

(defmethod ig/pre-init-spec corekey [_]
  (spec/known-keys :req-un [:nlptools/model
                            :nlptools/tokenizer
                            :nlptools/logger]))


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

(defrecord ClassificationTool [model tokenizer classifier logger]
  Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool)
    (reset! classifier (make-document-classifier (.get-model model) (.get-model tokenizer))))
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (apply-tool [this text]
    (let [resp (@classifier text)]
      (log @logger :debug ::apply-tool {:category resp :probabilities (meta resp)})
      (get resp :best-category "necunoscut"))))

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [model tokenizer logger]} spec]
    (log logger :debug ::init)
    (let [classif (->ClassificationTool model tokenizer (atom nil) (atom nil))]
      (.set-logger! classif logger)
      (.build-tool! classif)
      classif)))



(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - classify a text"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools" (name cmdkey) " -t TEXT -i MODEL_FILE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                          :model (ig/ref :nlptools.model/classification)
                          :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/classification {:binfile in
                                                       :loadbin? true
                                                       :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        classifier (get system ukey)
        text (get opts :text "")]
    (printf "text: %s,\ncategory: %s\n" text (.apply-tool classifier text))
    (ig/halt! system)
    0))
