
// SEIN
// ----

	ist  || (S DP[subject] (VP V:'ist' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	ist  || (S DP[subject] (VP V:'ist' ADJ[comp]))   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	war  || (S DP[subject] (VP V:'war' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	war  || (S DP[subject] (VP V:'war' ADJ[comp]))   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	
	sind  || (S DP[subject] (VP V:'sind' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	sind  || (S DP[subject] (VP V:'sind' ADJ[comp]))   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	waren || (S DP[subject] (VP V:'waren' DP[object])) || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	waren || (S DP[subject] (VP V:'waren' ADJ[comp]))  || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>

	die .+ ist   || (NP NP* (S C:'die' (VP DP[object] V:'ist')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	der .+ ist   || (NP NP* (S C:'der' (VP DP[object] V:'ist')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	das .+ ist   || (NP NP* (S C:'das' (VP DP[object] V:'ist')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	die .+ war   || (NP NP* (S C:'die' (VP DP[object] V:'war')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	der .+ war   || (NP NP* (S C:'der' (VP DP[object] V:'war')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	das .+ war   || (NP NP* (S C:'das' (VP DP[object] V:'war')))   || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	die .+ sind  || (NP NP* (S C:'die' (VP DP[object] V:'sind')))  || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>
	die .+ waren || (NP NP* (S C:'die' (VP DP[object] V:'waren'))) || <x, l1, t, [ l1:[ | x=y ] ], [ (l2,y,object,<<e,t>,t>) ], [ l2=l1 ],[]>

	gibt es || (S V:'gibt' DP:'es' DP[dp])       || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>
        gibt es || (S DP[dp] (VP V:'gibt' DP:'es'))  || <x, l1, t, [ l1:[ | ] ], [ (l2,x,dp,<<e,t>,t>) ], [ l2=l1 ],[]>


// SEIN: YES/NO QUESTIONS

	ist   || (S (VP V:'ist' DP[subject] DP[object]))   || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	ist   || (S (VP V:'ist' DP[subject] ADJ[comp]))    || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	war   || (S V:'was' DP[subject] DP[object])  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	war   || (S V:'was' DP[subject] ADJ[comp])   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	sind  || (S V:'sind' DP[subject] DP[object])  || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	sind  || (S V:'sind' DP[subject] ADJ[comp])   || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
	waren || (S V:'waren' DP[subject] DP[object]) || <x, l1, t, [ l1:[ | ], l2:[ | x=y ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	waren || (S V:'waren' DP[subject] ADJ[comp])  || <x, l1, t, [ l1:[ | x=y ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>

	did || (S V:'did' S*) || <x,l1,t,[ l1:[|] ],[],[],[]>  

// IMPERATIVES
// ------------

	gib mir   || (S (VP V:'gib' (DP N:'mir') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	zeige mir || (S (VP V:'zeige' (DP N:'mir') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	zeig mir  || (S (VP V:'zeig' (DP N:'mir') DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	zeige     || (S (VP V:'zeige' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	zeig      || (S (VP V:'zeig' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
	liste .+ auf || (S (VP V:'liste' DP[object] V:'auf')) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
        finde || (S (VP V:'finde' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
        suche || (S (VP V:'suche' DP[object])) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
        suche .+ raus || (S (VP V:'suche' DP[object] V:'raus')) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>
        suche .+ zusammen || (S (VP V:'suche' DP[object] V:'zusammen')) || <x,l1,t,[ l1:[ ?x | x=y ] ],[ (l2,y,object,<<e,t>,t>) ],[ l1=l2 ],[]>



// DETERMINER
// ----------
	
	ein     || (DP DET:'ein' NP[noun])     || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	eine    || (DP DET:'eine' NP[noun])    || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	einen   || (DP DET:'einen' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	einem   || (DP DET:'einem' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	einige  || (DP DET:'einige' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	einigen || (DP DET:'einigen' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] SOME y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	alle    || (DP DET:'alle' NP[noun])    || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	allen   || (DP DET:'allen' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	all die || (DP DET:'all' DET:'die' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	all den || (DP DET:'all' DET:'den' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	jede    || (DP DET:'jede' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	jeder   || (DP DET:'jeder' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	jedes   || (DP DET:'jedes' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	jedem   || (DP DET:'jedem' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	jeden   || (DP DET:'jeden' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] EVERY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	kein    || (DP DET:'kein' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] NO y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	keine   || (DP DET:'keine' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] NO y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	keinem  || (DP DET:'keinem' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] NO y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	keinen  || (DP DET:'keinen' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] NO y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	die meisten   || (DP DET:'die' ADJ:'meisten' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	den meisten   || (DP DET:'den' ADJ:'meisten' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	die wenigsten || (DP DET:'die' ADJ:'wenigsten' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	den wenigsten || (DP DET:'den' ADJ:'wenigsten' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	wenige  || (DP DET:'wenig' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] AFEW y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	wenig   || (DP DET:'wenig' NP[noun])   || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] AFEW y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	manche  || (DP DET:'manche' NP[noun])  || <x, l1, <<e,t>,t>, [ l1:[ x | ] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	welche  || (DP DET:'welche' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	welcher || (DP DET:'welcher' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	welches || (DP DET:'welches' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	welchen || (DP DET:'welchen' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	welchem || (DP DET:'welchem' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	was fuer || (DP DET:'was' DET:'fuer' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	was fuer ein  || (DP DET:'was' DET:'fuer' DET:'ein' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	was fuer eine || (DP DET:'was' DET:'fuer' DET:'eine' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ ?y | ] ], [ (l2,y,noun,<e,t>) ], [ l2=l1 ],[]>
	viele   || (DP DET:'viele' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] MANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	vielen  || (DP DET:'vielen' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] MANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	viel    || (DP DET:'viel' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] MANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[]>
	der     || (DP DET:'der' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	die     || (DP DET:'die' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	das     || (DP DET:'das' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	dem     || (DP DET:'dem' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	den     || (DP DET:'den' NP[noun]) || <x, l1, <<e,t>,t>, [ l1:[x|] ], [ (l2,x,noun,<e,t>) ], [ l2=l1 ],[]>
	mindestens  || (DP DET:'mindestens' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), greaterorequal(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | greaterorequal(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
	hoechstens  || (DP DET:'hoechstens' NUM[num] NP[noun]) ||  <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), lessorequal(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | lessorequal(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
	mehr als    || (DP DET:'mehr' DET:'als' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), greater(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | greater(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
	weniger als || (DP DET:'weniger' DET:'als' NUM[num] NP[noun]) ||  <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), less(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | less(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
	genau       || (DP DET:'genau' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), equal(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | equal(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
	insgesamt   || (DP DET:'insgesamt' NUM[num] NP[noun]) || <y,l1,<<e,t>,t>,[ l1:[ y,c | count(y,c), equal(c,x) ] ],[(l2,y,noun,<e,t>),(l3,x,num,e)],[l2=l1,l3=l1],[ SLOT_arg/RESOURCE/y ]> ;; <y,l1,<<e,t>,t>,[l1:[ y | equal(y,x) ]],[(l2,y,noun,<e,t>),(l3,x,num,e)],[ l1=l2, l2=l3 ],[ SLOT_arg/LITERAL/y ]>
        // also sum(a,x,s) ? 

	andere || (NP ADJ:'other' NP*) || <x,l1,<e,t>,[ l1:[ | ] ], [],[],[]>
	andere || (NP ADJ:'other' NP*) || <x,l1,<e,t>,[ l1:[ | ] ], [],[],[]>
	andere || (NP ADJ:'other' NP*) || <x,l1,<e,t>,[ l1:[ | ] ], [],[],[]>
	
	die wenigsten || (ADJ DET:'die' DET:'wenigsten' ADJ*) || <x,l1,<e,t>,[ l1:[ | minimum(a,x,x) ] ], [],[],[]>
	den wenigsten || (ADJ DET:'den' DET:'wenigsten' ADJ*) || <x,l1,<e,t>,[ l1:[ | minimum(a,x,x) ] ], [],[],[]>	

  	wieviele || (DET DET:'wieviele') || <x,l1,e, [ l1:[ ?x | ] ], [],[],[ SLOT_arg/DATATYPEPROPERTY/x ]>
  	wieviel  || (DET DET:'wieviel')  || <x,l1,e, [ l1:[ ?c,x | count(a,x,c) ] ], [],[],[ SLOT_arg/OBJECTPROPERTY_CLASS/x ]>
	ein      || (DET DET:'ein')      || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	eine     || (DET DET:'eine')     || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	welche   || (DET DET:'welche')   || <x,l1,e, [ l1:[ ?x |] ], [],[],[]>
	welcher  || (DET DET:'welcher')  || <x,l1,e, [ l1:[ ?x |] ], [],[],[]>
	welches  || (DET DET:'welches')  || <x,l1,e, [ l1:[ ?x |] ], [],[],[]>

	der || (DET DET:'der') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	die || (DET DET:'die') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	das || (DET DET:'das') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	den || (DET DET:'den') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	dam || (DET DET:'dem') || <x,l1,e, [ l1:[ x |] ], [],[],[]>
	die meisten   || (DET DET:'die' DET:'meisten') || <y, l1, e, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [], [],[]>
	den meisten   || (DET DET:'den' DET:'meisten') || <y, l1, e, [ l1:[ | l2:[ y | ] THEMOST y l3:[|] ] ], [], [],[]>
	die wenigsten || (DET DET:'die' DET:'wenigsten') || <y, l1, e, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [], [],[]>
	den wenigsten || (DET DET:'den' DET:'wenigsten') || <y, l1, e, [ l1:[ | l2:[ y | ] THELEAST y l3:[|] ] ], [], [],[]>


    // NECESSARY "CHEAT"
	hoechste  || (NP ADJ:'hoechste' NP*)  || <x, l1, e, [ l1:[ | maximum(x) ] ], [], [],[]> ;; <x, l1, e, [ l1:[ j | SLOT_high(x,j), maximum(j) ] ],[],[],[ SLOT_high/DATATYPEPROPERTY/height ]>
	hoechsten || (NP ADJ:'hoechsten' NP*) || <x, l1, e, [ l1:[ | maximum(x) ] ], [], [],[]> ;; <x, l1, e, [ l1:[ j | SLOT_high(x,j), maximum(j) ] ],[],[],[ SLOT_high/DATATYPEPROPERTY/height ]>

	// WIE
	// wie || (DP DET:'wie' ADJ[adj]) || <x,l1,<<e,t>,t>,[ l1:[?x,|] ],[ (x,l2,adj,<e,t>) ],[l2=l1],[]>

	
// EMPTY STUFF 
// ------------

	auch || (VP ADV:'auch' VP*) || <x,l1,t,[ l1:[|] ],[],[],[]>
	auch || (DP ADV:'auch' DP*) || <x,l1,<<e,t>,t>,[ l1:[|] ],[],[],[]>
	
	hat    || (S DP[subject] (VP V:'hat' DP[object]))    || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	haben  || (S DP[subject] (VP V:'haben' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
	hatte  || (S DP[subject] (VP V:'hatte' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
        hatten || (S DP[subject] (VP V:'hatten' DP[object])) || <x, l1, t, [ l1:[ | ], l2:[ | empty(x,y) ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>

//	mit || (NP NP* (PP P:'mit' DP[dp])) || <x,l1,<e,t>,[ l1:[| empty(x,y) ] ],[(l2,y,dp,<<e,t>,t>)],[l2=l1],[]>

//	people || (NP N:'people') || <x,l1,<e,t>,[ l1:[|] ],[],[],[]>
	

// WH WORDS
// --------

	was || (DP WH:'was') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	wer || (DP WH:'wer') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	wen || (DP WH:'wen') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
	wem || (DP WH:'wem') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>

//	welche   || (DP WH:'welche') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
//	welches  || (DP WH:'welche') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>
//	welcher  || (DP WH:'welche') || <x, l1, <<e,t>,t>, [ l1:[ ?x | ] ], [], [], []>

	wieviel  || (DP WH:'wieviel' NP[noun])  || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] HOWMANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[  SLOT_arg/RESOURCE/y ]>
	wieviele || (DP WH:'wieviele' NP[noun]) || <y, l1, <<e,t>,t>, [ l1:[ | l2:[ y | ] HOWMANY y l3:[|] ] ], [ (l4,y,noun,<e,t>) ], [ l4=l2 ],[  SLOT_arg/RESOURCE/y ]>
	
	wann  || (S WH:'wann' S[s])  || <x, l1, t, [ l1:[ ?x | SLOT_p(y,x) ] ], [(l2,y,s,t)], [l2=l1], [ SLOT_p/PROPERTY/date ]> 
	wann  || (DP WH:'wann')      || <y, l1, <<e,t>,t>, [ l1:[ ?x | SLOT_p(y,x) ] ], [], [], [ SLOT_p/PROPERTY/date ]> 
	wo    || (S WH:'wo' S[s]) || <x, l1, t, [ l1:[ ?x | SLOT_p(y,x) ] ], [(l2,y,s,t)], [l2=l1], [ SLOT_p/PROPERTY/place ]>
	wo    || (DP WH:'wo')     || <y, l1, <<e,t>,t>, [ l1:[ ?x | SLOT_p(y,x) ] ], [], [], [ SLOT_p/PROPERTY/place ]> 
	wo in || (DP WH:'wo' (PP P:'in' DP[dp])) || <y, l1, <<e,t>,t>, [ l1:[ ?x | SLOT_p(y,x), SLOT_in(x,z) ] ], [(l2,z,dp,<<e,t>,t>)], [l2=l1], [ SLOT_p/PROPERTY/place ]> 
	
	
// NEGATION 
// --------

        nicht || (ADJ NEG:'nicht' ADJ*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
        nicht || (VP NEG:'nicht' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
        hat nicht || (VP V:'hat' NEG:'nicht' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
        haben nicht || (VP V:'haben' NEG:'nicht' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
        hatte nicht || (VP V:'hatte' NEG:'nicht' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>	
        hatten nicht || (VP V:'hatten' NEG:'nicht' VP*) || <x,l2,t,[ l1:[ | NOT l2:[|] ] ],[],[],[]>		

        ist nicht   || (S DP[subject] (VP V:'ist' NEG:'nicht' DP[object]))   || <x, l1, t, [ l1:[ | ], l2:[ | NOT [ | x=y ] ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
        ist nicht   || (S DP[subject] (VP V:'ist' NEG:'nicht' ADJ[comp]))    || <x, l1, t, [ l1:[ | NOT [ | x=y ] ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
        war nicht   || (S DP[subject] (VP V:'war' NEG:'nicht' DP[object]))   || <x, l1, t, [ l1:[ | ], l2:[ | NOT [ | x=y ] ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
        war nicht   || (S DP[subject] (VP V:'war' NEG:'nicht' ADJ[comp]))    || <x, l1, t, [ l1:[ | NOT [ | x=y ] ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
        sind nicht  || (S DP[subject] (VP V:'sind' NEG:'nicht' DP[object]))  || <x, l1, t, [ l1:[ | ], l2:[ | NOT [ | x=y ] ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
        sind nicht  || (S DP[subject] (VP V:'sind' NEG:'nicht' ADJ[comp]))   || <x, l1, t, [ l1:[ | NOT [ | x=y ] ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>
        waren nicht || (S DP[subject] (VP V:'waren' NEG:'nicht' DP[object])) || <x, l1, t, [ l1:[ | ], l2:[ | NOT [ | x=y ] ] ], [ (l3,x,subject,<<e,t>,t>), (l4,y,object,<<e,t>,t>) ], [  l3<l1, l4<l1, l2<scope(l3), l2<scope(l4) ],[]>
        waren nicht || (S DP[subject] (VP V:'waren' NEG:'nicht' ADJ[comp]))  || <x, l1, t, [ l1:[ | NOT [ | x=y ] ]], [ (l2,x,subject,<<e,t>,t>), (l3,y,comp,<e,t>) ], [  l2=l1, l3=l2 ],[]>


// COORDINATION
// ------------

	und || (S S* CC:'und' S[s]) || <x,l1,t,[l1:[|]],[(l2,y,s,t)],[l1=l2],[]>
	und || (DP DP* CC:'und' DP[dp]) || <x,l1,<<e,t>,t>,[l1:[|]],[(l2,y,dp,<<e,t>,t>)],[l1=l2],[]>
	und || (NP NP* CC:'und' NP[np]) || <x,l1,<e,t>,[l1:[|x=y]],[(l2,y,np,<e,t>)],[l1=l2],[]>
	und || (PP PP* CC:'und' PP[pp]) || <x,l1,<e,t>,[l1:[|x=y]],[(l2,y,pp,<e,t>)],[l1=l2],[]>
	und || (ADJ ADJ* CC:'und' ADJ[adj]) || <x,l1,<e,t>,[l1:[|]],[(l2,y,adj,<e,t>)],[l1=l2],[]>

        aber || (S S* CC:'aber' S[s]) || <x,l1,t,[l1:[|]],[(l2,y,s,t)],[l1=l2],[]>
	aber || (DP DP* CC:'aber' DP[dp]) || <x,l1,<<e,t>,t>,[l1:[|]],[(l2,y,dp,<<e,t>,t>)],[l1=l2],[]>
	aber || (NP NP* CC:'aber' NP[np]) || <x,l1,<e,t>,[l1:[|x=y]],[(l2,y,np,<e,t>)],[l1=l2],[]>
	aber || (PP PP* CC:'aber' PP[pp]) || <x,l1,<e,t>,[l1:[|x=y]],[(l2,y,pp,<e,t>)],[l1=l2],[]>
	aber || (ADJ ADJ* CC:'aber' ADJ[adj]) || <x,l1,<e,t>,[l1:[|]],[(l2,y,adj,<e,t>)],[l1=l2],[]>
	
	sowohl .+ als auch || (NP CC:'sowohl' NP* CC:'als' CC:'auch' NP[np]) || <x,l1,<e,t>,[l1:[|]],[(l2,y,np,<e,t>)],[l1=l2],[]>
	
	oder || (S S[cc1] CC:'oder' S[cc2])    || <x, l1, t, [ l1:[ | l2:[|] OR l3:[|] ] ], [ (l5,x,cc1,t),(l4,y,cc2,t) ], [ l5=l2,l4=l3 ],[]>
	oder || (DP DP[cc1] CC:'oder' DP[cc2]) || <x, l1, <<e,t>,t>, [ l1:[ | l2:[|] OR l3:[|], x=y ] ], [ (l5,x,cc1,<<e,t>,t>),(l4,y,cc2,<<e,t>,t>) ], [ l5=l2,l4=l3 ],[]>
	oder || (NP NP[cc1] CC:'oder' NP[cc2]) || <x, l1, <e,t>, [ l1:[ | l2:[|] OR l3:[|], x=y ] ], [ (l5,x,cc1,<e,t>),(l4,y,cc2,<e,t>) ], [ l5=l2,l4=l3 ],[]>
	oder || (ADJ ADJ* CC:'oder' ADJ[cc])   || -
	

// EXPLETIVE
// ----------

	es || (DP NP:'es') || <x,l1,<<e,t>,t>,[l1:[|]],[],[],[]>


// NUMBERS (1-20)
// ---------------
	
	eins      || (NP NUM:'eins' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,1)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,1)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwei      || (NP NUM:'zwei' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,2)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,2)]],[],[],[ SLOT_arg/LITERAL/x ]>
	drei      || (NP NUM:'drei' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,3)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,3)]],[],[],[ SLOT_arg/LITERAL/x ]>
	vier      || (NP NUM:'vier' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,4)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,4)]],[],[],[ SLOT_arg/LITERAL/x ]>
	fuenf     || (NP NUM:'fuenf' NP*)     || <x,l1,<e,t>,[l1:[x|count(x,5)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,5)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sechs     || (NP NUM:'sechs' NP*)     || <x,l1,<e,t>,[l1:[x|count(x,6)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,6)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sieben    || (NP NUM:'sieben' NP*)    || <x,l1,<e,t>,[l1:[x|count(x,7)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,7)]],[],[],[ SLOT_arg/LITERAL/x ]>
	acht      || (NP NUM:'acht' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,8)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,8)]],[],[],[ SLOT_arg/LITERAL/x ]>
	neun      || (NP NUM:'neun' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,9)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,9)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zehn      || (NP NUM:'zehn' NP*)      || <x,l1,<e,t>,[l1:[x|count(x,10)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,10)]],[],[],[ SLOT_arg/LITERAL/x ]>
	elf       || (NP NUM:'elf' NP*)       || <x,l1,<e,t>,[l1:[x|count(x,11)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,11)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwoelf    || (NP NUM:'zwoelf' NP*)    || <x,l1,<e,t>,[l1:[x|count(x,12)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,12)]],[],[],[ SLOT_arg/LITERAL/x ]>
	dreizehn  || (NP NUM:'dreizehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,13)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,13)]],[],[],[ SLOT_arg/LITERAL/x ]>
	vierzehn  || (NP NUM:'vierzehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,14)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,14)]],[],[],[ SLOT_arg/LITERAL/x ]>
	fuenfzehn || (NP NUM:'fuenfzehn' NP*) || <x,l1,<e,t>,[l1:[x|count(x,15)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,15)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sechzehn  || (NP NUM:'sechzehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,16)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,16)]],[],[],[ SLOT_arg/LITERAL/x ]>
	siebzehn  || (NP NUM:'siebzehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,17)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,17)]],[],[],[ SLOT_arg/LITERAL/x ]>
	achtzehn  || (NP NUM:'achtzehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,18)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,18)]],[],[],[ SLOT_arg/LITERAL/x ]>
	neunzehn  || (NP NUM:'neunzehn' NP*)  || <x,l1,<e,t>,[l1:[x|count(x,19)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,19)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwanzig   || (NP NUM:'zwanzig' NP*)   || <x,l1,<e,t>,[l1:[x|count(x,20)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,<e,t>,[l1:[x|equal(x,10)]],[],[],[ SLOT_arg/LITERAL/x ]>
	
	eins      || (NUM NUM:'eins')      || <x,l1,e,[l1:[x|count(x,1)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,1)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwei      || (NUM NUM:'zwei')      || <x,l1,e,[l1:[x|count(x,2)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,2)]],[],[],[ SLOT_arg/LITERAL/x ]>
	drei      || (NUM NUM:'drei')      || <x,l1,e,[l1:[x|count(x,3)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,3)]],[],[],[ SLOT_arg/LITERAL/x ]>
	vier      || (NUM NUM:'vier')      || <x,l1,e,[l1:[x|count(x,4)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,4)]],[],[],[ SLOT_arg/LITERAL/x ]>
	fuenf     || (NUM NUM:'fuenf')     || <x,l1,e,[l1:[x|count(x,5)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,5)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sechs     || (NUM NUM:'sechs')     || <x,l1,e,[l1:[x|count(x,6)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,6)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sieben    || (NUM NUM:'sieben')    || <x,l1,e,[l1:[x|count(x,7)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,7)]],[],[],[ SLOT_arg/LITERAL/x ]>
	acht      || (NUM NUM:'acht')      || <x,l1,e,[l1:[x|count(x,8)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,8)]],[],[],[ SLOT_arg/LITERAL/x ]>
	neun      || (NUM NUM:'neun')      || <x,l1,e,[l1:[x|count(x,9)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,9)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zehn      || (NUM NUM:'zehn')      || <x,l1,e,[l1:[x|count(x,10)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,10)]],[],[],[ SLOT_arg/LITERAL/x ]>
        elf       || (NUM NUM:'elf')       || <x,l1,e,[l1:[x|count(x,11)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,11)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwoelf    || (NUM NUM:'zwoelf')    || <x,l1,e,[l1:[x|count(x,12)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,12)]],[],[],[ SLOT_arg/LITERAL/x ]>
	dreizehn  || (NUM NUM:'dreizehn')  || <x,l1,e,[l1:[x|count(x,13)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,13)]],[],[],[ SLOT_arg/LITERAL/x ]>
	vierzehn  || (NUM NUM:'vierzehn')  || <x,l1,e,[l1:[x|count(x,14)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,14)]],[],[],[ SLOT_arg/LITERAL/x ]>
	fuenfzehn || (NUM NUM:'fuenfzehn') || <x,l1,e,[l1:[x|count(x,15)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,15)]],[],[],[ SLOT_arg/LITERAL/x ]>
	sechzehn  || (NUM NUM:'sechzehn')  || <x,l1,e,[l1:[x|count(x,16)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,16)]],[],[],[ SLOT_arg/LITERAL/x ]>
	siebzehn  || (NUM NUM:'siebzehn')  || <x,l1,e,[l1:[x|count(x,17)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,17)]],[],[],[ SLOT_arg/LITERAL/x ]>
	achtzehn  || (NUM NUM:'achtzehn')  || <x,l1,e,[l1:[x|count(x,18)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,18)]],[],[],[ SLOT_arg/LITERAL/x ]>
	neunzehn  || (NUM NUM:'neunzehn')  || <x,l1,e,[l1:[x|count(x,19)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,19)]],[],[],[ SLOT_arg/LITERAL/x ]>
	zwanzig   || (NUM NUM:'zwanzig')   || <x,l1,e,[l1:[x|count(x,20)]],[],[],[ SLOT_arg/RESOURCE/x ]> ;; <x,l1,e,[l1:[x|equal(x,20)]],[],[],[ SLOT_arg/LITERAL/x ]>

