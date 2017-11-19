(set-env!
  :resource-paths #{"src" "test"}
  :dependencies '[[ola-clojure "0.1.7"]
                  [adzerk/boot-test "1.1.1" :scope "test"]
                  [circleci/bond "0.3.0"]
                  [incanter "1.5.7"]
                  [org.clojure/math.numeric-tower "0.0.4"]
                  [org.scream3r/jssc "2.8.0"]])


(require '[adzerk.boot-test :refer [test]])
