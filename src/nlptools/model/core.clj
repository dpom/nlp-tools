(ns nlptools.model.core
  "Model common protocol and specs"
  (:require
   [integrant.core :as ig]
   [clojure.spec.alpha :as s]))

(def corekey
  "model core key"
  :nlptools/model)

(def cmdkey
  "the command key for this unit"
  :corpus.entity)


(defprotocol Model
  (load-model! [this] "Load the model")
  (train-model! [this] "Train the model")
  (save-model! [this] "Save the model")
  (get-model [this] "Get the model")
  (set-logger! [this newlogger] "Set a new logger"))

(s/def :model/binfile string?)
(s/def :model/trainfile string?)
(s/def :model/language string?)
(s/def :model/loadbin? boolean?)
;; (s/def :model/logger #(instance? Logger %))
(s/def :model/logger map?)
(s/def :model/binconfig (s/keys :req-un [:model/binfile :model/logger]
                                :opt-un [:model/trainfile :model/loadbin? :model/language]))
(s/def :model/trainconfig (s/keys :req-un [:model/language :model/trainfile :model/loadbin? :model/logger]
                                  :opt-un [:model/binfile]))


(defmethod ig/pre-init-spec corekey [_]
  ;; (s/or :model/binconfig :model/trainconfig))
  (s/keys :req-un [:model/logger]
          :opt-un [:model/binfile :model/trainfile :model/loadbin? :model/language]))
