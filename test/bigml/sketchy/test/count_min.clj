;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.count-min
  (:use clojure.test)
  (:require (bigml.sketchy [count-min :as cm])))

(defn- make-data [size]
  (let [rnd (java.util.Random. 0)]
    (vec (concat
          (repeatedly (int (* 0.1 size))
                      #(long (* 100 (.nextGaussian rnd))))
          (repeatedly (int (* 0.9 size))
                      #(long (* 20000 (.nextGaussian rnd))))))))

(defn- test-sketch [sketch data val]
  {:estimate (cm/estimate-count sketch val)
   :actual (count (filter #(= val %) data))})

(deftest count-min-test
  (let [data (vec (make-data 200000))
        sketch (reduce cm/insert (cm/create) data)]
    (doseq [val (range 150)]
      (let [{:keys [estimate actual]} (test-sketch sketch data val)]
        (is (< (- estimate actual) 10))))))
