(ns nlptools.model.core)

(defprotocol Model
  (load-model [this] "Load the model")
  (train-model [this] "Train the model")
  (save-model [this] "Save the model")
  (get-model [this] "Get the model")
  (set-logger [this newlogger] "Set a new logger"))
