(ns core-clojure.core
  "Core functionalities for the StarkInfra, StarkBank and StarkSign Clojure SDKs"
  (:refer-clojure :exclude [get set update])
  (:require [core-clojure.utils.rest]
            [core-clojure.key]
            [core-clojure.user.project]
            [core-clojure.user.organization]))
