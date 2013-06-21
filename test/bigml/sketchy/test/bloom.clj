;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.bloom
  (:use clojure.test)
  (:require (bigml.sketchy [bloom :as bloom])))

(deftest bloom
  (let [d1 (range 10000)
        d2 (range 5000 15000)
        d3 (range 20000)
        b1 (reduce bloom/insert (bloom/create 15000 0.02) d1)
        b2 (reduce bloom/insert (bloom/create 15000 0.02) d2)
        true-count #(count (filter true? (map (partial bloom/contains? %1) %2)))]
    (is (= (true-count b1 d1) 10000)) ;; Never any false negatives
    (is (<= 10000 (true-count b1 d3) 10200))
    (is (<= 15000 (true-count (bloom/merge b1 b2) d3) 15300))))
