(ns nlptools.tool.entity
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :as tool]
   [nlptools.model.core :as modl]
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
  (spec/known-keys :req-un [:nlptools/id
                            :nlptools/model
                            :nlptools/tokenizer 
                            :nlptools/logger]))



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
  tool/Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool! {:id id :model (modl/get-id model) :tokenizer (modl/get-id tokenizer) })
    (reset! finder (make-entity-finder (modl/get-model model) (modl/get-model tokenizer) logger)))
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (get-id [this] id)
  (apply-tool [this text]
    (let [entities (@finder text)]
      (log @logger :debug ::apply-tool {:entities entities})
      entities)))  

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id model tokenizer logger]} spec]
    (log logger :debug ::init {:id id})
    (let [classif (->EntityTool id model tokenizer (atom nil) (atom nil))]
      (tool/set-logger! classif logger)
      (tool/build-tool! classif)
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
        res (tool/apply-tool classifier "Vreau un televizor")]
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
    (printf "text: %s,\nfinds: %s\n" text (tool/apply-tool finder text))
    (ig/halt! system)
    0))
