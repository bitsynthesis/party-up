(set-env!
  :resource-paths #{"src" "test"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [adzerk/boot-test "1.1.1" :scope "test"]
                  [boot-codox "0.10.2" :scope "test"]
                  [clj-time "0.14.2"]
                  [org.clojure/core.async "0.3.465"]
                  [org.clojure/math.numeric-tower "0.0.4"]
                  [org.scream3r/jssc "2.8.0"]])


(require '[adzerk.boot-test :refer [test]]
         '[codox.boot :as codox])


(def version "0.1")


(task-options!
 pom {:project 'bitsynthesis/party-up
      :version version})


(deftask doc []
  (comp (codox/codox
         :name "Party Up"
         :description ""
         :metadata {:doc/format :markdown}
         :source-uri ""
         :version version)
        (target)))


(deftask build []
  (comp (aot) (pom) (jar) (install)))
