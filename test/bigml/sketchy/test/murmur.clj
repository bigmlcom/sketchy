;; Copyright 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.murmur
  (:require [clojure.test :refer :all]
            (bigml.sketchy [murmur :as murmur])))

(deftest hash-values
  (let [pairs [[0 799242588501267692]
               [1E16 4977900633541538766]
               [(long 1E16) -4083462578434211217]
               ["hash me!" -3694557840885048153]
               [{:foo 1 :bar 2} -2811517481304713575]]]
    (doseq [[val expected-hash] pairs]
      (is (= expected-hash (murmur/hash val))))))

(deftest vary-bits
  (let [pairs [[63 6231696022289519434]
               [31 1825051466]
               [15 4938]
               [7 74]
               [3 2]]]
    (doseq [[bits expected-hash] pairs]
      (is (= expected-hash (murmur/truncate (murmur/hash "foo") bits))))))

(deftest seeds
  (is (= (murmur/hash "foo" 0) 6231696022289519434))
  (is (= (murmur/hash "foo" 123) 4010191379894525224))
  (is (= (take 3 (murmur/hash-seq "foo"))
         '(6231696022289519434 -1965669315023635442 -4826411765733908310))))
