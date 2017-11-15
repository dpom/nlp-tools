(ns nlptools.tool.stemmer
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [stemmer.snowball :as snowball]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :as tool]
   [nlptools.command :as cmd]))

(def languages-codes {"ro" :romanian
                      "en" :english})

(def ukey
  "this unit key"
  :nlptools.tool/stemmer)

(def cmdkey
  "the command key for this unit"
  :tool.stemmer)

(derive ukey tool/corekey)


(defrecord StemmerTool [id stemmer lang logger]
  tool/Tool
  (build-tool! [this]
    (log @logger :info ::build-tool {:id id :lang lang})
    (reset! stemmer (snowball/stemmer (get languages-codes lang :romanian)))
    this)
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (apply-tool [this text]
    (log @logger :debug ::apply-tool {:id id :text text})
    (@stemmer text)))

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id language logger]} spec]
    (log logger :debug ::init)
    (let [stemmer (->StemmerTool id (atom nil) language (atom nil))]
      (tool/set-logger! stemmer logger)
      (tool/build-tool! stemmer)
      stemmer)))

(s/def ::result string?)

(deftest apply-tool-test
  (let [config (merge (cmd/make-test-logger :error)
                      {ukey {:id "tets stemmer"
                             :language "ro"
                             :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stemmer (get system ukey)
        res (tool/apply-tool stemmer "copiilor")]
    (is (s/valid? ::result res))
    (is (= "cop" res))))


(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - reduce inflected (or sometimes derived) words to their word stem "))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -t TEXT"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        {:keys [language text] :or {text ""}} opts
        config (merge (cmd/make-logger opts)
                      {ukey {:id "run stemmer"
                             :language language
                             :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stemmer (get system ukey)]
    (printf "word: %s,\nstem: %s\n" text (tool/apply-tool stemmer text))
    (ig/halt! system)
    0))
