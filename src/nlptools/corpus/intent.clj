(ns nlptools.corpus.intent
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   [nlptools.spec :as spec]
   [nlptools.corpus.core :as corpus]
   [nlptools.module.mongo :as db]
   [nlptools.command :as cmd]))

(def ukey
  "this unit key"
  :nlptools.corpus/intent)

(def cmdkey
  "the command key for this unit"
  :corpus.intent)

(derive ukey corpus/corekey)


(defrecord IntentCorpus [filepath db logger]
  corpus/Corpus
  (build-corpus! [this]
    (log logger :info ::creating-corpus {:file filepath})
    (let [resultset (db/query db "nlp" {:is_valid true} ["text" "entities"])]
      (with-open [^java.io.BufferedWriter w (io/writer filepath)]
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
 
(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [db filepath logger]} spec]
    (->IntentCorpus filepath db logger)))

(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - create a corpus file for an intent type classification model."))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -c CONFIG-FILE -o CORPUS-FILE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {ukey {:db (ig/ref :nlptools.module/mongo)
                             :filepath (:out opts)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.module/mongo (assoc (:mongodb opts) :logger (ig/ref :duct.logger/timbre))})
        system (ig/init (cmd/prep-igconfig config))
        corpus (:nlptools.corpus/intent system)]
    (corpus/build-corpus! corpus)
    (printf "build intent corpus in: %s\n" (:filepath corpus) )
    (ig/halt! system)
    0))
