(defproject bigml/sketchy "0.2.0"
  :description "Sketching algorithms in Clojure"
  :url "https://github.com/bigmlcom/sketchy"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :aliases {"lint" ["do" "check," "eastwood"]
            "distcheck" ["do" "clean," "lint," "test"]}
  :profiles {:dev {:plugins [[jonase/eastwood "0.1.4"]]}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.google.guava/guava "14.0.1"]
                 [bigml/sampling "2.1.1"]
                 [commons-lang "2.6"]])
