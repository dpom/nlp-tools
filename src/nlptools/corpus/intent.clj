(ns nlptools.corpus.intent
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [duct.logger :refer [log]]
   ))

(defprotocol CorpusIntent
  (init [this logger]))

(defrecord Boundary [filepath db logger]
  CorpusIntent
  (init [this newlogger]
    (reset! logger newlogger)
    (log @logger :info ::creating-corpus-intent {:file filepath})
    (let [resultset (.query db "nlp" {:is_valid true} ["text" "entities"])]
      (with-open [w (io/writer filepath)]
        (let [total (reduce  (fn [counter {:keys [text entities]}]
                   (let [intent (get entities :intent "necunoscut")]
                     ;; (log @logger :debug ::write-line {:counter counter :intent intent :text text})
                     (.write w (format "%s %s" intent text))
                     (.newLine w)
                     (inc counter)))
                             0 resultset)]
          (log @logger :info ::corpus-intent-created {:total total :file filepath})
          )))
    this))

(defmethod ig/init-key :nlptools.corpus/intent [_ spec]
  (let [{:keys [db filepath logger]} spec]
    (.init (->Boundary filepath db (atom nil)) logger)))
