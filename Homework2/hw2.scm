#lang scheme
; ---------------------------------------------
; DO NOT REMOVE OR CHANGE ANYTHING UNTIL LINE 26
; ---------------------------------------------

; zipcodes.scm contains all the US zipcodes.
; This file must be in the same folder as hw2.scm file.
; You should not modify this file. Your code
; should work for other instances of this file.
(require "zipcodes.scm")

; Helper function
(define (mydisplay value)
	(display value)
	(newline)
)

; Helper function
(define (line func)
        (display "--------- ")
        (display func)
        (display " ------------")
        (newline)
)

; ================ Solve the following functions ===================
; Return a list with only the negatives items
(define (negatives lst)
	(filter (lambda (x) (< x 0)) lst)
)

(line "negatives")
(mydisplay (negatives '()))  ; -> ()
(mydisplay (negatives '(-1)))  ; -> (-1)
(mydisplay (negatives '(-1 1 2 3 4 -4 5)))  ; -> (-1 -4)
(mydisplay (negatives '(1 1 2 3 4 4 5)))  ; -> ()

; ---------------------------------------------

; Returns true if the two lists have identical structure
; in terms of how many elements and nested lists they have in the same order
(define (struct lst1 lst2)
; Recursively compares the structure of two lists
  (cond ((and (null? lst1) (null? lst2)) #t)
        ((or (null? lst1) (null? lst2)) #f)
        ((and (list? (car lst1)) (list? (car lst2)))
         (and (struct (car lst1) (car lst2)) (struct (cdr lst1) (cdr lst2))))
        ((or (list? (car lst1)) (list? (car lst2))) #f)
        (else (struct (cdr lst1) (cdr lst2)))))

(line "struct")
(mydisplay (struct '(a b c (c a b)) '(1 2 3 (a b c))))  ; -> #t
(mydisplay (struct '(a b c d (c a b)) '(1 2 3 (a b c))))  ; -> #f
(mydisplay (struct '(a b c (c a b)) '(1 2 3 (a b c) 0)))  ; -> #f

; ---------------------------------------------

; Returns a list of two numeric values. The first is the smallest
; in the list and the second is the largest in the list. 
; lst -- contains numeric values, and length is >= 1.
(define (minAndMax lst)
  (list (apply min lst) (apply max lst))
)

(line "minAndMax")
(mydisplay (minAndMax '(1 2 -3 4 2)))  ; -> (-3 4)
(mydisplay (minAndMax '(1)))  ; -> (1 1)

; ---------------------------------------------

; Returns a list identical to the first list, while having all elements
; that are inside nested loops taken out. So we want to flatten all elements and have
; them all in a single list. For example '(a (a a) a))) should become (a a a a)
(define (flatten lst)
; Recursively concatenates all elements of a nested list into a flat list
  (if (null? lst)
      '()
      (if (list? (car lst))
          (append (flatten (car lst)) (flatten (cdr lst)))
          (cons (car lst) (flatten (cdr lst))))))

(line "flatten")
(mydisplay (flatten '(a b c)))  ; -> (a b c)
(mydisplay (flatten '(a (a a) a)))  ; -> (a a a a)
(mydisplay (flatten '((a b) (c (d) e) f)))  ; -> (a b c d e f)
; ---------------------------------------------

; The paramters are two lists. The result should contain the cross product
; between the two lists: 
; The inputs '(1 2) and '(a b c) should return a single list:
; ((1 a) (1 b) (1 c) (2 a) (2 b) (2 c))
; lst1 & lst2 -- two flat lists.
(define (crossproduct lst1 lst2)
; Constructs the cross product of two lists by pairing each element of the first list with every element of the second
  (if (null? lst1)
      '() ; If the first list is empty, return an empty list
      (append 
       (map (lambda (item2) (list (car lst1) item2)) lst2) ; Pair the first element of lst1 with each element of lst2
       (crossproduct (cdr lst1) lst2)))) ; Recur on the rest of lst1

(line "crossproduct")
(mydisplay (crossproduct '(1 2) '(a b c)))

; ---------------------------------------------

; Returns the first latitude and longitude of a particular zip code.
; if there are multiple latitude and longitude pairs for the same zip code,
; the function should only return the first pair. e.g. (53.3628 -167.5107)
; zipcode -- 5 digit integer
; zips -- the zipcode DB- You MUST pass the 'zipcodes' function
; from the 'zipcodes.scm' file for this. You can just call 'zipcodes' directly
; as shown in the sample example
(define (getLatLon zipcode zips)
; Finds the first match for a zipcode and extracts the latitude and longitude
  (let ((found (assoc zipcode zips))) ; Use assoc to find the first match by zipcode
    (if found
        (list (cadr (cdr (cdr (cdr found)))) ; Latitude
              (cadr (cdr (cdr (cdr (cdr found)))))) ; Longitude
        '()))) ; Return an empty list if not found


(line "getLatLon")
(mydisplay (getLatLon 45056 zipcodes))
(line "getLatLon")
; ---------------------------------------------

; Returns a list of all the place names common to two states.
; placeName -- is the text corresponding to the name of the place
; zips -- the zipcode DB
(define (getCommonPlaces state1 state2 zips)
; Finds place names that are common between two states
  (let ((places1 (map cadr (filter (lambda (x) (string=? (caddr x) state1)) zips)))
        (places2 (map cadr (filter (lambda (x) (string=? (caddr x) state2)) zips))))
    (filter (lambda (place) (member place places2 equal?)) places1)))


(line "getCommonPlaces")
(mydisplay (getCommonPlaces "OH" "MI" zipcodes))

; ---------------------------------------------

; Returns the number of zipcode entries for a particular state.
; state -- state
; zips -- zipcode DB
(define (zipCount state zips)
; Counts the number of zip code entries for a given state
  (define (state-match? zip-record)
    (string=? state (caddr zip-record))) ; caddr retrieves the third element, which is the state
  (length (filter state-match? zips)))

(line "zipCount")
(mydisplay (zipCount "OH" zipcodes))
; ---------------------------------------------
; Some sample predicates
(define (POS? x) (> x 0))
(define (NEG? x) (< x 0))
(define (LARGE? x) (>= (abs x) 10))
(define (SMALL? x) (not (LARGE? x)))

; Returns a list of items that satisfy a set of predicates.
; For example (filterList '(1 2 3 4 100) '(EVEN?)) should return the even numbers (2 4 100)
; (filterList '(1 2 3 4 100) '(EVEN? SMALL?)) should return (2 4)
; lst -- flat list of items
; filters -- list of predicates to apply to the individual elements

(define (filterList lst filters)
; Filters a list of items based on a list of predicates
  (define (apply-filters item filters)
  ; Recursively applies filters to an item
    (cond ((null? filters) #t) ; If no more filters, item passes
          ((not ((car filters) item)) #f) ; Current filter fails
          (else (apply-filters item (cdr filters))))) ; Continue with next filter
  (define (filter-recursive lst)
  ; Recursively applies filters to a list
    (cond ((null? lst) '()) ; End of list
          ((apply-filters (car lst) filters) ; Item passes all filters
           (cons (car lst) (filter-recursive (cdr lst))))
          (else (filter-recursive (cdr lst))))) ; Skip item
  (filter-recursive lst))


(line "filterList")
(mydisplay (filterList '(1 2 3 11 22 33 -1 -2 -3 -11 -22 -33) (list POS?)))
(mydisplay (filterList '(1 2 3 11 22 33 -1 -2 -3 -11 -22 -33) (list POS? even?)))
(mydisplay (filterList '(1 2 3 11 22 33 -1 -2 -3 -11 -22 -33) (list POS? even? LARGE?)))


