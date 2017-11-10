(ns nlptools.corpus.entity
  "A corpus builder for opennlp entity extractor"
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [duct.logger :refer [log]]
   [nlptools.command :as cmd]
   [nlptools.corpus.core :refer [Corpus]]
   ))


(def ukey
  "this unit key"
  :nlptools.corpus/entity)

(def cmdkey
  "the command key for this unit"
  :corpus.entity)

(derive ukey :nlptools/corpus)

(s/def :corpus/entity string?)

(defmethod ig/pre-init-spec ukey [_]
  (s/keys :req-un [:corpus/entity
                   :corpus/filepath
                   :corpus/db
                   :corpus/logger]))


(defrecord EntityCorpus [filepath db entity logger]
  Corpus
  (build-corpus! [this]
    (log logger :info ::build-corpus! {:file filepath :entity entity})
    (let [entkey (keyword (str "entities." entity))
          resultset (.query db "nlp"  {:is_valid true entkey {"$exists" true}}["text" "entities"])]
      (with-open [w (io/writer filepath)]
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
          (log logger :info ::corpus-created {:total total :file filepath})
          )))
    this))
 
(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [db filepath entity logger]} spec]
    (->EntityCorpus filepath db entity logger)))

(defmethod cmd/help cmdkey [_]
  "corpus.entity - create a corpus file for an entity type model.")

(defmethod cmd/syntax cmdkey [_]
  "nlptools corpus.entity -c CONFIG-FILE -o CORPUS-FILE -t entity")

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [out mongodb text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:entity text
                             :db (ig/ref :nlptools.module/mongo)
                             :filepath out
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.module/mongo (assoc mongodb :logger (ig/ref :duct.logger/timbre))})
        system (ig/init (cmd/prep-igconfig config))
        corpus (get system ukey)]
    (.build-corpus! corpus)
    (printf "build %s entity corpus in: %s\n" (:entity corpus) (:filepath corpus) )
    (ig/halt! system)
    0))
