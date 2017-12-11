(ns nlptools.tool.classification
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.spec :as spec]
   [nlptools.test :as t]
   [nlptools.command :as cmd]
   [nlptools.tool.core :as tool])
  (:import
   (opennlp.tools.tokenize Tokenizer)
   (opennlp.tools.doccat DoccatModel
                         DocumentCategorizerME)))


(def ukey
  "this unit key"
  :nlptools.tool/classification)

(def cmdkey
  "the command key for this unit"
  :tool.classification)

(derive ukey tool/corekey)

(defmethod ig/pre-init-spec ukey [_]
  (nsp/known-keys :req-un [:nlpcore/id
                           :nlpcore/model
                           :nlptools/tokenizer
                           :nlpcore/logger]))


(defn parse-categories
  "Given a string that represents the opennlp outcomes and an array of
  probability outcomes, zip them into a map of category-probability pairs"
  [outcomes-string outcomes]
  (zipmap
   (map first (map rest (re-seq #"(\w+)\[.*?\]" outcomes-string)))
   outcomes))

(defn make-document-classifier
  [^DoccatModel model ^Tokenizer tokenizer]
  (fn document-classifier
    [^String text]
    (let [classifier (DocumentCategorizerME. model)
          tokens (.tokenize tokenizer  text)
          outcomes (.categorize classifier tokens)
          best-category (.getBestCategory classifier outcomes)
          confidences (.scoreMap classifier tokens)]
      (with-meta
        {:value best-category :confidence (get confidences best-category)}
        {:confidences confidences}))))

(defrecord ClassificationTool [id model tokenizer classifier logger]
  core/Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool! {:id id :model (core/get-id model) :tokenizer (core/get-id tokenizer)})
    (reset! classifier (make-document-classifier (core/get-model model) (core/get-model tokenizer))))
  (apply-tool [this text _]
    (let [resp (@classifier text)]
      (log @logger :debug ::apply-tool {:id id :category resp :confidences (meta resp)})
      resp)))

(extend ClassificationTool
  core/Module
  (merge core/default-module-impl
         {:get-features (fn [{:keys [model tokenizer]}] (merge (core/get-features model)
                                                               (core/get-features tokenizer)
                                                               {:type :classification}))}))


(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id model tokenizer logger]} spec]
    (log logger :debug ::init {:id id})
    (let [classif (->ClassificationTool id model tokenizer (atom nil) (atom nil))]
      (core/set-logger! classif logger)
      (core/build-tool! classif)
      classif)))


(s/def ::value string?)
(s/def ::confidence double?)
(s/def ::confidences (s/map-of string? double?))
(s/def ::result (s/keys :req-un [::value ::confidence]))
(s/def ::meta (s/keys :req-un [::confidences]))

(deftest tool-classification-test
  (let [tool (t/get-test-module "test/config_tool_classification_1.edn" ukey)]
    (testing "get-features"
      (is (= {:language "ro"
              :type :classification}
             (core/get-features tool))))
    (testing "apply-tool"
      (let [res (core/apply-tool tool "Vreau sa fac un cadou" {})]
        (is (s/valid? ::result res))
        ;; (is (s/valid? ::meta (meta res)))
        (is (= "cadou" (:value res)))))))


(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - classify a text"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools" (name cmdkey) " -t TEXT -i MODEL_FILE"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [in text]} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "classif tool"
                             :tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :model (ig/ref :nlptools.model/classification)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model/classification {:id "classif model"
                                                       :binfile in
                                                       :loadbin? true
                                                       :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        classifier (get system ukey)
        text (get opts :text "")]
    (printf "text: %s,\ncategory: %s\n" text (core/apply-tool classifier text {}))
    (ig/halt! system)
    0))
