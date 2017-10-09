(ns nlptools.core
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [taoensso.timbre :as log]
   [environ.core :refer [env]]
   [clojure.test :refer :all]
   [nlptools.config :as cfg]
   [nlptools.corpus :as corpus]
   )
  (:gen-class))

(def default_config_filename ".nlptools.cfg")

(defn print-msg
  "Print informal messages on the console and in log.

  Args:
    options (map):  options map, used key :quiet
    msg (string): the message to display 

  Returns:
    nothing"
  [options msg]
  (log/info msg)
  (if-not (:quiet options)
    (println msg)))

(def cli-options
  [
   ["-c" "--config FILE" "Configuration file" :default default_config_filename]
   ["-o" "--outfilename FILE" "Output file name"]
   ["-q" "--quiet"]
   ["-h" "--help"]
   ])

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
    "create    Create a corpus"
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


(defn action
  "Handler for action.

   Args:
    options (map): action options 
    actionfn (function): the function which run the action
    okmsg (string): the message to display if the action finishes normally"
  [options actionfn okmsg]
  (let [[ret err] (actionfn options)]
    (if err
      (println (str  "exception: " err))
      (print-msg options okmsg))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options
    (let [act (partial action (cfg/set-config options default_config_filename))]
      (case (first arguments)
        "create" (act corpus/create-command "The corpus is created.")
        (exit 1 (usage summary))))))
