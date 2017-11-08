(ns nlptools.model.classification
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [duct.logger :refer [log Logger]]
   [nlptools.model.core :refer [Model]]
   [nlptools.command :as cmd])
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


(defrecord ClassificationModel [binfile, trainfile, language, model, logger]
  Model
  (load-model [this]
    (log @logger :debug ::load-model {:file binfile})
    (reset! model (DoccatModel. (io/as-file binfile))))
  (train-model [this]
    (log @logger :debug ::train {:file trainfile :lang language})
    (reset! model (DocumentCategorizerME/train language
                                              (DocumentSampleStream. (PlainTextByLineStream. (MarkableFileInputStreamFactory. (io/file trainfile))
                                                                                             "UTF-8"))
                                              (doto (TrainingParameters.)
                                                (.put TrainingParameters/ITERATIONS_PARAM "100")
                                                (.put TrainingParameters/CUTOFF_PARAM     "1"))
                                              (DoccatFactory.))))
  (save-model [this]
    (log @logger :debug ::save-model {:file binfile})
    (.serialize ^DoccatModel @model (io/as-file binfile)))
  (get-model [this]
    @model)
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  )

(s/def ::binfile string?)
(s/def ::trainfile string?)
(s/def ::language string?)
(s/def ::loadbin? boolean?)
;; (s/def ::logger #(instance? Logger %))
(s/def ::logger map?)
(s/def ::binconfig (s/keys :req-un [::binfile ::logger]
                           :opt-un [::trainfile ::loadbin? ::language]))
(s/def ::trainconfig (s/keys :req-un [::language ::trainfile ::loadbin? ::logger]
                             :opt-un [::binfile]))

(derive :nlptools.model/classification :nlptools/model)

(defmethod ig/pre-init-spec :nlptools.model/classification [_]
 ;; (s/or ::binconfig ::trainconfig))
  (s/keys :req-un [::logger]
          :opt-un [::binfile ::trainfile ::loadbin? ::language]))


(defmethod ig/init-key :nlptools.model/classification [_ spec]
  (let [{:keys [language binfile trainfile loadbin? logger] :or {loadbin? true}} spec
        classif (->ClassificationModel  binfile trainfile language (atom nil) (atom nil))]
    (log logger :info ::init {:lang language :binfile binfile :loadbin? loadbin?})
    (.set-logger! classif logger)
    (if loadbin?
      (.load-model classif)
      (.train-model classif))
    classif))

(defmethod cmd/help :model.classification [_]
  "model.classification - build and save a classification model")

(defmethod cmd/syntax :model.classification [_]
  "nlptools model.classification -i CORPUS-FILE -o MODEL-FILE -l LANGUAGE")

(defmethod cmd/run :model.classification [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in out language]} opts
        config (merge (cmd/make-logger opts)
                      {:nlptools.model/classification {:language language
                                                       :binfile out
                                                       :trainfile in
                                                       :loadbin? false
                                                       :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        model (:nlptools.model/classification system)]
    (.save-model model)
    (printf "the model trained with %s was saved in %s\n" in out)
    (ig/halt! system)
    0))
