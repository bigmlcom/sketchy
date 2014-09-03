;; Copyright 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.sip
  (:require [clojure.test :refer :all]
            (bigml.sketchy [sip :as sip])))

(deftest hash-values
  (let [pairs [[0 4166820438245540263]
               [1E16 -4442821926291207691]
               [(long 1E16) 4652768757582477090]
               ["hash me!" 6237280638637203966]
               [{:foo 1 :bar 2} 5045669914251822320]]]
    (doseq [[val expected-hash] pairs]
      (is (= expected-hash (sip/hash val))))))

(deftest vary-bits
  (let [pairs [[63 2121124175213604009]
               [31 2082057385]
               [15 11433]
               [7 41]
               [3 1]]]
    (doseq [[bits expected-hash] pairs]
      (is (= expected-hash (sip/truncate (sip/hash "foo") bits))))))

(deftest seeds
  (is (= (sip/hash "foo" 0) -4874926522583603657))
  (is (= (sip/hash "foo" 123) -5272168615997740478))
  (is (= (take 3 (sip/hash-seq "foo"))
         '(-4874926522583603657 2540774949006662565 6327481778609814014))))
