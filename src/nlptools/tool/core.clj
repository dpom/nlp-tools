(ns nlptools.tool.core)

(defprotocol Tool
  (build-tool! [this] "(Re)Build the tool")
  (apply-tool [this text] "Apply the tool to a text")
  (set-logger! [this newlogger] "Set a new logger"))
