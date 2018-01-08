(ns example.matrix
  (:require [clojure.core.matrix :as matrix]))


;; (def default-matrix (matrix/matrix :ndarray [[[0 0 0] [0 0 0] [0 0 0] [0 0 0]]
;;                                              [[0 0 0] [0 0 0] [0 0 0] [0 0 0]]
;;                                              [[0 0 0] [0 0 0] [0 0 0] [0 0 0]]
;;                                              [[0 0 0] [0 0 0] [0 0 0] [0 0 0]]]))
;;
;;
;; (def default-matrix (matrix/new-matrix 4 4))
;;
;;
;; (def rgb-matrix (matrix/matrix :ndarray [(matrix/new-matrix 4 4)
;;                                          (matrix/new-matrix 4 4)
;;                                          (matrix/new-matrix 4 4)]))
;;
;;
;;
;; (matrix/pm default-matrix)
;; (matrix/pm rgb-matrix)
;;
;;
;; ;; (matrix/assign! (matrix/submatrix default-matrix [[1 2] [1 2] [0 3]])
;; ;;                 (matrix/matrix [[[255 0 0] [255 0 0]] [[255 0 0] [255 0 0]]]))
;;
;;
;; (matrix/assign! (matrix/submatrix rgb-matrix [[0 1] [1 2] [1 2]])
;;                 (matrix/matrix [[255 255] [255 255]]))
;;
;;
;; (matrix/shape default-matrix)
;; (matrix/ecount default-matrix)
;; (matrix/esum default-matrix)
