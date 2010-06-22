/* Prolog theory for nitro aromatic and heteroaromatic compounds
It expects the Quintus libraries. Quanta bond type 7 is aromatic. */

:- [library(basics)].
:- [library(sets)].

%****************************************************************************

% Three benzene rings connected linearly
anthracene(Drug,[Ring1,Ring2,Ring3]) :-
   benzene(Drug,Ring1),
   benzene(Drug,Ring2),
   Ring1 @> Ring2,
   interjoin(Ring1,Ring2,Join1),
   benzene(Drug,Ring3),
   Ring1 @> Ring3,
   Ring2 @> Ring3,
   interjoin(Ring2,Ring3,Join2),
   \+ interjoin(Join1,Join2,_),
   \+ members_bonded(Drug,Join1,Join2).

% Three benzene rings connected in a curve
phenanthrene(Drug,[Ring1,Ring2,Ring3]) :-
   benzene(Drug,Ring1),
   benzene(Drug,Ring2),
   Ring1 @> Ring2,
   interjoin(Ring1,Ring2,Join1),
   benzene(Drug,Ring3),
   Ring1 @> Ring3,
   Ring2 @> Ring3,
   interjoin(Ring2,Ring3,Join2),
   \+ interjoin(Join1,Join2,_),
   members_bonded(Drug,Join1,Join2).


% Three benzene rings connected in a ball
ball3(Drug,[Ring1,Ring2,Ring3]) :-
   benzene(Drug,Ring1),
   benzene(Drug,Ring2),
   Ring1 @> Ring2,
   interjoin(Ring1,Ring2,Join1),
   benzene(Drug,Ring3),
   Ring1 @> Ring3,
   Ring2 @> Ring3,
   interjoin(Ring2,Ring3,Join2),
   interjoin(Join1,Join2,_).

members_bonded(Drug,Join1,Join2) :-
   member(J1,Join1),
   member(J2,Join2),
   bondd(Drug,J1,J2,7).
   


%****************************************************************************

no_of_benzenes(Drug,No) :-
   setof(Ring,benzene(Drug,Ring),List),
   length(List,No).

no_of_carbon_5_aromatic_rings(Drug,No) :-
   setof(Ring,carbon_5_aromatic_ring(Drug,Ring),List),
   length(List,No).

no_of_carbon_6_rings(Drug,No) :-
   setof(Ring,carbon_6_ring(Drug,Ring),List),
   length(List,No).

no_of_carbon_5_rings(Drug,No) :-
   setof(Ring,carbon_5_ring(Drug,Ring),List),
   length(List,No).

no_of_hetero_aromatic_6_rings(Drug,No) :-
   setof(Ring,hetero_aromatic_6_ring(Drug,Ring),List),
   length(List,No).

no_of_hetero_aromatic_5_rings(Drug,No) :-
   setof(Ring,hetero_aromatic_5_ring(Drug,Ring),List),
   length(List,No).


%****************************************************************************

ring_size_6(Drug,Ring_list) :-
   atoms(Drug,6,Atom_list,_),
   ring6(Drug,Atom_list,Ring_list,_).

ring_size_5(Drug,Ring_list) :-
   atoms(Drug,5,Atom_list,_),
   ring5(Drug,Atom_list,Ring_list,_).

%****************************************************************************

% benzene - 6 membered carbon aromatic ring
benzene(Drug,Ring_list) :-
   atoms(Drug,6,Atom_list,[c,c,c,c,c,c]),
   ring6(Drug,Atom_list,Ring_list,[7,7,7,7,7,7]).

carbon_5_aromatic_ring(Drug,Ring_list) :-
   atoms(Drug,5,Atom_list,[c,c,c,c,c]),
   ring5(Drug,Atom_list,Ring_list,[7,7,7,7,7]).

%****************************************************************************

carbon_6_ring(Drug,Ring_list) :-
   atoms(Drug,6,Atom_list,[c,c,c,c,c,c]),
   ring6(Drug,Atom_list,Ring_list,Bond_list),
   Bond_list \== [7,7,7,7,7,7].

carbon_5_ring(Drug,Ring_list) :-
   atoms(Drug,5,Atom_list,[c,c,c,c,c]),
   ring5(Drug,Atom_list,Ring_list,Bond_list),
   Bond_list \== [7,7,7,7,7].

%****************************************************************************

hetero_aromatic_6_ring(Drug,Ring_list) :-
   atoms(Drug,6,Atom_list,Type_list),
   Type_list \== [c,c,c,c,c,c],
   ring6(Drug,Atom_list,Ring_list,[7,7,7,7,7,7]).

hetero_aromatic_5_ring(Drug,Ring_list) :-
   atoms(Drug,5,Atom_list,Type_list),
   Type_list \== [c,c,c,c,c],
   ring5(Drug,Atom_list,Ring_list,[7,7,7,7,7]).


%****************************************************************************

atoms(Drug,1,[Atom],[T]) :- 
   atm(Drug,Atom,T,_,_),
   T \== h.
atoms(Drug,N1,[Atom1|[Atom2|List_a]],[T1|[T2|List_t]]) :- 
   N1 > 1,
   N2 is N1 - 1,
   atoms(Drug,N2,[Atom2|List_a],[T2|List_t]),
   atm(Drug,Atom1,T1,_,_),
   Atom1 @> Atom2,
   T1 \== h.


%****************************************************************************

ring6(Drug,[Atom1|List],[Atom1,Atom2,Atom4,Atom6,Atom5,Atom3],
   [Type1,Type2,Type3,Type4,Type5,Type6]) :-
   bondd(Drug,Atom1,Atom2,Type1),
   memberchk(Atom2,[Atom1|List]),
   bondd(Drug,Atom1,Atom3,Type2),
   memberchk(Atom3,[Atom1|List]),
   Atom3 @> Atom2,
   bondd(Drug,Atom2,Atom4,Type3),
   Atom4 \== Atom1,
   memberchk(Atom4,[Atom1|List]),
   bondd(Drug,Atom3,Atom5,Type4),
   Atom5 \== Atom1,
   memberchk(Atom5,[Atom1|List]),
   bondd(Drug,Atom4,Atom6,Type5),
   Atom6 \== Atom2,
   memberchk(Atom6,[Atom1|List]),
   bondd(Drug,Atom5,Atom6,Type6),
   Atom6 \== Atom3.


ring5(Drug,[Atom1|List],[Atom1,Atom2,Atom4,Atom5,Atom3],
   [Type1,Type2,Type3,Type4,Type5]) :-
   bondd(Drug,Atom1,Atom2,Type1),
   memberchk(Atom2,[Atom1|List]),
   bondd(Drug,Atom1,Atom3,Type2),
   memberchk(Atom3,[Atom1|List]),
   Atom3 @> Atom2,
   bondd(Drug,Atom2,Atom4,Type3),
   Atom4 \== Atom1,
   memberchk(Atom4,[Atom1|List]),
   bondd(Drug,Atom3,Atom5,Type4),
   Atom5 \== Atom1,
   memberchk(Atom5,[Atom1|List]),
   bondd(Drug,Atom4,Atom5,Type5),
   Atom5 \== Atom2.

%****************************************************************************

no_of_nitros(Drug,No) :-
   setof(Nitro,nitro(Drug,Nitro),List),
   length(List,No).

nitro(Drug,[Atom0,Atom1,Atom2,Atom3]) :-
   atm(Drug,Atom1,n,38,_),
   bondd(Drug,Atom0,Atom1,1),
   bondd(Drug,Atom1,Atom2,2),
   atm(Drug,Atom2,o,40,_),
   bondd(Drug,Atom1,Atom3,2),
   Atom3 @> Atom2,
   atm(Drug,Atom3,o,40,_).

%****************************************************************************


methyl(Drug,[Atom0,Atom1,Atom2,Atom3,Atom4]) :-
   atm(Drug,Atom1,c,10,_),
   bondd(Drug,Atom0,Atom1,1),
   atm(Drug,Atom0,Type,_,_), 
   Type \== h,
   bondd(Drug,Atom1,Atom2,1),
   atm(Drug,Atom2,h,3,_), 
   bondd(Drug,Atom1,Atom3,1),
   Atom3 @> Atom2,
   atm(Drug,Atom3,h,3,_), 
   bondd(Drug,Atom1,Atom4,1),
   Atom4 @> Atom3,
   atm(Drug,Atom4,h,3,_).


%****************************************************************************

% intersection(+Set1, +Set2, ?Intersection)

interjoin(A,B,C) :-
   intersection(A,B,C),
   C \== [].

%****************************************************************************


bondd(Drug,Atom1,Atom2,Type) :-  bond(Drug,Atom1,Atom2,Type).
bondd(Drug,Atom1,Atom2,Type) :-  bond(Drug,Atom2,Atom1,Type).

%****************************************************************************
