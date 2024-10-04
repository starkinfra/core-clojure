(defproject com.starkinfra/starkcore "0.2.0"
  :description "Core functionalities for the StarkInfra, StarkBank and StarkSign Clojure SDKs"
  :url "https://github.com/starkinfra/core-clojure"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure "1.10.1"]
    [clj-http "3.12.3"]
    [org.clojure/data.json "2.4.0"]
    [com.starkbank.ellipticcurve/starkbank-ecdsa "1.0.2"]
    [clj-time "0.15.2"]
    [cheshire "5.10.0"]]
  :repl-options {:init-ns core-clojure.core}
)

