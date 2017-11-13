(ns nlptools.spec
  (:require
   [clojure.spec.alpha :as s]))

(defmacro known-keys
  [& {:keys [req req-un opt opt-un gen] :as args}]
  (letfn [(known-spec? [k] (boolean (s/get-spec k)))]
    (doseq [e (concat req req-un opt opt-un)]
      (when-not (known-spec? e)
        (throw (ex-info (str e " is not a currently registered spec.") args)))))
  `(s/keys ~@(interleave (keys args) (vals args))))



(s/def :nlptools/logger map?)
(s/def :nlptools/language string?)
(s/def :nlptools/model map?)
(s/def :nlptools/tokenizer map?)
