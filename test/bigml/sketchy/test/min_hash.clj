;; Copyright 2013, 2014, 2015, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.min-hash
  (:require [clojure.test :refer :all]
            (bigml.sketchy [min-hash :as mh])))

(deftest similarity-test
  (is (<= 0.85
          (mh/jaccard-similarity (mh/into (mh/create) (range 0 5000))
                                 (mh/into (mh/create) (range 500 5000)))
          0.95))
  (is (== 1 (mh/jaccard-similarity (mh/merge (mh/into (mh/create)
                                                      (range 1000))
                                             (mh/into (mh/create)
                                                      (range 500 1500)))
                                   (mh/into (mh/create) (range 1500))))))

(deftest speed-test
  (let [start (System/currentTimeMillis)
        similarity
        (mh/jaccard-similarity (mh/into (mh/create) (range 0.0 1E6))
                               (mh/into (mh/create) (range 1E5 1E6)))
        end (System/currentTimeMillis)]
    (is (< (- end start) 1000))
    (is (< 0.85 similarity 0.95))))
