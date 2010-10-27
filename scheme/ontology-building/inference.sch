
;; these don't handle blank nodes well, at the moment.

(define (relation-matcher rel) 
  (define (place-match pattern value)
    (if (null? pattern) #t (equal? pattern value)))
  (lambda (kb-rel) 
    (and (= (length rel) (length kb-rel))
	 (apply and (map place-match rel kb-rel)))))

(define (kb-has-relation? kb rel)
  (let ((matcher (relation-matcher rel)))
    (cond ((= 0 (length kb)) #f) 
	  ((matcher (car kb)) #t)
	  (else (kb-has-relation? (cdr kb) rel)))))

(define (assert proptree) 
  (cond ((prop? proptree) (lambda (kb) 
			    (kb-has-relation? kb proptree)))
	((> (length proptree) 0) (lambda (kb) 
				   (and ((assert (car proptree)) kb)
					((apply assert (cdr proptree)) kb))))
	(else (lambda (kb) #t))))




