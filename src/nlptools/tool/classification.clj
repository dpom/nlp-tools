(ns nlptools.tool.classification
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [clojure.test :refer :all]
   [nlptools.tool.core :as tool]
   [nlptools.model.core :as modl]
   [nlptools.spec :as spec]
   [nlptools.command :as cmd])
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
  (spec/known-keys :req-un [:nlptools/id
                            :nlptools/model
                            :nlptools/tokenizer
                            :nlptools/logger]))


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
  tool/Tool
  (build-tool! [this]
    (log @logger :debug ::build-tool! {:id id :model (modl/get-id model) :tokenizer (modl/get-id tokenizer)})
    (reset! classifier (make-document-classifier (modl/get-model model) (modl/get-model tokenizer))))
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (get-id [this] id)
  (apply-tool [this text]
    (let [resp (@classifier text)]
      (log @logger :debug ::apply-tool {:id id :category resp :confidences (meta resp)})
      resp)))

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id model tokenizer logger]} spec]
    (log logger :debug ::init {:id id})
    (let [classif (->ClassificationTool id model tokenizer (atom nil) (atom nil))]
      (tool/set-logger! classif logger)
      (tool/build-tool! classif)
      classif)))


(s/def ::value string?)
(s/def ::confidence double?)
(s/def ::confidences (s/map-of string? double?))
(s/def ::result (s/keys :req-un [::value ::confidence]))
(s/def ::meta (s/keys :req-un [::confidences]))

;; (deftest apply-tool-test
;;   (let [config (merge (cmd/make-test-logger :debug)
;;                       {ukey {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
;;                              :model (ig/ref :nlptools.model/classification)
;;                              :logger (ig/ref :duct.logger/timbre)}
;;                        :nlptools.model.tokenizer/simple {:logger (ig/ref :duct.logger/timbre)}
;;                        :nlptools.model/classification {:binfile "test/ema.bin"
;;                                                        :loadbin? true
;;                                                        :logger (ig/ref :duct.logger/timbre)}})
;;         system (ig/init (cmd/prep-igconfig config))
;;         classifier (get system ukey)
;;         res (tool/apply-tool classifier "Vreau sa fac un cadou")]
;;     (is (s/valid? ::result res))
;;     (is (s/valid? ::meta (meta res)))
;;     (is (= "cadou" (:value res)))))



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
    (printf "text: %s,\ncategory: %s\n" text (tool/apply-tool classifier text))
    (ig/halt! system)
    0))
