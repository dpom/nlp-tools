(ns nlptools.corpus.intent
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   [nlptools.command :as cmd]
   [nlptools.corpus.core :refer [Corpus]]
   ))



(defrecord IntentCorpus [filepath db logger]
  Corpus
  (build-corpus [this]
    (log logger :info ::creating-corpus {:file filepath})
    (let [resultset (.query db "nlp" {:is_valid true} ["text" "entities"])]
      (with-open [w (io/writer filepath)]
        (let [total (reduce  (fn [counter {:keys [text entities]}]
                   (let [intent (get entities :intent "necunoscut")]
                     ;; (log @logger :debug ::write-line {:counter counter :intent intent :text text})
                     (.write w (format "%s %s" intent text))
                     (.newLine w)
                     (inc counter)))
                             0 resultset)]
          (log logger :info ::corpus-created {:total total :file filepath})
          )))
    this))
 
(defmethod ig/init-key :nlptools.corpus/intent [_ spec]
  (let [{:keys [db filepath logger]} spec]
    (->IntentCorpus filepath db logger)))

(defmethod cmd/help :corpus.intent [_]
  "corpus.intent - create a corpus file for an intent type classification model.")

(defmethod cmd/syntax :corpus.intent [_]
  "nlptools corpus.intent -c CONFIG-FILE -o CORPUS-FILE")

(defmethod cmd/run :corpus.intent [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {:nlptools.corpus/intent {:db (ig/ref :nlptools.module/mongo)
                                                :filepath (:out opts)
                                                :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.module/mongo (assoc (:mongodb opts) :logger (ig/ref :duct.logger/timbre))})
        system (ig/init (cmd/prep-igconfig config))
        corpus (:nlptools.corpus/intent system)]
    (.build-corpus corpus)
    (printf "build intent corpus in: %s\n" (:filepath corpus) )
    (ig/halt! system)
    0))
