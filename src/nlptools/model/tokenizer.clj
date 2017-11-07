(ns nlptools.model.tokenizer
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   [nlptools.model.core :refer [Model]]
   [nlptools.command :as cmd])
  (:import
   (opennlp.tools.tokenize TokenizerME
                           SimpleTokenizer
                           WhitespaceTokenizer
                           TokenizerModel
                           TokenSampleStream
                           TokenizerFactory)
   (opennlp.tools.util PlainTextByLineStream
                       TrainingParameters
                       MarkableFileInputStreamFactory)
   ))


(defrecord TokenizerModel [binfile, trainfile, language, model, logger]
  Model
  (load-model [this]
    (log @logger :debug ::load-model {:file binfile})
    (reset! model (TokenizerModel. (io/as-file binfile))))
  (train-model [this]
    (log @logger :debug ::train {:file trainfile :lang language})
    (reset! model (TokenizerME/train (TokenSampleStream.
                                      (PlainTextByLineStream.
                                       (MarkableFileInputStreamFactory. (io/file trainfile)) "UTF-8"))
                                     (TokenizerFactory. language nil false nil)
                                     (doto (TrainingParameters.)
                                       (.put TrainingParameters/ITERATIONS_PARAM "100")
                                       (.put TrainingParameters/CUTOFF_PARAM     "5")))))
  (save-model [this]
    (log @logger :debug ::save-model {:file binfile})
    (.serialize ^TokenizerModel @model (io/as-file binfile)))
  (get-model [this]
    @model)
  (set-logger [this newlogger]
    (reset! logger newlogger))
  )

(defrecord SimpleTokenizerModel [model, logger]
  Model
  (load-model [this]
    (log @logger :debug ::load-model)
    (reset! model SimpleTokenizer/INSTANCE))
  (train-model [this]
    (log @logger :debug ::train ))
  (save-model [this]
    (log @logger :debug ::save-model))
  (get-model [this]
    @model)
  (set-logger [this newlogger]
    (reset! logger newlogger))
  )

(defrecord WhitespaceTokenizerModel [model, logger]
  Model
  (load-model [this]
    (log @logger :debug ::load-model)
    (reset! model WhitespaceTokenizer/INSTANCE))
  (train-model [this]
    (log @logger :debug ::train ))
  (save-model [this]
    (log @logger :debug ::save-model))
  (get-model [this]
    @model)
  (set-logger [this newlogger]
    (reset! logger newlogger))
  )



(derive :nlptools.model/tokenizer :nlptools/model)
(derive :nlptools.model.tokenizer/simple :nlptools.model/tokenizer)


(defmethod ig/init-key ::simple [_ spec]
  (let [{:keys [logger]} spec
        tokenizer (->SimpleTokenizerModel (atom nil) (atom nil))]
    (log logger :info ::init-simple-tokenizer)
    (.set-logger tokenizer logger)
    (.load-model tokenizer)
    tokenizer))



