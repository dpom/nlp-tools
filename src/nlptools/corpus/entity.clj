(ns nlptools.corpus.entity
  "A corpus builder for opennlp entity extractor"
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [duct.logger :refer [log]]
   [clojure.test :refer :all]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.command :as cmd]
   [nlptools.module.mongo :as db]
   [nlptools.spec :as spec]
   [nlptools.corpus.core :as corpus]))


(def ukey
  "this unit key"
  :nlptools.corpus/entity)

(def cmdkey
  "the command key for this unit"
  :corpus.entity)

(derive ukey corpus/corekey)

(s/def :corpus/entity string?)

(defmethod ig/pre-init-spec ukey [_]
  (nsp/known-keys :req-un [:nlpcore/id
                            :corpus/entity
                            :corpus/filepath
                            :corpus/db
                            :nlpcore/logger]))


(defrecord EntityCorpus [id filepath db entity logger]
  core/Corpus
  (build-corpus! [this]
    (log @logger :info ::build-corpus! {:id id :file filepath :entity entity})
    (let [entkey (keyword (str "entities." entity))
          resultset (db/query db "nlp"  {:is_valid true entkey {"$exists" true}}["text" "entities"])]
      (with-open [^java.io.BufferedWriter w (io/writer filepath)]
        (let [total (reduce  (fn [counter item]
                               (let [{:keys [text entities]} item 
                                     value  (get entities (keyword entity))
                                     reval (re-pattern (str "(?i)" value))
                                     ]
                     ;; (log @logger :debug ::write-line {:counter counter :entity entity :text text})
                                 (.write w (str/replace text reval (str "<START:" entity "> " value " <END>")))
                                 (.newLine w)
                                 (inc counter)))
                             0 resultset)]
          (log @logger :info ::corpus-created {:id id :total total :file filepath}))))
    this)
  (get-corpus [this] filepath))

(extend EntityCorpus
  core/Module
  core/default-module-impl)
 
(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id db filepath entity logger]} spec
        corpus (->EntityCorpus id filepath db entity (atom nil))]
    (core/set-logger! corpus logger)
    corpus))


(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - create a corpus file for an entity type model."))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -c CONFIG-FILE -o CORPUS-FILE -t entity"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [out mongodb text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "entity corpus"
                             :entity text
                             :db (ig/ref :nlptools.module/mongo)
                             :filepath out
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.module/mongo (assoc mongodb :logger (ig/ref :duct.logger/timbre))})
        system (ig/init (cmd/prep-igconfig config))
        corpus (get system ukey)]
    (core/build-corpus! corpus)
    (printf "build %s entity corpus in: %s\n" (:entity corpus) (:filepath corpus) )
    (ig/halt! system)
    0))
