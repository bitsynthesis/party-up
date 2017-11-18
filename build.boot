(set-env!
  :resource-paths #{"src" "test"}
  :dependencies '[[ola-clojure "0.1.7"]
                  [adzerk/boot-test "1.1.1" :scope "test"]
                  [clojurewerkz/buffy "1.1.0"]
                  [org.clojars.brunchboy/protobuf "0.8.3"]
                  [org.scream3r/jssc "2.8.0"]])


(require '[adzerk.boot-test :refer [test]])
