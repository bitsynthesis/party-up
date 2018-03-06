(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [adzerk/boot-test "1.1.1" :scope "test"]
                  [boot-codox "0.10.2" :scope "test"]
                  [incanter "1.5.7"]
                  [bitsynthesis/party-up "0.1"]
                  [quil "2.6.0"]])


(require '[adzerk.boot-test :refer [test]]
         '[codox.boot :as codox])


(def version "0.1")


(task-options!
 pom {:project 'bitsynthesis/party-up-ui
      :version version})


(deftask doc []
  (comp (codox/codox
         :name "Party Up UI"
         :description ""
         :metadata {:doc/format :markdown}
         :source-uri ""
         :version version)
        (target)))


(deftask build []
  (comp (aot) (pom) (jar) (install)))
