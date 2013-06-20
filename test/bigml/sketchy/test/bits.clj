;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.bits
  (:use clojure.test)
  (:require (bigml.sketchy [bits :as bits])))

(deftest bits-test
  (let [[s1 s2] (partition 128 (shuffle (range 256)))
        bs1 (apply bits/set (bits/create 256) s1)
        bs2 (apply bits/set (bits/create 256) s2)]
    (is (= (sort s1) (bits/set-seq bs1)))
    (is (= (sort s2) (bits/clear-seq bs1)))
    (is (empty? (bits/set-seq (apply bits/flip bs1 s1))))
    (is (empty? (bits/set-seq (bits/and bs1 bs2))))
    (is (empty? (bits/clear-seq (bits/or bs1 bs2))))))
