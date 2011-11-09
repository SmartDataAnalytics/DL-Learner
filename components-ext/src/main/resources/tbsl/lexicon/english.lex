
// TO BE
// ------

	is  || (S DP[subject] (VP V:'is' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	is  || (S DP[subject] (VP V:'is' ADJ[comp]))   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	was || (S DP[subject] (VP V:'was' DP[object])) || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	was || (S DP[subject] (VP V:'was' ADJ[comp]))  || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	which is || (NP NP* (S C:'which' (VP V:'is' DP[object]))) || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	that is || (NP NP* (S C:'that' (VP V:'is' DP[object]))) || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
		
	are  || (S DP[subject] (VP V:'are' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	are  || (S DP[subject] (VP V:'are' ADJ[comp]))   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	were || (S DP[subject] (VP V:'were' DP[object])) || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	were || (S DP[subject] (VP V:'were' ADJ[comp]))  || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	which are || (NP NP* (S C:'which' (VP V:'are' DP[object]))) || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	that are || (NP NP* (S C:'that' (VP V:'are' DP[object]))) || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>

	is there  || (S V:'is' C:'there' DP[dp])  || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>
	are there || (S V:'are' C:'there' DP[dp]) || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>
	is there  || (S DP[dp] (VP V:'is' C:'there'))  || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>
	are there || (S DP[dp] (VP V:'are' C:'there')) || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>

// TO BE: YES/NO QUESTIONS

	is   || (S (VP V:'is' DP[subject] DP[object]))   || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	is   || (S (VP V:'is' DP[subject] ADJ[comp]))    || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	was  || (S V:'was' DP[subject] DP[object])  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	was  || (S V:'was' DP[subject] ADJ[comp])   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	are  || (S V:'are' DP[subject] DP[object])  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	are  || (S V:'are' DP[subject] ADJ[comp])   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	were || (S V:'were' DP[subject] DP[object]) || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	were || (S V:'were' DP[subject] ADJ[comp])  || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>

	did || (S V:'did' S*) || <x,l1,t,[ l1:[|] ],[],[],[]>  

// IMPERATIVES
// ---------------------

	give me || (S (VP V:'give' (DP N:'me') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	name || (S (VP V:'name' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
//	give me all || (S (VP V:'give' (DP N:'me') DET:'all' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
//	name all || (S (VP V:'name' DET:'all' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	show me || (S (VP V:'show' (DP N:'me') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	show || (S (VP V:'show' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	list me || (S (VP V:'list' (DP N:'me') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	list || (S (VP V:'list' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>


// DETERMINER
// ----------
	
	a  || (DP DET:'a' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	an || (DP DET:'an' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	all || (DP DET:'all' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	all the || (DP DET:'all' DET:'the' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	every || (DP DET:'every' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	no || (DP DET:'no' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] NO y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	each || (DP DET:'each' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	the most || (DP DET:'the' ADJ:'most' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	the least || (DP DET:'the' ADJ:'least' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	most || (DP DET:'most' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] MOST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	a few || (DP DET:'a' ADJ:'few' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] AFEW y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	some || (DP DET:'some' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[ x | ] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	which || (DP DET:'which' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	what || (DP DET:'what' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	many || (DP DET:'many' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] MANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	the || (DP DET:'the' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	at least || (DP DET:'at' DET:'least' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[l1:[ y,j |count(a,y,j), greaterorequal(j,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[]>
	at most || (DP DET:'at' DET:'most' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[l1:[ y,j | count(a,y,j), smallerorequal(j,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[]>
	exactly || (DP DET:'exactly' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[l1:[ y,j | count(y,j), equals(j,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[]>
	
	other || (NP ADJ:'other' NP*) || <x,l1,<e,t>,[ l1:[ | ] ], [],[],[]>
	total || (NP ADJ:'total' NP[np]) || <s,l1,<e,t>,[ l1:[ ?s | sum(a,x,s) ] ], [ (l2,x,np,<e,t>) ],[ l2=l1 ],[]>
	
	least || (ADJ DET:'least' ADJ*) || <x,l1,<e,t>,[ l1:[ | minimum(a,x,x) ] ], [],[],[]>
	
  	how many || (DET DET:'how' DET:'many') || <x,l1,e, [ l1:[ ?x | ] ], [],[],[]>
  	how many || (DET DET:'how' DET:'many') || <x,l1,e, [ l1:[ ?c,x | count(a,x,c) ] ], [],[],[]>
	a  || (DET DET:'a') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	an || (DET DET:'an') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	which || (DET DET:'which') || <x,l1,e, [ l1:[ ?x |] ], [],[],[]>
	the || (DET DET:'the') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	the most || (DET DET:'the' DET:'most') || <y, l1, e, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [], [],[]>
	the least || (DET DET:'the' DET:'least') || <y, l1, e, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [], [],[]>

    // CHEAT!
	highest || (NP ADJ:'highest' NP*) || <x, l1, e, [ l1:[ | maximum(x) ] ], [], [],[]> ;; <x, l1, e, [ l1:[ j | SLOT_high(x,j), maximum(j) ] ],[],[],[ SLOT_high/PROPERTY/height^elevation ]>

	// COUNT
	more than || (DP DET:'more' DET:'than' NUM[num] NP[np]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), greater(c,z) ] ],[(l2,y,np,<e,t>),(l3,z,num,e)],[l2=l1,l3=l1],[]> ;; <y,l1,<<e,t>,t>,[ l1:[ y | greater(y,z) ] ],[(l2,y,np,<e,t>),(l3,z,num,e)],[l2=l1,l3=l1],[]>
	less than || (DP DET:'less' DET:'than' NUM[num] NP[np]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), less(c,z) ] ],[(l2,y,np,<e,t>),(l3,z,num,e)],[l2=l1,l3=l1],[]> ;; <y,l1,<<e,t>,t>,[ l1:[ y | less(y,z) ] ],[(l2,y,np,<e,t>),(l3,z,num,e)],[l2=l1,l3=l1],[]>

	// HOW
	// how || (DP DET:'how' ADJ[adj]) || <x,l1,<<e,t>,t>,[ l1:[?x,|] ],[ (x,l2,adj,<e,t>) ],[l2=l1],[]>

	
// EMPTY STUFF 
// ------------

	also || (VP ADV:'also' VP*) || <x,l1,t,[ l1:[|] ],[],[],[]>
	also || (DP ADV:'also' DP*) || <x,l1,<<e,t>,t>,[ l1:[|] ],[],[],[]>
	
	has  || (S DP[subject] (VP V:'has' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	have || (S DP[subject] (VP V:'have' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	had  || (S DP[subject] (VP V:'had' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	
//	with || (NP NP* (PP P:'with' DP[dp])) || <x,l1,<e,t>,[ l1:[| empty(x,y) ] ],[(l2,y,dp,<<e,t>,t>)],[l2=l1],[]>
	
	people || (NP N:'people') || <x,l1,<e,t>,[ l1:[|] ],[],[],[]>
	

// WH WORDS
// --------

	what     || (DP WH:'what')      || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	which    || (DP WH:'which')     || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	how many || (DP WH:'how' ADJ:'many' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] HOWMANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	who      || (DP WH:'who')       || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>	
	whom     || (DP WH:'whom')      || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	when     || (S WH:'when' S[s])  || <x, l1, t, [ l1:[ ?x | SLOT_p(y,x) ] ], [(l2,y,s,t)], [l2=l1], [ SLOT_p/PROPERTY/date ]> 
	when     || (DP WH:'when')      || <y, l1, <<e,t>,t>, [ l1:[ ?x | SLOT_p(y,x) ] ], [], [], [ SLOT_p/PROPERTY/date ]> 
	where    || (S WH:'where' S[s]) || <x, l1, t, [ l1:[ ?x | SLOT_p(y,x) ] ], [(l2,y,s,t)], [l2=l1], [ SLOT_p/PROPERTY/place ]>
	where    || (DP WH:'where')     || <y, l1, <<e,t>,t>, [ l1:[ ?x | SLOT_p(y,x) ] ], [], [], [ SLOT_p/PROPERTY/date ]> 
	
	
// NEGATION 
// --------

   not || (ADJ NEG:'not' ADJ*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
   not || (VP NEG:'not' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
   did not || (VP V:'did' NEG:'not' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
   does not || (VP V:'does' NEG:'not' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
   do not || (VP V:'do' NEG:'not' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	


// COORDINATION
// ------------

	and || (S S* CC:'and' S[s]) || <x,l1,t,[l1:[|]],[(l2,y,s,t)],[l1=l2],[]>
	and || (DP DP* CC:'and' DP[dp]) || <x,l1,<<e,t>,t>,[l1:[|]],[(l2,y,dp,<<e,t>,t>)],[l1=l2],[]>
	and || (NP NP* CC:'and' NP[np]) || <x,l1,<e,t>,[l1:[|]],[(l2,y,np,<e,t>)],[l1=l2],[]>
	and || (VP VP* CC:'and' VP[vp]) || -
	and || (ADJ ADJ* CC:'and' ADJ[adj]) || -
	
	as well as || (NP NP* CC:'as' CC:'well' CC:'as' NP[np]) || <x,l1,<e,t>,[l1:[|]],[(l2,y,np,<e,t>)],[l1=l2],[]>
	
	or || (S S* CC:'or' S[2]) || -
	or || (DP DP* CC:'or' DP[2]) || -
	or || (NP NP* CC:'or' NP[2]) || -
	or || (VP VP* CC:'or' VP[2]) || -
	or || (ADJ ADJ* CC:'or' ADJ[2]) || -
	

// EXISTENTIAL
// -----------

	there || (DP (NP EX:'there')) || <x,l1,<<e,t>,t>,[l1:[|]],[],[],[]>


// NUMBERS (1-10)
// ---------------------
	
	one   || (NP NUM:'one' NP*)   || <x,l1,<e,t>,[l1:[x|count(x,1)]],[],[],[]>
	two   || (NP NUM:'two' NP*)   || <x,l1,<e,t>,[l1:[x|count(x,2)]],[],[],[]>
	three || (NP NUM:'three' NP*) || <x,l1,<e,t>,[l1:[x|count(x,3)]],[],[],[]>
	four  || (NP NUM:'four' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,4)]],[],[],[]>
	five  || (NP NUM:'five' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,5)]],[],[],[]>
	six   || (NP NUM:'six' NP*)   || <x,l1,<e,t>,[l1:[x|count(x,6)]],[],[],[]>
	seven || (NP NUM:'seven' NP*) || <x,l1,<e,t>,[l1:[x|count(x,7)]],[],[],[]>
	eight || (NP NUM:'eight' NP*) || <x,l1,<e,t>,[l1:[x|count(x,8)]],[],[],[]>
	nine  || (NP NUM:'nine' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,9)]],[],[],[]>
	ten   || (NP NUM:'ten' NP*)   || <x,l1,<e,t>,[l1:[x|count(x,10)]],[],[],[]>
	
	one   || (NUM NUM:'one')   || <x,l1,e,[l1:[x|equal(x,1)]],[],[],[]>
	two   || (NUM NUM:'two')   || <x,l1,e,[l1:[x|equal(x,2)]],[],[],[]>
	three || (NUM NUM:'three') || <x,l1,e,[l1:[x|equal(x,3)]],[],[],[]>
	four  || (NUM NUM:'four')  || <x,l1,e,[l1:[x|equal(x,4)]],[],[],[]>
	five  || (NUM NUM:'five')  || <x,l1,e,[l1:[x|equal(x,5)]],[],[],[]>
	six   || (NUM NUM:'six')   || <x,l1,e,[l1:[x|equal(x,6)]],[],[],[]>
	seven || (NUM NUM:'seven') || <x,l1,e,[l1:[x|equal(x,7)]],[],[],[]>
	eight || (NUM NUM:'eight') || <x,l1,e,[l1:[x|equal(x,8)]],[],[],[]>
	nine  || (NUM NUM:'nine')  || <x,l1,e,[l1:[x|equal(x,9)]],[],[],[]>
	ten   || (NUM NUM:'ten')   || <x,l1,e,[l1:[x|equal(x,10)]],[],[],[]>