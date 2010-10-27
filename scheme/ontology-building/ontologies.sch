(load "uris.sch")

(define rdfs:Class (rdfs: "Class"))
(define rdf:type (rdf: "type"))
(define rdfs:label (rdfs: "label"))
(define owl:subClassOf (owl: "subClassOf"))

;; creates an individual
(define (one name) (list 'one name))

;; properties are pairwise functions that assert a relation between the arguments.
(define (prop name) 
  (lambda (src target) 
    (list 'prop name src target)))

(define (prop? prop) 
  (and (list? prop) 
       (= 4 (length prop))
       (equal? 'prop (car prop))))


(define (chain . props) 
  (if (= 0 (length props))
      '()
      (lambda (s t) 
	(let ((x (blank)))
	  (append ((car props) s x) 
		  ((apply chain (cdr props)) x t))))))

(define (inv prop) 
  (lambda (s t) (prop t s)))

;; creates a class
(define (class name) (list 'class name))

(define (in i c) (rdf:type i c))
 
(define (isa c1 c2) (owl:subClassOf c1 c2))

(define (equivalent c1 c2) 
  (list (isa c1 c2)
	(isa c2 c1)))

(define (class-expr? cls) (and (list? cls) 
			       (> (length cls) 0)
			       (class-constructor? (car cls))))

(define (individual-expr? idv) 
  (and (list? idv) 
       (= (length idv) 2)
       (equal? 'one (car idv))))

(define (class-constructor? symb) 
  (or (equal? symb 'class)
      (equal? symb 'some)
      (equal? symb 'only)
      (equal? symb 'complement) 
      (equal? symb 'intersection)
      (equal? symb 'union)
      (equal? symb 'oneof)
      (equal? symb 'exactly)))

(define (some prop cls) (list 'some prop cls))



(define (only prop cls) (list 'only prop cls))
(define (complement cls) (list 'complement cls))
(define (exactly N prop cls) (list 'exactly N prop cls))
(define (intersection . cls) (cons 'intersection (apply list cls)))
(define (union . cls) (cons 'union (apply list cls)))
(define (oneof . idvs) (cons 'oneof (apply list idvs)))


