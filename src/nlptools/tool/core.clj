(ns nlptools.tool.core)

(defprotocol Tool
  (build-tool [this] "(Re)Build the tool")
  (set-logger [this newlogger] "Set a new logger"))
