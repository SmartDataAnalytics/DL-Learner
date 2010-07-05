% alzheimer's data from j. med chem
% non-determinate background knowledge

%!- mode(x_subst(+,-,-)).
%!- mode(r_subst(+,-,-)).
%!- mode(ring_struc(+,-)).
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

r_subst(a1,1,h).
r_subst(b1,1,h).
r_subst(c1,1,h).
r_subst(d1,1,h).
r_subst(e1,1,h).
r_subst(f1,1,h).
r_subst(g1,1,h).
n_val(g1,1).

r_subst(h1,1,single_alk(1)).
r_subst(i1,1,single_alk(3)).

r_subst(j1,1,single_alk(2)).
r_subst(j1,2,bond(n,group(ch3,2))).

r_subst(k1,1,single_alk(2)).
r_subst(k1,2,aro(1)).
ring_struc(k1,normal).

r_subst(l1,1,single_alk(3)).
r_subst(l1,2,o).
r_subst(l1,3,aro(1)).
ring_struc(l1,normal).

r_subst(m1,1,single_alk(3)).
r_subst(m1,2,double_alk(1)).
r_subst(m1,3,aro(2)).
ring_struc(m1,normal).

r_subst(n1,1,single_alk(3)).
r_subst(n1,2,double_alk(1)).
r_subst(n1,3,aro(2)).
ring_struc(n1,subs(f,4)).

r_subst(o1,1,single_alk(3)).
r_subst(o1,2,double_alk(1)).
r_subst(o1,3,aro(2)).
ring_struc(o1,subs(f,3)).

r_subst(p1,1,single_alk(1)).
r_subst(p1,2,aro(1)).
ring_struc(p1,normal).

r_subst(q1,1,single_alk(1)).
r_subst(q1,2,aro(1)).
ring_struc(q1,subs(cl,2)).

r_subst(r1,1,single_alk(1)).
r_subst(r1,2,aro(1)).
ring_struc(r1,subs(cl,3)).

r_subst(s1,1,single_alk(1)).
r_subst(s1,2,aro(1)).
ring_struc(s1,subs(cl,4)).

r_subst(t1,1,single_alk(1)).
r_subst(t1,2,aro(1)).
ring_struc(t1,subs(f,2)).

r_subst(u1,1,single_alk(1)).
r_subst(u1,2,aro(1)).
ring_struc(u1,subs(f,3)).

r_subst(v1,1,single_alk(1)).
r_subst(v1,2,aro(1)).
ring_struc(v1,subs(f,4)).

r_subst(w1,1,single_alk(1)).
r_subst(w1,2,aro(1)).
ring_struc(w1,subs(och3,2)).

r_subst(x1,1,single_alk(1)).
r_subst(x1,2,aro(1)).
ring_struc(x1,subs(och3,3)).

r_subst(y1,1,single_alk(1)).
r_subst(y1,2,aro(1)).
ring_struc(y1,subs(och3,4)).

r_subst(z1,1,single_alk(1)).
r_subst(z1,2,aro(1)).
ring_struc(z1,subs(ch3,2)).

r_subst(aa1,1,single_alk(1)).
r_subst(aa1,2,aro(1)).
ring_struc(aa1,subs(ch3,3)).

r_subst(bb1,1,single_alk(1)).
r_subst(bb1,2,aro(1)).
ring_struc(aa1,subs(ch3,4)).

r_subst(cc1,1,single_alk(1)).
r_subst(cc1,2,aro(1)).
ring_struc(cc1,subs(cf3,2)).

r_subst(dd1,1,single_alk(1)).
r_subst(dd1,2,aro(1)).
ring_struc(dd1,subs(cf3,3)).

r_subst(ee1,1,single_alk(1)).
r_subst(ee1,2,aro(1)).
ring_struc(ee1,subs(cf3,4)).

r_subst(ff1,1,single_alk(1)).
r_subst(ff1,2,aro(1)).
ring_struc(ff1,subs(f,1)).
ring_struc(ff1,subs(f,2)).
ring_struc(ff1,subs(f,3)).
ring_struc(ff1,subs(f,4)).
ring_struc(ff1,subs(f,5)).

% r_subst(gg1,1,single_alk(1)).

r_subst(hh1,1,single_alk(1)).
r_subst(hh1,2,aro(1)).
ring_struc(hh1,normal).

r_subst(ii1,1,single_alk(1)).
r_subst(ii1,2,aro(1)).
ring_struc(ii1,subs(f,4)).

r_subst(jj1,1,single_alk(1)).
r_subst(jj1,2,aro(1)).
ring_struc(jj1,normal).

r_subst(kk1,1,single_alk(1)).
r_subst(kk1,2,aro(1)).
ring_struc(kk1,subs(cf3,2)).

r_subst(ll1,1,single_alk(1)).
r_subst(ll1,2,aro(1)).
ring_struc(ll1,normal).

% polarities etc.

polar(ch3,polar0).
polar(bond(n,group(ch3,2)),polar1).
polar(och3,polar2).
polar(cf3,polar3).
polar(cl,polar3).
polar(f,polar5).

size(ch3,size1).
size(cl,size1).
size(f,size1).
size(cf3,size1).
size(bond(n,group(ch3,2)),size2).
size(och3,size2).


flex(f,flex0).
flex(ch3,flex0).
flex(cl,flex0).
flex(cf3,flex0).
flex(och3,flex1).
flex(bond(n,group(ch3,2)),flex0).

h_doner(f,h_don0).
h_doner(ch3,h_don0).
h_doner(cf3,h_don0).
h_doner(och3,h_don0).
h_doner(cl,h_don0).
h_doner(bond(n,group(ch3,2)),h_don0).

h_acceptor(ch3,h_acc0).
h_acceptor(cl,h_acc0).
h_acceptor(cf3,h_acc0).
h_acceptor(f,h_acc1).
h_acceptor(bond(n,group(ch3,2)),h_acc1).
h_acceptor(och3,h_acc1).

pi_doner(ch3,pi_don0).
pi_doner(cf3,pi_don0).
pi_doner(f,pi_don0).
pi_doner(cl,pi_don0).
pi_doner(och3,pi_don1).
pi_doner(bond(n,group(ch3,2)),pi_don2).

pi_acceptor(f,pi_acc0).
pi_acceptor(ch3,pi_acc0).
pi_acceptor(cl,pi_acc0).
pi_acceptor(bond(n,group(ch3,2)),pi_acc0).
pi_acceptor(och3,pi_acc0).
pi_acceptor(cf3,pi_acc0).

polarisable(cf3,polari0).
polarisable(f,polari0).
polarisable(ch3,polari1).
polarisable(cl,polari1).
polarisable(bond(n,group(ch3,2)),polari1).
polarisable(och3,polari1).

sigma(ch3,sigma0).
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
