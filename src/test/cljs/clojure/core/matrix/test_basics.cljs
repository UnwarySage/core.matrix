(ns clojure.core.matrix.test-basics
  (:require
    [figwheel.client :as figwheel :include-macros true]
    [cljs.test :refer-macros [deftest is testing run-tests run-all-tests]]
    [clojure.string :as s]
    [clojure.core.matrix :as mat]
    [clojure.core.matrix.impl.wrappers :as wrap]
    [thi.ng.ndarray.core :as nd]
    [clojure.core.matrix.impl.double-array])
  (:require-macros
    [clojure.core.matrix.macros :refer [error?]]))

(enable-console-print!)

(declare init)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback init)

(deftest test-matrix-math
  (let [a (mat/array [1 2 3])
        b (mat/array [1 2 3])
        c (mat/array [[2 2] [2 2]])
        d (mat/array [[3 3] [3 3]])]
    (is (= [2 4 6] (vec (mat/add a b))))
    (is (= [1 4 9] (vec (mat/mul a b))))
    (is (= [[12 12] [12 12]]) (vec (map vec (mat/mmul c d))))
    (is (mat/array [[12 12] [12 12]]) (mat/mmul c d))))

(deftest test-regressions
  (testing "vector 3D transpose"
    (is (= [[[1]]] (mat/transpose [[[1]]]))))
  (testing "vector wrapper coerce"
    (is (= 1.0 (mat/coerce [] (wrap/wrap-scalar 1.0)))))
    ;(is (= [1.0 2.0] (mat/coerce [] (mat/slices (double-array [1 2])))))

  (testing "vector length"
    (is (== 5 (mat/length [3 4]))))
  (testing "scalar broadcast"
    (is (mat/e= [11 12 13] (mat/add [1 2 3] 10)))
    (is (mat/e= [11 12 13] (mat/add 10 [1 2 3]))))
  (testing "persistent vector shape"
    (is (= [2] (seq (mat/shape [1 2]))))
    (is (= [0] (seq (mat/shape [])))))
  (testing "empty vector"
    (is (mat/e= [] (mat/coerce [] [])))
    (is (mat/e= [] (mat/assign [] 1.0)))
    (is (empty? (mat/eseq [])))
    (is (nil? (mat/coerce [] nil))))
  (testing "broadcast on emap"
    (is (mat/equals [[6 7] [8 9]] (mat/emap + [[1 2] [3 4]] 5)))
    (is (mat/equals [[6 7] [8 9]] (mat/emap + 5 [[1 2] [3 4]])))))

(deftest test-assign
  (is (= [[1 2] [1 2]] (mat/assign [[1 2] [3 4]] [1 2]))))
  ;(is (error? (mat/assign [1 2] [[1 2] [3 4]])))


(deftest test-construction
  (is (mat/equals [[0 0] [0 0]] (mat/zero-array [] [2 2]))))

(deftest test-properties
  (is (mat/numerical? [2 43]))
  (is (not (mat/mutable? [1 2])))
  (is (not (mat/mutable? [[1 2] [3 4]])))
  (testing "trace"
    (is (== 5 (mat/trace [[1 2] [3 4]])))))

(deftest test-fill
  (is (mat/equals [[2 2] [2 2]] (mat/fill [[1 2] [3 4]] 2)))
  (is (mat/equals [[2 2] [2 2]] (mat/fill [[1 2] [3 4]] [2 2]))))

(deftest test-transpose
  (testing "vector transpose"
    (is (= [[1 3] [2 4]] (mat/transpose [[1 2] [3 4]])))
    (is (= [1 2 3] (mat/transpose [1 2 3])))
    (is (= [[[[1]]]] (mat/transpose [[[[1]]]])))))

(deftest test-broadcast
  (is (mat/equals [[1 2] [1 2]] (mat/broadcast [1 2] [2 2]))))

(deftest test-rows-columns
  (is (mat/equals [[1 2] [3 4]] (mat/rows [[1 2] [3 4]])))
  (is (mat/equals [[1 3] [2 4]] (mat/columns [[1 2] [3 4]]))))

(deftest test-submatrix
  (is (mat/equals [2 3] (mat/submatrix [1 2 3] 0 [1 2]))))

(deftest test-rotate
  (is (mat/equals [2 3 1] (mat/rotate [1 2 3] 0 1))))

(deftest test-order
  (is (mat/equals [1 3 3] (mat/order [1 2 3 4] [0 2 2])))
  (is (mat/equals [4] (mat/order [1 2 3 4] [3])))
  (is (mat/equals [[1 2] [4 5]] (mat/order [[1 2 3] [4 5 6]] 1 [0 1]))))
  ;(is (error? (mat/order [1 2] 1 [0])))
  ;(is (error? (mat/order [1 2] [2])))

(deftest test-dot
  (is (mat/equals [2 4 6] (mat/dot 2 [1 2 3])))
  (is (mat/equals [2 4 6] (mat/dot [1 2 3] 2)))
  (is (mat/equals 20 (mat/dot [1 2 3] [2 3 4])))
  (is (mat/equals [[1 2] [6 8]] (mat/dot [[1 0] [0 2]] [[1 2] [3 4]]))))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn ^:export init []
  (let [element (.getElementById js/document "app")
        test-results (with-out-str (run-all-tests))
        lines (remove #(zero? (count %)) (remove #(= "\n" %) (s/split-lines test-results)))
        [lines res-lines] (split-at (- (count lines) 2) lines)
        content (apply str (map (fn [l] (str "<p>" l "</p>\n")) lines))
        results (map (fn [l] (str "<h3>" l "</h3>\n")) res-lines)
        report (apply str content results)]
    (set-html! element report)
    (js/window.scrollTo 0 js/document.body.scrollHeight)))

;(init)
