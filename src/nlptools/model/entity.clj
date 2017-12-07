(ns nlptools.model.entity
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [duct.logger :refer [log Logger]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.model.core :as model]
   [nlptools.spec :as spec]
   [nlptools.command :as cmd])
  (:import
   (opennlp.tools.namefind NameSampleDataStream
                           NameFinderME
                           TokenNameFinderModel
                           TokenNameFinderFactory
                           BioCodec)
   (opennlp.tools.util PlainTextByLineStream
                       TrainingParameters
                       MarkableFileInputStreamFactory)
   (java.nio.charset StandardCharsets)
   ))

(def ukey
  "this unit key"
  :nlptools.model/entity)

(def cmdkey
  "the command key for this unit"
  :model.entity)

(derive ukey model/corekey)

(s/def :model/entity string?)

(defmethod ig/pre-init-spec ukey [_]
  (nsp/known-keys :req-un [:nlpcore/id :nlpcore/logger]
                  :opt-un [:model/entity
                           :model/binfile
                           :model/trainfile
                           :model/loadbin?
                           :nlpcore/language]))

(defrecord EntityModel [id entity binfile trainfile language model logger]
  core/Model
  (load-model! [this]
    (log @logger :debug ::load-model {:file binfile})
    (reset! model (TokenNameFinderModel. (io/as-file binfile))))
  (train-model! [this]
    (log @logger :debug ::train {:file trainfile :lang language})
    (reset! model (NameFinderME/train language
                                      entity
                                      (NameSampleDataStream. (PlainTextByLineStream. (MarkableFileInputStreamFactory. (io/file trainfile))
                                                                                     StandardCharsets/UTF_8))
                                      (TrainingParameters/defaultParams)
                                      ;; (doto (TrainingParameters.)
                                      ;;   (.put TrainingParameters/ALGORITHM_PARAM "MAXENT")
                                      ;;   (.put TrainingParameters/ITERATIONS_PARAM "100")
                                      ;;   (.put TrainingParameters/CUTOFF_PARAM     "5"))
                                      ;; (TokenNameFinderFactory. "default" {} (BioCodec.))
                                      (TokenNameFinderFactory.)
                                      )))
  (save-model! [this]
    (log @logger :debug ::save-model! {:file binfile})
    (.serialize ^TokenNameFinderModel @model (io/as-file binfile)))
  (get-model [this] @model))

(extend EntityModel
  core/Module
  core/default-module-impl)

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id entity language binfile trainfile loadbin? logger] :or {loadbin? true}} spec
        classif (->EntityModel id entity binfile trainfile language (atom nil) (atom nil))]
    (log logger :info ::init {:id id :lang language :binfile binfile :loadbin? loadbin?})
    (core/set-logger! classif logger)
    (if loadbin?
      (core/load-model! classif)
      (core/train-model! classif))
    classif))

(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - build and save an entity model"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -i CORPUS-FILE -o MODEL-FILE -l LANGUAGE -t entity"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in out language text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "entity"
                             :language language
                             :entity text
                             :binfile out
                             :trainfile in
                             :loadbin? false
                             :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        model (ukey system)]
    (core/save-model! model)
    (printf "the %s entity model trained with %s was saved in %s\n" text in out)
    (ig/halt! system)
    0))
