(ns nlptools.tool.entity
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.tool.core :as tool]
   [nlptools.span :as nspan]
   [nlptools.spec :as spec]
   [nlptools.command :as cmd])
  (:import
   (opennlp.tools.tokenize Tokenizer)
   (opennlp.tools.util Span)
   (opennlp.tools.namefind NameFinderME
                           TokenNameFinderModel)
   ))


(def ukey
  "this unit key"
  :nlptools.tool/entity)

(def cmdkey
  "the command key for this unit"
  :tool.entity)

(derive ukey tool/corekey)

(defmethod ig/pre-init-spec ukey [_]
  (nsp/known-keys :req-un [:nlpcore/id
                            :nlpcore/model
                            :nlptools/tokenizer 
                            :nlpcore/logger]))



(defn make-entity-finder
  [^TokenNameFinderModel model ^Tokenizer tokenizer logger]
  (fn entity-finder
    [^String text]
    (let [finder (NameFinderME. model)
          tokens (.tokenize tokenizer text)
          matches (.find finder tokens)
          vals (Span/spansToStrings #^"[Lopennlp.tools.util.Span;" matches #^"[Ljava.lang.String;" tokens)]
      (log @logger :debug ::entity-finder {:found  (count matches)})
      (mapv (fn [^Span m v]
              {:entity (.getType m)
               :value {:value v}
               :start (.getStart m)
               :end (.getEnd m)
               :confidence (.getProb m)})
            matches vals))))

(defrecord EntityTool [id model tokenizer finder logger]
  core/Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool! {:id id :model (core/get-id model) :tokenizer (core/get-id tokenizer) })
    (reset! finder (make-entity-finder (core/get-model model) (core/get-model tokenizer) logger)))
  (apply-tool [this text _]
    (let [entities (@finder text)]
      (log @logger :debug ::apply-tool {:entities entities})
      entities)))  

(extend EntityTool
  core/Module
  core/default-module-impl)


(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id model tokenizer logger]} spec]
    (log logger :debug ::init {:id id})
    (let [classif (->EntityTool id model tokenizer (atom nil) (atom nil))]
      (core/set-logger! classif logger)
      (core/build-tool! classif)
      classif)))


(s/def ::entity string?)
(s/def ::start int?)
(s/def ::end int?)
(s/def ::value map?)
(s/def ::confidence double?)
(s/def ::entity-item (s/keys :req-un [::entity ::value ::confidence ::start ::end]))
(s/def ::result (s/coll-of ::entity-item))

(deftest apply-tool-test
  (let [config (merge (cmd/make-test-logger :error)
                      {ukey {:id "test entity"
                             :tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :model (ig/ref :nlptools.model/entity)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:id "test tokenizer"
                                                         :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/entity {:id "test model"
                                               :binfile "test/category.bin"
                                               :loadbin? true
                                               :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        classifier (get system ukey)
        res (core/apply-tool classifier "Vreau un televizor" {})]
    (is (s/valid? ::result res))
    (is (= "televizor" (get-in (first res) [:value :value])))))

(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - extract entity from a text"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -t TEXT -i MODEL_FILE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "run entity"
                             :tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :model (ig/ref :nlptools.model/entity)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:id "run tokenizer"
                                                         :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/entity {:id "run model"
                                               :binfile in
                                               :loadbin? true
                                               :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        finder (get system ukey)
        text (get opts :text "")]
    (printf "text: %s,\nfinds: %s\n" text (core/apply-tool finder text {}))
    (ig/halt! system)
    0))
