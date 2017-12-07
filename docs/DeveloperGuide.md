---

---

Architecture
============

The nlptools development is based on:

-   Integrant component (see [integrant](https://github.com/weavejester/integrant))
-   nlpcore protocols (see [API](https://dpom.github.io/nlpcore/api/nlpcore.protocols.html))

core
----

Implement the CLI.

The actual actions keywords are stored in `commands` map.

Each action should implement 3 multimethod (defined in `command.clj`):

run  
execute the action

help  
return the help string

syntax  
return the syntax string

model folder
------------

Contains models implementations.

The `core` unit contains the specific spec keys.

corpus folder
-------------

Contains corpus builder implementations.

tool folder
-----------

Contains end user tools implementations.

module
------

Contains integrant modules used by the other units (usually `corpus` units).
