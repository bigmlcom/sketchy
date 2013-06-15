;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.min-hash
  (:use clojure.test)
  (:require (bigml.sketchy [min-hash :as mh])))

(deftest min-hash-test
  (is (<= 0.88
          (mh/similarity (mh/into (mh/create) (range 0 5000))
                         (mh/into (mh/create) (range 500 5000)))
          0.92))
  (is (== 1 (mh/similarity (mh/merge (mh/into (mh/create) (range 1000))
                                     (mh/into (mh/create) (range 500 1500)))
                           (mh/into (mh/create) (range 1500))))))
