;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.murmur
  (:require [clojure.test :refer :all]
            (bigml.sketchy [murmur :as murmur])))

(deftest hash-as-int
  (let [pairs [[0 1669671676]
               [1E16 -972377044]
               [(long 1E16) 2022854314]
               ["hash me!" -326467501]
               [{:foo 1 :bar 2} -1669603877]]]
    (doseq [[val expected-hash] pairs]
      (is (= expected-hash
             (murmur/int-hash val)
             (murmur/hash val)
             (murmur/hash val :type :int))))))

(deftest hash-as-long
  (let [pairs [[0 2945182322382062539]
               [1E16 5478387224804409920]
               [(long 1E16) -4217971671870434059]
               ["hash me!" 6024472191590094728]
               [{:foo 1 :bar 2} -8885066329871693690]]]
    (doseq [[val expected-hash] pairs]
      (is (= expected-hash
             (murmur/long-hash val)
             (murmur/hash val :type :long))))))

(deftest hash-as-bigint
  (let [pairs [[0 -69434805100249084986062662884695124494N]
               [1E16 85415914660744809423781278367912174890N]
               [(long 1E16) -13850437204032123720209553473715423685N]
               ["hash me!" -159050720489313092263579472871045522759N]
               [{:foo 1 :bar 2} -161126250544495391138811671157663193495N]]]
    (doseq [[val expected-hash] pairs]
      (is (= expected-hash
             (murmur/hash val :type :bigint)
             (murmur/bigint-hash val)
             (bigint (murmur/bytes-hash val))
             (bigint (murmur/hash val :type :bytes)))))))

(deftest vary-bits
  (let [pairs [[127 58959437246456010946025114970552624115N]
               [63 5208370748186188588]
               [31 2085578581]
               [15 26453]
               [7 85]
               [3 5]]]
    (doseq [[bits expected-hash] pairs]
      (is (= expected-hash (murmur/hash "foo" :bits bits))))))

(deftest seeds
  (is (= 2085578581 (murmur/hash "foo" :seed 0)))
  (is (= -318501550 (murmur/hash "foo" :seed 123)))
  (is (= (take 5 (murmur/hash-seq "foo" :seed 0))
         '(-686467394 -1249983478 2108059474 208250426 -863809458)))
  (is (= (take 5 (murmur/hash-seq "foo" :seed 123))
         '(-1450917960 -1545370347 -1315306456 -950992082 -1549759388))))
