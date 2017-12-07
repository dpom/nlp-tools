(ns nlptools.model.core
  "Model common keys and specs"
  (:require
   [integrant.core :as ig]
   [clojure.spec.alpha :as s]
   [nlpcore.spec :as nsp]
   [nlptools.spec :as spec]))

(def corekey
  "model core key"
  :nlptools/model)




(s/def :model/binfile string?)
(s/def :model/trainfile string?)
(s/def :model/loadbin? boolean?)

(s/def :model/binconfig (nsp/known-keys :req-un [:model/binfile :nlpcore/logger]
                                :opt-un [:model/trainfile :model/loadbin? :nlpcore/language]))
(s/def :model/trainconfig (nsp/known-keys :req-un [:nlpcore/language :model/trainfile :model/loadbin? :nlpcore/logger]
                                  :opt-un [:model/binfile]))


(defmethod ig/pre-init-spec corekey [_]
  ;; (s/or :model/binconfig :model/trainconfig))
  (nsp/known-keys :req-un [:nlpcore/id :nlpcore/logger]
                   :opt-un [:model/binfile
                            :model/trainfile
                            :model/loadbin?
                            :nlpcore/language]))
