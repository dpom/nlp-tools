(ns nlptools.model.classification
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [nlptools.command :as cmd]
   [duct.logger :refer [log]])
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

(defn ^DoccatModel train-model
  "Returns a classification model based on a given training file."
  ([lang in] (train-model lang in 1 100))
  ([lang in cut] (train-model lang in cut 100))
  ([lang in cut iter]
   (DocumentCategorizerME/train 
    lang
    (DocumentSampleStream.
     (PlainTextByLineStream.
      (MarkableFileInputStreamFactory. (io/file in)) "UTF-8"))
    (doto (TrainingParameters.)
      (.put TrainingParameters/ITERATIONS_PARAM (Integer/toString iter))
      (.put TrainingParameters/CUTOFF_PARAM     (Integer/toString cut)))
    (DoccatFactory.))))

(defn save-model
  "Save a classification model in file."
  [^DoccatModel model out]
  (.serialize model (io/as-file out)))

(defn ^DoccatModel load-model
  "Returns a classification model loaded from a binary file."
  [in]
  (DoccatModel. (io/as-file in)))

(defmethod ig/init-key ::binary [_ spec]
  (load-model (:file spec)))

(defmethod ig/init-key ::trained [_ spec]
  (let [{:keys [language file]} spec]
    (train-model language file)))

(defmethod cmd/help :model.classification [_]
  "model.classification - build and save a classification model")

(defmethod cmd/syntax :model.classification [_]
  "nlptools model.classification -i CORPUS-FILE -o MODEL-FILE -l LANGUAGE")

(defmethod cmd/run :model.classification [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in out language]} opts
        model (train-model  language in)]
    (save-model model out)
    (printf "the model trained with %s was saved in %s\n" in out)
    0))
