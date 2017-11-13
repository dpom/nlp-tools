(ns nlptools.tool.entity
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool corekey]]
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

(derive ukey corekey)

(defmethod ig/pre-init-spec corekey [_]
  (spec/known-keys :req-un [:nlptools/model
                            :nlptools/tokenizer 
                            :nlptools/logger]))


(defn- to-native-span
  [^Span span]
  (nspan/make-span (.getStart span) (.getEnd span) (.getType span)))


(defn make-entity-finder
  [^TokenNameFinderModel model ^Tokenizer tokenizer logger]
  (fn entity-finder
    [text]
    {:pre [(string? text)]}
    (let [finder (NameFinderME. model)
          tokens (.tokenize tokenizer  ^String text)
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

(defrecord EntityTool [model tokenizer finder logger]
  Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool)
    (reset! finder (make-entity-finder (.get-model model) (.get-model tokenizer) logger)))
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (apply-tool [this text]
    (let [entities (@finder text)]
      (log @logger :debug ::apply-tool {:entities entities})
      entities)))  

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [model tokenizer logger]} spec]
    (log logger :debug ::init)
    (let [classif (->EntityTool model tokenizer (atom nil) (atom nil))]
      (.set-logger! classif logger)
      (.build-tool! classif)
      classif)))



(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - extract entity from a text"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -t TEXT -i MODEL_FILE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :model (ig/ref :nlptools.model/entity)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/entity {:binfile in
                                               :loadbin? true
                                               :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        finder (get system ukey)
        text (get opts :text "")]
    (printf "text: %s,\nfinds: %s\n" text (.apply-tool finder text))
    (ig/halt! system)
    0))
