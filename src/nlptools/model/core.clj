(ns nlptools.model.core
  "Model common protocol and specs"
  (:require
   [integrant.core :as ig]
   [clojure.spec.alpha :as s]
   [nlptools.spec :as spec]))

(def corekey
  "model core key"
  :nlptools/model)


(defprotocol Model
  (load-model! [this] "Load the model")
  (train-model! [this] "Train the model")
  (save-model! [this] "Save the model")
  (get-model [this] "Get the model")
  (get-id [this] "Get the model id")
  (set-logger! [this newlogger] "Set a new logger"))

(s/def :model/binfile string?)
(s/def :model/trainfile string?)
(s/def :model/loadbin? boolean?)

(s/def :model/binconfig (s/keys :req-un [:model/binfile :nlptools/logger]
                                :opt-un [:model/trainfile :model/loadbin? :nlptools/language]))
(s/def :model/trainconfig (s/keys :req-un [:nlptools/language :model/trainfile :model/loadbin? :nlptools/logger]
                                  :opt-un [:model/binfile]))


(defmethod ig/pre-init-spec corekey [_]
  ;; (s/or :model/binconfig :model/trainconfig))
  (spec/known-keys :req-un [:nlptools/id :nlptools/logger]
                   :opt-un [:model/binfile
                            :model/trainfile
                            :model/loadbin?
                            :nlptools/language]))
