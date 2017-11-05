(ns nlptools.model.tokenizer
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [nlptools.command :as cmd]
   [duct.logger :refer [log]])
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

(defn ^TokenizerModel train-model
  "Returns a tokenizer model based on a given training file."
  ([lang in] (train-model lang in 5 100))
  ([lang in cut] (train-model lang in cut 100))
  ([lang in cut iter]
   (TokenizerME/train 
    (TokenSampleStream.
     (PlainTextByLineStream.
      (MarkableFileInputStreamFactory. (io/file in)) "UTF-8"))
    (TokenizerFactory. 
     lang nil false nil)
    (doto (TrainingParameters.)
      (.put TrainingParameters/ITERATIONS_PARAM (Integer/toString iter))
      (.put TrainingParameters/CUTOFF_PARAM     (Integer/toString cut))))))

(defn save-model
  "Save a tokenize model in file."
  [^TokenizerModel model out]
  (.serialize model (io/as-file out)))

(defn ^TokenizerModel load-model
  "Returns a tokenize model loaded from a binary file."
  [in]
  (TokenizerModel. (io/as-file in)))


(defmethod ig/init-key ::simple [_ spec]
  SimpleTokenizer/INSTANCE)

(defmethod ig/init-key ::whitespace [_ spec]
  WhitespaceTokenizer/INSTANCE)

(defmethod ig/init-key ::binary [_ spec]
  (load-model (:file spec)))

(defmethod ig/init-key ::trained [_ spec]
  (let [{:keys [language file]} spec]
    (train-model language file)))

(defmethod cmd/help :model.tokenizer [_]
  "model.tokenize - build and save a tokenize model")

(defmethod cmd/run :model.tokenizer [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in out language]} opts
        model (train-model  language in)]
    (save-model model out)
    (printf "the model trained with %s was saved in %s\n" in out)
    0))
