(ns nlptools.intent-corpus
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   ))

(defrecord Boundary [filepath db])

(defmethod ig/init-key :nlptools/intent-corpus [_ spec]
  (let [{:keys [db filepath logger]} spec
        resultset (.query db "nlp" {:is_valid true} ["text" "entities"])]
    (log logger :info ::creating-intent-corpus)
    (with-open [w (io/writer filepath)]
      (reduce  (fn [total {:keys [text entities]}]
                 (let [intent (get entities :intent "necunoscut")]
                   (.write w (format "%s %s" intent text))
                   (.newLine w)
                   (inc total)))
               0 resultset))
    (->Boundary filepath db)))
