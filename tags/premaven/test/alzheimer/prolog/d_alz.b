% alzheimer's data from j. med chem
% determinate background knowledge

%!- mode(x_subst(+,-,-)).
%!- mode(alk_groups(+,-)).
%!- mode(r_subst_1(+,-)).
%!- mode(r_subst_2(+,-)).
%!- mode(r_subst_3(+,-)).
%!- mode(ring_substitutions(+,-)).
%!- mode(ring_subst_1(+,-)).
%!- mode(ring_subst_2(+,-)).
%!- mode(ring_subst_3(+,-)).
%!- mode(ring_subst_4(+,-)).
%!- mode(ring_subst_5(+,-)).
%!- mode(ring_subst_6(+,-)).
%!- mode(polar(+,-)).
%!- mode(size(+,-)).
%!- mode(flex(+,-)).
%!- mode(h_doner(+,-)).
%!- mode(h_acceptor(+,-)).
%!- mode(pi_doner(+,-)).
%!- mode(pi_acceptor(+,-)).
%!- mode(polarisable(+,-)).
%!- mode(sigma(+,-)).
%!- mode(n_val(+,-)).
%!- mode(subs(+,-)).
%!- mode(group(+,-)).


% polarities etc.

polar(ch3,polar0).
% the formula N(CH3)2 can be represented as a list
% polar([n,group(ch3,2)],polar1).
% or as any other 2-arity function
polar(bond(n,group(ch3,2)),polar1).
polar(och3,polar2).
polar(cf3,polar3).
polar(cl,polar3).
polar(f,polar5).

size(ch3,size1).
size(cl,size1).
size(f,size1).
size(cf3,size1).
% size([n,group(ch3,2)],size2).
size(bond(n,group(ch3,2)),size2).
size(och3,size2).


flex(f,flex0).
flex(ch3,flex0).
flex(cl,flex0).
flex(cf3,flex0).
flex(och3,flex1).
% flex([n,group(ch3,2)],flex0).
flex(bond(n,group(ch3,2)),flex0).

h_doner(f,h_don0).
h_doner(ch3,h_don0).
h_doner(cf3,h_don0).
h_doner(och3,h_don0).
h_doner(cl,h_don0).
% h_doner([n,group(ch3,2)],h_don0).
h_doner(bond(n,group(ch3,2)),h_don0).

h_acceptor(ch3,h_acc0).
h_acceptor(cl,h_acc0).
h_acceptor(cf3,h_acc0).
h_acceptor(f,h_acc1).
% h_acceptor([n,group(ch3,2)],h_acc1).
h_acceptor(bond(n,group(ch3,2)),h_acc1).
h_acceptor(och3,h_acc1).

pi_doner(ch3,pi_don0).
pi_doner(cf3,pi_don0).
pi_doner(f,pi_don0).
pi_doner(cl,pi_don0).
pi_doner(och3,pi_don1).
% pi_doner([n,group(ch3,2)],pi_don2).
pi_doner(bond(n,group(ch3,2)),pi_don2).

pi_acceptor(f,pi_acc0).
pi_acceptor(ch3,pi_acc0).
pi_acceptor(cl,pi_acc0).
% pi_acceptor([n,group(ch3,2)],pi_acc0).
pi_acceptor(bond(n,group(ch3,2)),pi_acc0).
pi_acceptor(och3,pi_acc0).
pi_acceptor(cf3,pi_acc0).

polarisable(cf3,polari0).
polarisable(f,polari0).
polarisable(ch3,polari1).
polarisable(cl,polari1).
% polarisable([n,group(ch3,2)],polari1).
polarisable(bond(n,group(ch3,2)),polari1).
polarisable(och3,polari1).

sigma(ch3,sigma0).
% sigma([n,group(ch3,2)],sigma1).
sigma(bond(n,group(ch3,2)),sigma1).
sigma(och3,sigma1).
sigma(cl,sigma3).
sigma(cf3,sigma3).
sigma(f,sigma5).

gt(1,0).
gt(2,0).
gt(3,0).
gt(4,0).
gt(2,1).
gt(3,1).
gt(4,1).
gt(3,2).
gt(4,2).
gt(4,3).

great_polar(polar1,polar0).
great_polar(polar2,polar0).
great_polar(polar3,polar0).
great_polar(polar4,polar0).
great_polar(polar5,polar0).
great_polar(polar6,polar0).
great_polar(polar7,polar0).
great_polar(polar8,polar0).
great_polar(polar9,polar0).
great_polar(polar2,polar1).
great_polar(polar3,polar1).
great_polar(polar4,polar1).
great_polar(polar5,polar1).
great_polar(polar6,polar1).
great_polar(polar7,polar1).
great_polar(polar8,polar1).
great_polar(polar9,polar1).
great_polar(polar3,polar2).
great_polar(polar4,polar2).
great_polar(polar5,polar2).
great_polar(polar6,polar2).
great_polar(polar7,polar2).
great_polar(polar8,polar2).
great_polar(polar9,polar2).
great_polar(polar4,polar3).
great_polar(polar5,polar3).
great_polar(polar6,polar3).
great_polar(polar7,polar3).
great_polar(polar8,polar3).
great_polar(polar9,polar3).
great_polar(polar5,polar4).
great_polar(polar6,polar4).
great_polar(polar7,polar4).
great_polar(polar8,polar4).
great_polar(polar9,polar4).
great_polar(polar6,polar5).
great_polar(polar7,polar5).
great_polar(polar8,polar5).
great_polar(polar9,polar5).
great_polar(polar7,polar6).
great_polar(polar8,polar6).
great_polar(polar9,polar6).
great_polar(polar8,polar7).
great_polar(polar9,polar7).
great_polar(polar9,polar8).

great_size(size1,size0).
great_size(size2,size0).
great_size(size3,size0).
great_size(size4,size0).
great_size(size5,size0).
great_size(size6,size0).
great_size(size7,size0).
great_size(size8,size0).
great_size(size9,size0).
great_size(size2,size1).
great_size(size3,size1).
great_size(size4,size1).
great_size(size5,size1).
great_size(size6,size1).
great_size(size7,size1).
great_size(size8,size1).
great_size(size9,size1).
great_size(size3,size2).
great_size(size4,size2).
great_size(size5,size2).
great_size(size6,size2).
great_size(size7,size2).
great_size(size8,size2).
great_size(size9,size2).
great_size(size4,size3).
great_size(size5,size3).
great_size(size6,size3).
great_size(size7,size3).
great_size(size8,size3).
great_size(size9,size3).
great_size(size5,size4).
great_size(size6,size4).
great_size(size7,size4).
great_size(size8,size4).
great_size(size9,size4).
great_size(size6,size5).
great_size(size7,size5).
great_size(size8,size5).
great_size(size9,size5).
great_size(size7,size6).
great_size(size8,size6).
great_size(size9,size6).
great_size(size8,size7).
great_size(size9,size7).
great_size(size9,size8).

great_flex(flex1,flex0).
great_flex(flex2,flex0).
great_flex(flex3,flex0).
great_flex(flex4,flex0).
great_flex(flex5,flex0).
great_flex(flex6,flex0).
great_flex(flex7,flex0).
great_flex(flex8,flex0).
great_flex(flex9,flex0).
great_flex(flex2,flex1).
great_flex(flex3,flex1).
great_flex(flex4,flex1).
great_flex(flex5,flex1).
great_flex(flex6,flex1).
great_flex(flex7,flex1).
great_flex(flex8,flex1).
great_flex(flex9,flex1).
great_flex(flex3,flex2).
great_flex(flex4,flex2).
great_flex(flex5,flex2).
great_flex(flex6,flex2).
great_flex(flex7,flex2).
great_flex(flex8,flex2).
great_flex(flex9,flex2).
great_flex(flex4,flex3).
great_flex(flex5,flex3).
great_flex(flex6,flex3).
great_flex(flex7,flex3).
great_flex(flex8,flex3).
great_flex(flex9,flex3).
great_flex(flex5,flex4).
great_flex(flex6,flex4).
great_flex(flex7,flex4).
great_flex(flex8,flex4).
great_flex(flex9,flex4).
great_flex(flex6,flex5).
great_flex(flex7,flex5).
great_flex(flex8,flex5).
great_flex(flex9,flex5).
great_flex(flex7,flex6).
great_flex(flex8,flex6).
great_flex(flex9,flex6).
great_flex(flex8,flex7).
great_flex(flex9,flex7).
great_flex(flex9,flex8).

great_h_don(h_don1,h_don0).
great_h_don(h_don2,h_don0).
great_h_don(h_don3,h_don0).
great_h_don(h_don4,h_don0).
great_h_don(h_don5,h_don0).
great_h_don(h_don6,h_don0).
great_h_don(h_don7,h_don0).
great_h_don(h_don8,h_don0).
great_h_don(h_don9,h_don0).
great_h_don(h_don2,h_don1).
great_h_don(h_don3,h_don1).
great_h_don(h_don4,h_don1).
great_h_don(h_don5,h_don1).
great_h_don(h_don6,h_don1).
great_h_don(h_don7,h_don1).
great_h_don(h_don8,h_don1).
great_h_don(h_don9,h_don1).
great_h_don(h_don3,h_don2).
great_h_don(h_don4,h_don2).
great_h_don(h_don5,h_don2).
great_h_don(h_don6,h_don2).
great_h_don(h_don7,h_don2).
great_h_don(h_don8,h_don2).
great_h_don(h_don9,h_don2).
great_h_don(h_don4,h_don3).
great_h_don(h_don5,h_don3).
great_h_don(h_don6,h_don3).
great_h_don(h_don7,h_don3).
great_h_don(h_don8,h_don3).
great_h_don(h_don9,h_don3).
great_h_don(h_don5,h_don4).
great_h_don(h_don6,h_don4).
great_h_don(h_don7,h_don4).
great_h_don(h_don8,h_don4).
great_h_don(h_don9,h_don4).
great_h_don(h_don6,h_don5).
great_h_don(h_don7,h_don5).
great_h_don(h_don8,h_don5).
great_h_don(h_don9,h_don5).
great_h_don(h_don7,h_don6).
great_h_don(h_don8,h_don6).
great_h_don(h_don9,h_don6).
great_h_don(h_don8,h_don7).
great_h_don(h_don9,h_don7).
great_h_don(h_don9,h_don8).


great_h_acc(h_acc1,h_acc0).
great_h_acc(h_acc2,h_acc0).
great_h_acc(h_acc3,h_acc0).
great_h_acc(h_acc4,h_acc0).
great_h_acc(h_acc5,h_acc0).
great_h_acc(h_acc6,h_acc0).
great_h_acc(h_acc7,h_acc0).
great_h_acc(h_acc8,h_acc0).
great_h_acc(h_acc9,h_acc0).
great_h_acc(h_acc2,h_acc1).
great_h_acc(h_acc3,h_acc1).
great_h_acc(h_acc4,h_acc1).
great_h_acc(h_acc5,h_acc1).
great_h_acc(h_acc6,h_acc1).
great_h_acc(h_acc7,h_acc1).
great_h_acc(h_acc8,h_acc1).
great_h_acc(h_acc9,h_acc1).
great_h_acc(h_acc3,h_acc2).
great_h_acc(h_acc4,h_acc2).
great_h_acc(h_acc5,h_acc2).
great_h_acc(h_acc6,h_acc2).
great_h_acc(h_acc7,h_acc2).
great_h_acc(h_acc8,h_acc2).
great_h_acc(h_acc9,h_acc2).
great_h_acc(h_acc4,h_acc3).
great_h_acc(h_acc5,h_acc3).
great_h_acc(h_acc6,h_acc3).
great_h_acc(h_acc7,h_acc3).
great_h_acc(h_acc8,h_acc3).
great_h_acc(h_acc9,h_acc3).
great_h_acc(h_acc5,h_acc4).
great_h_acc(h_acc6,h_acc4).
great_h_acc(h_acc7,h_acc4).
great_h_acc(h_acc8,h_acc4).
great_h_acc(h_acc9,h_acc4).
great_h_acc(h_acc6,h_acc5).
great_h_acc(h_acc7,h_acc5).
great_h_acc(h_acc8,h_acc5).
great_h_acc(h_acc9,h_acc5).
great_h_acc(h_acc7,h_acc6).
great_h_acc(h_acc8,h_acc6).
great_h_acc(h_acc9,h_acc6).
great_h_acc(h_acc8,h_acc7).
great_h_acc(h_acc9,h_acc7).
great_h_acc(h_acc9,h_acc8).

great_pi_don(pi_don1,pi_don0).
great_pi_don(pi_don2,pi_don0).
great_pi_don(pi_don3,pi_don0).
great_pi_don(pi_don4,pi_don0).
great_pi_don(pi_don5,pi_don0).
great_pi_don(pi_don6,pi_don0).
great_pi_don(pi_don7,pi_don0).
great_pi_don(pi_don8,pi_don0).
great_pi_don(pi_don9,pi_don0).
great_pi_don(pi_don2,pi_don1).
great_pi_don(pi_don3,pi_don1).
great_pi_don(pi_don4,pi_don1).
great_pi_don(pi_don5,pi_don1).
great_pi_don(pi_don6,pi_don1).
great_pi_don(pi_don7,pi_don1).
great_pi_don(pi_don8,pi_don1).
great_pi_don(pi_don9,pi_don1).
great_pi_don(pi_don3,pi_don2).
great_pi_don(pi_don4,pi_don2).
great_pi_don(pi_don5,pi_don2).
great_pi_don(pi_don6,pi_don2).
great_pi_don(pi_don7,pi_don2).
great_pi_don(pi_don8,pi_don2).
great_pi_don(pi_don9,pi_don2).
great_pi_don(pi_don4,pi_don3).
great_pi_don(pi_don5,pi_don3).
great_pi_don(pi_don6,pi_don3).
great_pi_don(pi_don7,pi_don3).
great_pi_don(pi_don8,pi_don3).
great_pi_don(pi_don9,pi_don3).
great_pi_don(pi_don5,pi_don4).
great_pi_don(pi_don6,pi_don4).
great_pi_don(pi_don7,pi_don4).
great_pi_don(pi_don8,pi_don4).
great_pi_don(pi_don9,pi_don4).
great_pi_don(pi_don6,pi_don5).
great_pi_don(pi_don7,pi_don5).
great_pi_don(pi_don8,pi_don5).
great_pi_don(pi_don9,pi_don5).
great_pi_don(pi_don7,pi_don6).
great_pi_don(pi_don8,pi_don6).
great_pi_don(pi_don9,pi_don6).
great_pi_don(pi_don8,pi_don7).
great_pi_don(pi_don9,pi_don7).
great_pi_don(pi_don9,pi_don8).

great_pi_acc(pi_acc1,pi_acc0).
great_pi_acc(pi_acc2,pi_acc0).
great_pi_acc(pi_acc3,pi_acc0).
great_pi_acc(pi_acc4,pi_acc0).
great_pi_acc(pi_acc5,pi_acc0).
great_pi_acc(pi_acc6,pi_acc0).
great_pi_acc(pi_acc7,pi_acc0).
great_pi_acc(pi_acc8,pi_acc0).
great_pi_acc(pi_acc9,pi_acc0).
great_pi_acc(pi_acc2,pi_acc1).
great_pi_acc(pi_acc3,pi_acc1).
great_pi_acc(pi_acc4,pi_acc1).
great_pi_acc(pi_acc5,pi_acc1).
great_pi_acc(pi_acc6,pi_acc1).
great_pi_acc(pi_acc7,pi_acc1).
great_pi_acc(pi_acc8,pi_acc1).
great_pi_acc(pi_acc9,pi_acc1).
great_pi_acc(pi_acc3,pi_acc2).
great_pi_acc(pi_acc4,pi_acc2).
great_pi_acc(pi_acc5,pi_acc2).
great_pi_acc(pi_acc6,pi_acc2).
great_pi_acc(pi_acc7,pi_acc2).
great_pi_acc(pi_acc8,pi_acc2).
great_pi_acc(pi_acc9,pi_acc2).
great_pi_acc(pi_acc4,pi_acc3).
great_pi_acc(pi_acc5,pi_acc3).
great_pi_acc(pi_acc6,pi_acc3).
great_pi_acc(pi_acc7,pi_acc3).
great_pi_acc(pi_acc8,pi_acc3).
great_pi_acc(pi_acc9,pi_acc3).
great_pi_acc(pi_acc5,pi_acc4).
great_pi_acc(pi_acc6,pi_acc4).
great_pi_acc(pi_acc7,pi_acc4).
great_pi_acc(pi_acc8,pi_acc4).
great_pi_acc(pi_acc9,pi_acc4).
great_pi_acc(pi_acc6,pi_acc5).
great_pi_acc(pi_acc7,pi_acc5).
great_pi_acc(pi_acc8,pi_acc5).
great_pi_acc(pi_acc9,pi_acc5).
great_pi_acc(pi_acc7,pi_acc6).
great_pi_acc(pi_acc8,pi_acc6).
great_pi_acc(pi_acc9,pi_acc6).
great_pi_acc(pi_acc8,pi_acc7).
great_pi_acc(pi_acc9,pi_acc7).
great_pi_acc(pi_acc9,pi_acc8).

great_polari(polari1,polari0).
great_polari(polari2,polari0).
great_polari(polari3,polari0).
great_polari(polari4,polari0).
great_polari(polari5,polari0).
great_polari(polari6,polari0).
great_polari(polari7,polari0).
great_polari(polari8,polari0).
great_polari(polari9,polari0).
great_polari(polari2,polari1).
great_polari(polari3,polari1).
great_polari(polari4,polari1).
great_polari(polari5,polari1).
great_polari(polari6,polari1).
great_polari(polari7,polari1).
great_polari(polari8,polari1).
great_polari(polari9,polari1).
great_polari(polari3,polari2).
great_polari(polari4,polari2).
great_polari(polari5,polari2).
great_polari(polari6,polari2).
great_polari(polari7,polari2).
great_polari(polari8,polari2).
great_polari(polari9,polari2).
great_polari(polari4,polari3).
great_polari(polari5,polari3).
great_polari(polari6,polari3).
great_polari(polari7,polari3).
great_polari(polari8,polari3).
great_polari(polari9,polari3).
great_polari(polari5,polari4).
great_polari(polari6,polari4).
great_polari(polari7,polari4).
great_polari(polari8,polari4).
great_polari(polari9,polari4).
great_polari(polari6,polari5).
great_polari(polari7,polari5).
great_polari(polari8,polari5).
great_polari(polari9,polari5).
great_polari(polari7,polari6).
great_polari(polari8,polari6).
great_polari(polari9,polari6).
great_polari(polari8,polari7).
great_polari(polari9,polari7).
great_polari(polari9,polari8).

great_sigma(sigma1,sigma0).
great_sigma(sigma2,sigma0).
great_sigma(sigma3,sigma0).
great_sigma(sigma4,sigma0).
great_sigma(sigma5,sigma0).
great_sigma(sigma6,sigma0).
great_sigma(sigma7,sigma0).
great_sigma(sigma8,sigma0).
great_sigma(sigma9,sigma0).
great_sigma(sigma2,sigma1).
great_sigma(sigma3,sigma1).
great_sigma(sigma4,sigma1).
great_sigma(sigma5,sigma1).
great_sigma(sigma6,sigma1).
great_sigma(sigma7,sigma1).
great_sigma(sigma8,sigma1).
great_sigma(sigma9,sigma1).
great_sigma(sigma3,sigma2).
great_sigma(sigma4,sigma2).
great_sigma(sigma5,sigma2).
great_sigma(sigma6,sigma2).
great_sigma(sigma7,sigma2).
great_sigma(sigma8,sigma2).
great_sigma(sigma9,sigma2).
great_sigma(sigma4,sigma3).
great_sigma(sigma5,sigma3).
great_sigma(sigma6,sigma3).
great_sigma(sigma7,sigma3).
great_sigma(sigma8,sigma3).
great_sigma(sigma9,sigma3).
great_sigma(sigma5,sigma4).
great_sigma(sigma6,sigma4).
great_sigma(sigma7,sigma4).
great_sigma(sigma8,sigma4).
great_sigma(sigma9,sigma4).
great_sigma(sigma6,sigma5).
great_sigma(sigma7,sigma5).
great_sigma(sigma8,sigma5).
great_sigma(sigma9,sigma5).
great_sigma(sigma7,sigma6).
great_sigma(sigma8,sigma6).
great_sigma(sigma9,sigma6).
great_sigma(sigma8,sigma7).
great_sigma(sigma9,sigma7).
great_sigma(sigma9,sigma8).

% substitution X at positions 6 or 7

x_subst(b1,7,cl).
x_subst(c1,6,cl).
x_subst(d1,6,och3).
x_subst(e1,6,cf3).
x_subst(f1,6,f).
x_subst(hh1,6,cl).
x_subst(ii1,6,cl).
x_subst(jj1,6,f).
x_subst(kk1,6,f).
x_subst(ll1,6,cf3).

% substitution R in middle ring
% number of alkyl substitutions

alk_groups(a1,0).
alk_groups(b1,0).
alk_groups(c1,0).
alk_groups(d1,0).
alk_groups(e1,0).
alk_groups(f1,0).
alk_groups(g1,0).
alk_groups(h1,1).
alk_groups(i1,3).
alk_groups(j1,2).
alk_groups(k1,2).
alk_groups(l1,3).
alk_groups(m1,4).
alk_groups(n1,4).
alk_groups(o1,4).
alk_groups(p1,1).
alk_groups(q1,1).
alk_groups(r1,1).
alk_groups(s1,1).
alk_groups(t1,1).
alk_groups(u1,1).
alk_groups(v1,1).
alk_groups(w1,1).
alk_groups(x1,1).
alk_groups(y1,1).
alk_groups(z1,1).
alk_groups(aa1,1).
alk_groups(bb1,1).
alk_groups(cc1,1).
alk_groups(dd1,1).
alk_groups(ee1,1).
alk_groups(ff1,1).
alk_groups(hh1,1).
alk_groups(ii1,1).
alk_groups(jj1,1).
alk_groups(kk1,1).
alk_groups(ll1,1).




r_subst_1(a1,h).
r_subst_1(b1,h).
r_subst_1(c1,h).
r_subst_1(d1,h).
r_subst_1(e1,h).
r_subst_1(f1,h).
r_subst_1(g1,h).
n_val(g1,1).

r_subst_1(h1,single_alk(1)).
r_subst_1(i1,single_alk(3)).
r_subst_1(j1,single_alk(2)).
r_subst_2(j1,bond(n,group(ch3,2))).
r_subst_1(k1,single_alk(2)).
r_subst_2(k1,aro(1)).
r_subst_1(l1,single_alk(3)).
r_subst_2(l1,o).
r_subst_3(l1,aro(1)).
r_subst_1(m1,single_alk(3)).
r_subst_2(m1,double_alk(1)).
r_subst_3(m1,aro(2)).
r_subst_1(n1,single_alk(3)).
r_subst_2(n1,double_alk(1)).
r_subst_3(n1,aro(2)).
r_subst_1(o1,single_alk(3)).
r_subst_2(o1,double_alk(1)).
r_subst_3(o1,aro(2)).
r_subst_1(p1,single_alk(1)).
r_subst_2(p1,aro(1)).
r_subst_1(q1,single_alk(1)).
r_subst_2(q1,aro(1)).
r_subst_1(r1,single_alk(1)).
r_subst_2(r1,aro(1)).
r_subst_1(s1,single_alk(1)).
r_subst_2(s1,aro(1)).
r_subst_1(t1,single_alk(1)).
r_subst_2(t1,aro(1)).
r_subst_1(u1,single_alk(1)).
r_subst_2(u1,aro(1)).
r_subst_1(v1,single_alk(1)).
r_subst_2(v1,aro(1)).
r_subst_1(w1,single_alk(1)).
r_subst_2(w1,aro(1)).
r_subst_1(x1,single_alk(1)).
r_subst_2(x1,aro(1)).
r_subst_1(y1,single_alk(1)).
r_subst_2(y1,aro(1)).
r_subst_1(z1,single_alk(1)).
r_subst_2(z1,aro(1)).
r_subst_1(aa1,single_alk(1)).
r_subst_2(aa1,aro(1)).
r_subst_1(bb1,single_alk(1)).
r_subst_2(bb1,aro(1)).
r_subst_1(cc1,single_alk(1)).
r_subst_2(cc1,aro(1)).
r_subst_1(dd1,single_alk(1)).
r_subst_2(dd1,aro(1)).
r_subst_1(ee1,single_alk(1)).
r_subst_2(ee1,aro(1)).
r_subst_1(ff1,single_alk(1)).
r_subst_2(ff1,aro(1)).
r_subst_1(hh1,single_alk(1)).
r_subst_2(hh1,aro(1)).
r_subst_1(ii1,single_alk(1)).
r_subst_2(ii1,aro(1)).
r_subst_1(jj1,single_alk(1)).
r_subst_2(jj1,aro(1)).
r_subst_1(kk1,single_alk(1)).
r_subst_2(kk1,aro(1)).
r_subst_1(ll1,single_alk(1)).
r_subst_2(ll1,aro(1)).

% number of substituents in ring
% 0 == normal benzene ring

ring_substitutions(k1,0).
ring_substitutions(l1,0).
ring_substitutions(m1,0).
ring_substitutions(n1,1).
ring_substitutions(o1,1).
ring_substitutions(p1,0).
ring_substitutions(q1,1).
ring_substitutions(r1,1).
ring_substitutions(s1,1).
ring_substitutions(t1,1).
ring_substitutions(u1,1).
ring_substitutions(v1,1).
ring_substitutions(w1,1).
ring_substitutions(x1,1).
ring_substitutions(y1,1).
ring_substitutions(z1,1).
ring_substitutions(aa1,1).
ring_substitutions(bb1,1).
ring_substitutions(cc1,1).
ring_substitutions(dd1,1).
ring_substitutions(ee1,1).
ring_substitutions(ff1,5).
ring_substitutions(hh1,0).
ring_substitutions(ii1,1).
ring_substitutions(jj1,0).
ring_substitutions(kk1,1).
ring_substitutions(ll1,0).

% substitents in ring
% ring_subs_Pos(Drug,Substituent)

ring_subst_4(n1,f).
ring_subst_3(o1,f).
ring_subst_2(q1,cl).
ring_subst_3(r1,cl).
ring_subst_4(s1,cl).
ring_subst_2(t1,f).
ring_subst_3(u1,f).
ring_subst_4(v1,f).
ring_subst_2(w1,och3).
ring_subst_3(x1,och3).
ring_subst_4(y1,och3).
ring_subst_2(z1,ch3).
ring_subst_3(aa1,ch3).
ring_subst_4(bb1,ch3).
ring_subst_2(cc1,cf3).
ring_subst_3(dd1,cf3).
ring_subst_4(ee1,cf3).
ring_subst_2(ff1,f).
ring_subst_3(ff1,f).
ring_subst_4(ff1,f).
ring_subst_5(ff1,f).
ring_subst_6(ff1,f).
ring_subst_4(ii1,f).
ring_subst_2(kk1,cf3).
