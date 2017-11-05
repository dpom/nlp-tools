(ns nlptools.core
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [environ.core :refer [env]]
   [clojure.test :refer :all]
   [nlptools.command :as cmd]
   )
  (:gen-class))




(def cli-options
  [
   ["-c" "--config FILE" "Configuration file" :default cmd/default-config-filename]
   ["-h" "--help"]
   ["-i" "--in FILE" "Input file name"]
   ["-l" "--language LANGUAGE" "Language" :default "ro"]
   ["-o" "--out FILE" "Output file name"]
   ["-q" "--quiet"]
   ["-t" "--text TEXT" "The text to be parsed"]
   ])

(def commands [:stemmer :stopwords :classification :model.classification :corpus.intent])

(defn print-msg
  "Print informal messages on the console.

  Args:
    options (map):  options map, used key :quiet
    msg (string): the message to display

  Returns:
    nothing"
  [options msg]
  (if-not (:quiet options)
    (println msg)))

(defn try-require [cmd]
  (let [sym (symbol (str "nlptools." (name cmd)))]
    (try (do (require sym) sym)
         (catch java.io.FileNotFoundException _))))

(defn commands-help
  []
  (str/join
   \newline
   (map (fn [k]
          (try-require k)
          (cmd/help k))
        commands)))

(defn syntax-help
  []
  (str/join
   \newline
   (map (fn [k]
          (cmd/syntax k))
        commands)))

(defn usage
  "Generate the usage text.

  Args:
    options_summary (string): cli generated options help

  Returns:
    (string): the usage text."
  [options_summary]
  (str/join
   \newline
   ["This is the nlp-tools program."
    ""
    "Usage: nlptools [options] action"
    ""
    "Options:"
    options_summary
    ""
    "Actions:"
    (commands-help)
    ""
    "Syntax examples:"
    (syntax-help)
    ""
    "Please refer to the user's guide for more information."]))

(defn error-msg
  "Generate the errors text.

   Args:
     errors (vector): cli generated errors string collection

   Returns:
      (string): the errors text."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn exit
  "Exit application after displaing a message.

   Args:
     status (int): the exit code
     msg (string): text to display/log"
  [status msg]
  (println msg)
  (System/exit status))


(defmethod cmd/run :help [_ _ summary]
  (exit 0 (usage summary)))

(defmethod cmd/run :default [_ _ summary]
  (exit 2 (usage summary)))

(defmethod cmd/run :errors [_ _ errors]
  (exit 1 (error-msg errors)))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (cmd/run :help options summary)
      (not= (count arguments) 1) (cmd/run :default options summary)
      errors (cmd/run :errors options errors)
      :else (let [k (keyword (first arguments))]
              (try-require k)
              (cmd/run k options summary)))))
