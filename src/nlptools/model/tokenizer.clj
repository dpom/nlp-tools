(ns nlptools.model.tokenizer
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.spec :as spec]
   [nlptools.model.core :as model]
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


(defrecord TokModel [id binfile, trainfile, language, model, logger]
  core/Model
  (load-model! [this]
    (log @logger :debug ::load-model {:id id :file binfile})
    (reset! model (TokenizerModel. (io/as-file binfile))))
  (train-model! [this]
    (log @logger :debug ::train {:id id :file trainfile :lang language})
    (reset! model (TokenizerME/train (TokenSampleStream.
                                      (PlainTextByLineStream.
                                       (MarkableFileInputStreamFactory. (io/file trainfile)) "UTF-8"))
                                     (TokenizerFactory. language nil false nil)
                                     (doto (TrainingParameters.)
                                       (.put TrainingParameters/ITERATIONS_PARAM "100")
                                       (.put TrainingParameters/CUTOFF_PARAM     "5")))))
  (save-model! [this]
    (log @logger :debug ::save-model! {:id id :file binfile})
    (.serialize ^TokenizerModel @model (io/as-file binfile)))
  (get-model [this] @model))

(extend TokenizerModel
  core/Module
  core/default-module-impl)

(defrecord SimpleTokModel [logger]
  core/Model
  (load-model! [this]
    (log @logger :debug ::load-model! {:id "SimpleTokenizer" :action :no-action}))
  (train-model! [this]
    (log @logger :debug ::train-model! {:id "SimpleTokenizer" :action :no-action}))
  (save-model! [this]
    (log @logger :debug ::save-model! {:id "SimpleTokenizer" :action :no-action}))
  (get-model [this] SimpleTokenizer/INSTANCE))

(extend SimpleTokModel
  core/Module
  (merge core/default-module-impl
         {:get-id (fn [_] "SimpleTokenizer")}))


(defrecord WhitespaceTokModel [logger]
  core/Model
  (load-model! [this]
    (log @logger :debug ::load-model! {:action :no-action}))
  (train-model! [this]
    (log @logger :debug ::train-model!  {:action :no-action}))
  (save-model! [this]
    (log @logger :debug ::save-model! {:action :no-action}))
  (get-model [this] WhitespaceTokenizer/INSTANCE))

(extend WhitespaceTokModel
  core/Module
  (merge core/default-module-impl
         {:get-id (fn [_] "WhitespaceTokenizer")}))

(defmethod ig/init-key ::simple [_ spec]
  (let [{:keys [logger]} spec
        tokenizer (->SimpleTokModel (atom nil))]
    (log logger :info ::init)
    (core/set-logger! tokenizer logger)
    tokenizer))



