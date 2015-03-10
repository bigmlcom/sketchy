(defproject bigml/sketchy "0.3.0"
  :description "Sketching algorithms in Clojure"
  :url "https://github.com/bigmlcom/sketchy"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :aliases {"lint" ["do" "check," "eastwood"]
            "distcheck" ["do" "clean," "lint," "test"]}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :jvm-opts ^:replace ["-server"]
  :profiles {:dev {:plugins [[jonase/eastwood "0.1.4"]]}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [byte-transforms "0.1.3"]])
