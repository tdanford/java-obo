
;; (define foo (make-namespace "foo" "http://foo.com/bar/"))
;; (foo) => ("foo:" "http://foo.com/bar/")
;; (foo "blah") => "http://foo.com/bar/blah"
;; (foo "foo:blah") => "http://foo.com/bar/blah"
;; (foo 3) => 3
(define (make-namespace prefix uri)
  (let ((colonized (string-append prefix ":")))
    (lambda . args
	    (cond ((= 0 (length args)) (list colonized uri))
		  ((string? (car args)) 
		   (if (prefix? colonized (car args))
		       (string-append uri (substring (length colonized) (length (car args)) (car args)))
		       (string-append uri (car args))))
		  (else args)))))

(define rdf: (make-namespace "rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
(define rdfs: (make-namespace "rdfs" "http://www.w3.org/2000/01/rdf-schema#"))
(define owl: (make-namespace "owl" "http://www.w3.org/2002/07/owl#"))

(define (make-expander . nslist) 
  (lambda (str) 
    (let ((applicable (filter (lambda (ns) (prefix? (ns) str)) nslist)))
      (if (null? applicable) 
	  str
	  ((car applicable) str)))))

(define uri+ (make-expander rdf: rdfs: owl:))

;; utility class for generating unique strings.
(define gensym
  (let ((c 0))
    (lambda (prefix) 
      (lambda () 
	(let ((sym (string-append prefix (int->string c))))
	  (set! c (+ c 1))
	  sym)))))

(define blank (gensym "_:"))
