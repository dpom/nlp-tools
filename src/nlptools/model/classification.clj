(ns nlptools.model.classification
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [duct.logger :refer [log Logger]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.command :as cmd]
   [nlptools.test :as t]
   [nlptools.model.core :as modl])
  (:import
   [opennlp.tools.doccat
    DoccatModel
    DocumentCategorizerME
    DocumentSampleStream
    DoccatFactory]
   [opennlp.tools.util
    PlainTextByLineStream 
    TrainingParameters
    MarkableFileInputStreamFactory]
   ))

(def ukey
  "this unit key"
  :nlptools.model/classification)

(def cmdkey
  "the command key for this unit"
  :model.classification)

(derive ukey modl/corekey)


(defrecord ClassificationModel [id binfile, trainfile, language, model, logger]
  core/Model
  (load-model! [this]
    (log @logger :debug ::load-model {:file binfile})
    (reset! model (DoccatModel. (io/as-file binfile))))
  (train-model! [this]
    (log @logger :debug ::train {:file trainfile :lang language})
    (reset! model (DocumentCategorizerME/train language
                                              (DocumentSampleStream. (PlainTextByLineStream. (MarkableFileInputStreamFactory. (io/file trainfile))
                                                                                             "UTF-8"))
                                              (doto (TrainingParameters.)
                                                (.put TrainingParameters/ITERATIONS_PARAM "100")
                                                (.put TrainingParameters/CUTOFF_PARAM     "1"))
                                              (DoccatFactory.))))
  (save-model! [this]
    (log @logger :debug ::save-model! {:file binfile})
    (.serialize ^DoccatModel @model (io/as-file binfile)))
  (get-model [this] @model)
  )

(extend ClassificationModel
  core/Module
  (merge core/default-module-impl
         {:get-features (fn [{:keys [language]}] {:language language})}))

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id language binfile trainfile loadbin? logger] :or {loadbin? true}} spec
        classif (->ClassificationModel id binfile trainfile language (atom nil) (atom nil))]
    (log logger :info ::init {:id id :lang language :binfile binfile :loadbin? loadbin?})
    (core/set-logger! classif logger)
    (if loadbin?
      (core/load-model! classif)
      (core/train-model! classif))
    classif))

(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - build and save a classification model"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -i CORPUS-FILE -o MODEL-FILE -l LANGUAGE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in out language]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "classif"
                             :language language
                             :binfile out
                             :trainfile in
                             :loadbin? false
                             :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        model (ukey system)]
    (core/save-model! model)
    (printf "the model trained with %s was saved in %s\n" in out)
    (ig/halt! system)
    0))
