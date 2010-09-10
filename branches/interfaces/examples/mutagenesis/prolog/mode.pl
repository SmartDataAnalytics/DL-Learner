:- modeh(1,active(+drug))?

:- modeb(1,lumo(+drug,-energy))?
:- modeb(1,logp(+drug,-hydrophob))?

:- modeb(*,bond(+drug,-atomid,-atomid,#int))?
:- modeb(*,bond(+drug,+atomid,-atomid,#int))?
:- modeb(*,atm(+drug,-atomid,#element,#int,-charge))?

:- modeb(1,gteq(+charge,#float))?
:- modeb(1,gteq(+energy,#float))?
:- modeb(1,gteq(+hydrophob,#float))?
:- modeb(1,lteq(+charge,#float))?
:- modeb(1,lteq(+energy,#float))?
:- modeb(1,lteq(+hydrophob,#float))?

:- modeb(1,(+charge)=(#charge))?
:- modeb(1,(+energy)=(#energy))?
:- modeb(1,(+hydrophob)=(#hydrophob))?

:- modeb(*,benzene(+drug,-ring))?
:- modeb(*,carbon_5_aromatic_ring(+drug,-ring))?
:- modeb(*,carbon_6_ring(+drug,-ring))?
:- modeb(*,hetero_aromatic_6_ring(+drug,-ring))?
:- modeb(*,hetero_aromatic_5_ring(+drug,-ring))?
:- modeb(*,ring_size_6(+drug,-ring))?
:- modeb(*,ring_size_5(+drug,-ring))?
:- modeb(*,nitro(+drug,-ring))?
:- modeb(*,methyl(+drug,-ring))?
:- modeb(*,anthracene(+drug,-ringlist))?
:- modeb(*,phenanthrene(+drug,-ringlist))?
:- modeb(*,ball3(+drug,-ringlist))?

:- modeb(*,member(-ring,+ringlist))?
:- modeb(1,member(+ring,+ringlist))?
:- modeb(1,connected(+ring,+ring))?


:- determination(active/1,atm/5)?
:- determination(active/1,bond/4)?
:- determination(active/1,gteq/2)?
:- determination(active/1,lteq/2)?
:- determination(active/1,'='/2)?

:- determination(active/1,lumo/2)?
:- determination(active/1,logp/2)?

:- determination(active/1,benzene/2)?
:- determination(active/1,carbon_5_aromatic_ring/2)?
:- determination(active/1,carbon_6_ring/2)?
:- determination(active/1,hetero_aromatic_6_ring/2)?
:- determination(active/1,hetero_aromatic_5_ring/2)?
:- determination(active/1,ring_size_6/2)?
:- determination(active/1,ring_size_5/2)?
:- determination(active/1,nitro/2)?
:- determination(active/1,methyl/2)?
:- determination(active/1,anthracene/2)?
:- determination(active/1,phenanthrene/2)?
:- determination(active/1,ball3/2)?
:- determination(active/1,member/2)?
:- determination(active/1,connected/2)?




% type information

drug(D):-
	name(D,[_|X]), name(Num,X), int(Num),
	Num >= 1, Num =< 230, !.

atomid(A):-
	name(A,[_|X]),
	append(Z,[95|Y],X),
	name(N1,Y),
	name(N2,Z),
	int(N1), int(N2),
	N2 >= 1, N2 =< 230,
	N1 =< 500, !.

append([],A,A).
append([H|T],A,[H|T1]):-
        append(T,A,T1).



charge(X):-
        float(X).
energy(X):-
        float(X).
hydrophob(X):-
        float(X).

% ring([_|_]).

ring(X):-
	name(X,[114|_]).

ringlist(X):- 
	name(X,[114|_]).

% ringlist([]).
% ringlist([Ring|Rings]):-
        % ring(Ring),
        % ringlist(Ringlist).



element(br).
element(c).
element(cl).
element(f).
element(h).
element(i).
element(n).
element(o).
element(s).


% background knowledge 

gteq(X,Y):-
	not(var(X)), not(var(Y)),
	float(X), float(Y), 
	X >= Y, !.
gteq(X,X):-
	not(var(X)),
	float(X).

lteq(X,Y):-
	not(var(X)), not(var(Y)),
	float(X), float(Y),
	X =< Y, !.
lteq(X,X):-
	not(var(X)), 
	float(X).


:- set(i,2)?
:- set(nodes,20000)?
:- set(noise,5)?
:- set(c,3)?
:- set(verbose,0)?
:- noreductive?
:- nosplit?
