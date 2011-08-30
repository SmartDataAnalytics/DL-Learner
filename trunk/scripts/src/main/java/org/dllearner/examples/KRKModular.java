package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.statistics.SimpleClock;
import org.semanticweb.owlapi.model.IRI;

/*
 * Structure
 * 
 * 
 * 
 * 
 * */

public class KRKModular {

	// REMEMBER
	// FILES are letters
	// RANKS are numbers

	// FLAGS
	// 
	// turn off to not write the owl, needs about 30 seconds or more
	/*static boolean writeOWL = true;
	static boolean writeKB = false;
	static boolean useTransitivity = false;
	
	static boolean writeExampleSets = true;
	static boolean writeConciseOWLAllDifferent = false;
	*/
	boolean useHigherThan = KRKOntologyTBox.useHigherThan;
	
	

	//static boolean useInverse = false;
	// dependent, love and marriage, horse and carriage
	//static boolean useTripleSubProps = useInverse && false;
	
	
	static String workingDir = "examples/krkrecognizer/";
	static String allData = workingDir+"krkopt_no_draw.data";
	//static String allData = workingDir+"krkopt_original_dataset.data";
	
	static IRI ontologyURI = IRI.create(KRKOntologyTBox.ontologyURI);
	
	// static SortedSet<String> fileSet = new TreeSet<String>();
	//static SortedSet<String> allInstances = new TreeSet<String>();
	//static SortedSet<String> classSet = new TreeSet<String>();
	//static SortedSet<String> symmetricRoleSet = new TreeSet<String>();

	static HashMap<String, SortedSet<Individual>> classToInd = new HashMap<String, SortedSet<Individual>>();
	static HashMap<Individual, String> indToClass = new HashMap<Individual, String>();
	
	static Set<AbstractReasonerComponent> allReasoners =  new HashSet<AbstractReasonerComponent>();
	static int negativeExamplesAdded = 200;
	
	// static LinkedList<String> words;
	public KB kb;
	public AbstractReasonerComponent reasoner;
	
	
	//public FastInstanceChecker fic;
	// static HashMap<String,SortedSet<String>> classToInd;
	
	
	public static void init(){
		if (!new File(workingDir).exists()) {
			System.out.println("Created directory: " + workingDir + 
					" : " + new File(workingDir).mkdir()+ ".");
		}
	}
	
	public static void main(String[] args) {
		main1(args);
		//main2(args);
		
	}
	
	
	public static void main1(String[] args) {
		init();
		initAllInstancesWithoutReasoners();
		System.out.println("initializing finished");
		//String currentClass = "ZERO"; 
		LinkedList<String> ll=new LinkedList<String>();
		
		ll.add("DRAW");
		ll.add("ZERO");
		ll.add("ONE");
		ll.add("TWO");
		ll.add("THREE");
		ll.add("FOUR");
		ll.add("FIVE");
		ll.add("SIX");
		ll.add("SEVEN");
		ll.add("EIGHT");
		ll.add("NINE");
		ll.add("TEN");
		ll.add("ELEVEN");
		ll.add("TWELVE");
		ll.add("THIRTEEN");
		ll.add("FOURTEEN");
		ll.add("FIFTEEN");
		ll.add("SIXTEEN");
		
		String skript="";
		
		for (int i = 0; i < ll.size(); i++) {
			System.out.println("progress "+i+" of "+ll.size());
			String currentClass=ll.get(i);
			SortedSet<Individual> allPos = classToInd.get(currentClass);
			if(classToInd.get(currentClass)==null)continue;
			if(currentClass.equals("SIXTEEN"))continue;
			classToInd.remove(currentClass);
			SortedSet<Individual> neg =   new TreeSet<Individual>();
			for (SortedSet<Individual> set : classToInd.values()) {
				neg.addAll(set);
			}
			SortedSet<Integer> lines = getLines(allPos, neg);
			KB kb = getKB(lines);
			KRKModular km=new KRKModular(kb);
			//starting reasone
			//km.initReasonerFact();
			String filename="";
			if(i==0)  filename="KRK_recognizerDRAW";
			else filename="KRK_recognizer"+(i-1);
			km.writeOWLFile(filename+".owl");
			
			StringBuffer buf= new StringBuffer();
			buf.append("\nimport(\""+filename+".owl"+"\");\n\n");
			
			buf.append("refexamples.ignoredConcepts={\n");
			buf.append("\""+ontologyURI+"#"+currentClass+"\"");
			for (String str : classToInd.keySet()) {
				buf.append(",\n");
				buf.append("\""+ontologyURI+"#"+str+"\"");
			}
			
			buf.append("};\n\n");
			buf.append("algorithm = refexamples;\n"+
					"reasoner=fastInstanceChecker;\n"+
					"refexamples.useAllConstructor = false;\n"+
					"refexamples.useExistsConstructor = true;\n"+
					"refexamples.useCardinalityRestrictions = false;\n"+
					"refexamples.useNegation = false;\n\n\n");
			
			for (Individual ind : allPos) {
				buf.append("+\""+ind+"\"\n");
			}
			buf.append("\n\n\n");
			for (Individual ind : neg) {
				buf.append("-\""+ind+"\"\n");
			}
			
			writeToFile(workingDir+filename+".conf", buf.toString());
			skript+= "./dllearner "+workingDir+filename+".conf >> "+workingDir+filename+"result.txt\n";
		}
		System.out.println(skript);
		writeToFile(workingDir+"skript.sh", skript);
		
	
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main2(String[] args) {
		init();
		initAllInstancesAndReasoners();
		System.out.println("initializing finished");
		String currentClass = "ZERO"; 
		SortedSet<Individual> allPos = classToInd.get(currentClass);
		
		
		//if(allPos.size()<400)negativeExamplesAdded = allPos.size();
		//else negativeExamplesAdded = 400;
		SortedSet<Individual> tmp =   new TreeSet<Individual>();
		for (Individual individual : indToClass.keySet()) {
			tmp.add(individual);
		}
	
		SortedSet<Individual> neg = getNegativeExamples(currentClass, tmp, negativeExamplesAdded);
		SortedSet<Integer> lines = getLines(allPos, neg);
		KB kb = getKB(lines);
		Description d=null;
		try{
		d = KBParser.parseConcept("EXISTS \"http://dl-learner.org/krk#hasPiece\".(EXISTS \"http://dl-learner.org/krk#hasLowerRankThan\".(EXISTS \"http://dl-learner.org/krk#fileDistanceLessThan1\".(\"http://dl-learner.org/krk#BKing\" AND EXISTS \"http://dl-learner.org/krk#rankDistanceLessThan2\".(\"http://dl-learner.org/krk#WKing\" OR EXISTS \"http://dl-learner.org/krk#rankDistanceLessThan1\".EXISTS \"http://dl-learner.org/krk#rankDistanceLessThan3\".\"http://dl-learner.org/krk#WKing\")) AND (\"http://dl-learner.org/krk#FileA\" OR \"http://dl-learner.org/krk#WKing\")) AND (\"http://dl-learner.org/krk#FileC\" OR (\"http://dl-learner.org/krk#BKing\" AND \"http://dl-learner.org/krk#FileD\")))");
		}catch (Exception e) {e.printStackTrace();}
		
		
		
		while (true){
			
			 
			 SortedSet<Individual>  result = retrieveAll(d);
			 System.out.println(result);
			System.out.println("still left: " + (result.size()-allPos.size()));
			if(verify(currentClass, result)) {
				System.out.println("Correct solution: "+ d.toKBSyntaxString(ontologyURI+"#", null));
				break;}
			neg.addAll(getNegativeExamples(currentClass, result, negativeExamplesAdded));
			lines = getLines(allPos, neg);
			kb = getKB(lines);
			d= learn(kb, allPos, neg);
			
		}
	
		
	}
	
	static Description learn(KB kb, SortedSet<Individual> pos,SortedSet<Individual> neg){
		
		System.out.println(pos);
		System.out.println(neg);
		
		ComponentManager cm = ComponentManager.getInstance();
		AbstractCELA la = null;
        try {
            Set<AbstractKnowledgeSource> sources = new HashSet<AbstractKnowledgeSource>();
            sources.add(new KBFile(kb));
            FastInstanceChecker r = new FastInstanceChecker();
            r.setSources(sources);

            r.init();
//		ReasonerComponent rs = new ReasonerComponent(r); 

            //cm.learningProblem(lpClass, reasoner)
            PosNegLPStandard lp = new PosNegLPStandard();
            lp.setReasoner(r);
            //cm.getConfigOptionValue(lp, "");
            cm.applyConfigEntry(lp, "positiveExamples", pos);
            cm.applyConfigEntry(lp, "negativeExamples", neg);

            lp.init();

            la = cm.learningAlgorithm(OCEL.class, lp, r);
            SortedSet<String> ignoredConcepts = getIgnoredConcepts(pos, neg);

            cm.applyConfigEntry(la, "useAllConstructor", false);
            cm.applyConfigEntry(la, "useExistsConstructor", true);
            cm.applyConfigEntry(la, "useCardinalityRestrictions", false);
            cm.applyConfigEntry(la, "useNegation", false);
            //cm.applyConfigEntry(la,"quiet",false);
            cm.applyConfigEntry(la, "ignoredConcepts", ignoredConcepts);

            la.init();

            System.out.println("start learning");

            la.start();
            //System.out.println("best"+la.getBestSolution());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return la.getCurrentlyBestDescription();
    }
	
	static KB getKB(SortedSet<Integer> lines){
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(allData));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		KRKModular km = new KRKModular();
		try {
			String line = "";
			int x = 0;
			while ((line = in.readLine()) != null) {
				Integer tmp = new Integer(x);
				if(lines.contains(tmp))
					km.addOneLineToKB(x, line);
				x++;
			}// endWhile
		}catch (Exception e) {e.printStackTrace();}
		return km.kb;
		
	}
	
	
	static SortedSet<Integer> getLines(SortedSet<Individual> pos,SortedSet<Individual> neg){
		SortedSet<Integer> ret =  new TreeSet<Integer>();
		
		for (Individual individual : pos) {
			int a = Integer.parseInt(individual.getName().substring((ontologyURI+"#g").length()));
			ret.add(new Integer(a));
		}
		for (Individual individual : neg) {
			int a = Integer.parseInt(individual.getName().substring((ontologyURI+"#g").length()));
			ret.add(new Integer(a));
		}
		return ret;
		
	}
	
	static SortedSet<String> getIgnoredConcepts(SortedSet<Individual> pos,SortedSet<Individual> neg){
		SortedSet<String> ret =  new TreeSet<String>();
		
		for (Individual individual : pos) {
			String tmp = indToClass.get(individual);
			//System.out.println("pos"+tmp+individual);
			ret.add(ontologyURI+"#"+tmp);
		}
		for (Individual individual : neg) {
			//String tmp = indToClass.get((Individual)individual);
			//System.out.println("neg"+tmp+individual);
			for (Individual string : indToClass.keySet()) {
				if(string.toString().equals(individual.getName())){
					ret.add(ontologyURI+"#"+indToClass.get(string));
					//System.out.println("aaaaaaaa"+individual.getName());
				}
				
			}
			
		}
		
		//System.out.println(indToClass);
		return ret;
		
	}
		
	public  KRKModular() {
		this.kb = makeOntologyTBox();
	}
	public  KRKModular(KB kb) {
		this.kb = kb;
	}
	
	public  KRKModular(String concept) {
		this.kb = makeOntologyTBox(concept);
	}
	
	public static void initAllInstancesAndReasoners(){
		// Datei oeffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(allData));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		SimpleClock sc= new SimpleClock();
		KRKModular km =null;
		km = new KRKModular();
		try {
			String line = "";
			int x = 0;
			while ((line = in.readLine()) != null) {
				if (x % 1000 == 0)
					{sc.print("Currently at line " + x+" : ");}
				km.addOneLineToKBinit(x, line);
				if(x%1000==0 && x!=0){
					km.initReasonerFact();
					allReasoners.add(km.reasoner);
					km = new KRKModular();
				}
				//if(x==200)break;
				x++;
			}// endWhile
			
			km.initReasonerFact();
			allReasoners.add(km.reasoner);
			km.writeOWLFile();
			km = null;
			
			
			sc.printAndSet("initialization finished");
			
			//sc.printAndSet("nach Retrieval");
			//km.writeOWLFile();
			/*SortedSet<Individual> s = new TreeSet<Individual>(); 
			for (int i = 0; i < x; i++) {
				s.add(getIndividual("g"+i));
				//if(km.check2(i))howmany++;
			}*/
			//writeExampleSet(s);
			
			
			//sc.printAndSet("before ");
			//km.initReasonerFact();
			//sc.printAndSet("after initfact");
			
			//SortedSet<Individual> ret = km.checkAllOWLAPI(concept, s);
			//sc.print("totalfic ("+ret.size()+") for 1000: ");
		
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void initAllInstancesWithoutReasoners(){
		// Datei oeffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(allData));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		SimpleClock sc= new SimpleClock();
		KRKModular km =null;
		km = new KRKModular();
		try {
			String line = "";
			int x = 0;
			while ((line = in.readLine()) != null) {
				if (x % 1000 == 0)
					{sc.print("Currently at line " + x+" : ");}
				km.addOneLineToKBinit(x, line);
				
				//if(x==200)break;
				x++;
			}// endWhile
			km = null;
			sc.printAndSet("initialization finished");
		
		
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static SortedSet<Individual> retrieveAll(String concept){
		Description d = null;
		try{
		d = KBParser.parseConcept(concept);
		}catch (Exception e) {e.printStackTrace();}
		return retrieveAll(d);
	}
	
	public static SortedSet<Individual> retrieveAll(Description d){
		SortedSet<Individual> ret = new TreeSet<Individual>(); 
		try{
			
			for (AbstractReasonerComponent onereasoner : allReasoners) {
				ret.addAll(onereasoner.getIndividuals(d));
			}
			
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	}
	
	
	
	
	
	public void initReasonerFact(){
		KBFile kbFile = new KBFile(this.kb);
		Set<AbstractKnowledgeSource> ks = new HashSet<AbstractKnowledgeSource>();
		ks.add(kbFile);
		
		reasoner = new OWLAPIReasoner(ks);
		
		((OWLAPIReasoner)reasoner).setReasonerTypeString("fact");
		try{
			reasoner.init();
		}catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	public void initFIC(){
		KBFile kbFile = new KBFile(this.kb);
		Set<AbstractKnowledgeSource> ks = new HashSet<AbstractKnowledgeSource>();
		ks.add(kbFile);
		//System.out.println("blabla");
		reasoner = new FastInstanceChecker();
        reasoner.setSources(ks);
		//fic.setReasonerType("fact");
		try{
		reasoner.init();
		}catch (Exception e) {e.printStackTrace();}
	}
	
	public KB makeOntologyTBox(){
		
		KRKOntologyTBox tbox = new KRKOntologyTBox();
		return tbox.getKb();
		
	}
	
	public KB makeOntologyTBox(String concept){
		
		KRKOntologyTBox tbox = new KRKOntologyTBox();
		tbox.addConcept(concept);		
		return tbox.getKb();
		
	}
	
	
	public void addOneLineToKBinit(int x, String line){
		
			String[] ar = new String[6];
			Individual gameind = KRKOntologyTBox.getIndividual("g" + x);
			ar = tokenize(line);
			addToHM(ar[6], gameind);
			addOneLineToKB( x,  line);
		
		
	}
	
	public void addOneLineToKB(int x, String line){
		
			Individual gameind;
			Individual wkingind;
			Individual wrookind;
			Individual bkingind;
			
			String[] ar = new String[6];
			
			ar = tokenize(line);
	
			//String currentclass = ar[6];
			
			gameind = KRKOntologyTBox.getIndividual("g" + x);
			wkingind = KRKOntologyTBox.getIndividual("wking_" + ar[0] + ar[1] + "_" + x);
			wrookind = KRKOntologyTBox.getIndividual("wrook_" + ar[2] + ar[3] + "_" + x);
			bkingind = KRKOntologyTBox.getIndividual("bking_" + ar[4] + ar[5] + "_" + x);
			
			
			
			//allInstances.add(gameind+"");
			//allInstances.add(wkingind+"");
			//allInstances.add(wrookind+"");
			//allInstances.add(bkingind+"");
	
			// save it for examplegeneration
			
			// .add(gameind.getName());
			//classSet.add(ar[6]);
	
			// CLASSES
			kb.addABoxAxiom(new ClassAssertionAxiom(KRKOntologyTBox.Game, gameind));
			kb.addABoxAxiom(new ClassAssertionAxiom(
					getAtomicConcept(ar[6]), gameind));
			kb.addABoxAxiom(new ClassAssertionAxiom(KRKOntologyTBox.WKing, wkingind));
			kb.addABoxAxiom(new ClassAssertionAxiom(KRKOntologyTBox.WRook, wrookind));
			kb.addABoxAxiom(new ClassAssertionAxiom(KRKOntologyTBox.BKing, bkingind));
	
			
			/**Files and Ranks***/
			// FILES are letters
			// RANKS are numbers
			
			//WKing
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("File"+ar[0].toUpperCase()), wkingind));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("Rank"+ar[1]) , wkingind));
			
			//WRook
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("File"+ar[2].toUpperCase()), wrookind));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("Rank"+ar[3])	, wrookind));
			
			//BKing
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("File"+ar[4].toUpperCase()), bkingind));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept("Rank"+ar[5]), bkingind));
			
		
			
			// PROPERTIES
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.hasPiece, gameind,
					wkingind));
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.hasPiece, gameind,
					wrookind));
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.hasPiece, gameind,
					bkingind));
	
			// labels
	
			KRKPiece WKingPiece = new KRKPiece(wkingind, ar[0], Integer
					.parseInt(ar[1]));
			KRKPiece WRookPiece = new KRKPiece(wrookind, ar[2], Integer
					.parseInt(ar[3]));
			KRKPiece BKingPiece = new KRKPiece(bkingind, ar[4], Integer
					.parseInt(ar[5]));
	
			makeDistanceRoles(WKingPiece, WRookPiece);
			makeDistanceRoles(WKingPiece, BKingPiece);
			makeDistanceRoles(WRookPiece, BKingPiece);
		
	}
	
	
	/*protected static void verifySomeConcepts(OntologyCloser oc) {
		
		ArrayList<String> test=new ArrayList<String>();
		test.add("(EXISTS \"http://www.test.de/test#hasPiece\".EXISTS \"http://www.test.de/test#rankDistanceLessThan2\".(\"http://www.test.de/test#BKing\" AND EXISTS \"http://www.test.de/test#fileDistanceLessThan1\".(\"http://www.test.de/test#A\" OR \"http://www.test.de/test#WKing\")) AND EXISTS \"http://www.test.de/test#hasPiece\".(\"http://www.test.de/test#WKing\" AND ((\"http://www.test.de/test#C\" AND EXISTS \"http://www.test.de/test#hasLowerRankThan\".\"http://www.test.de/test#A\") OR (\"http://www.test.de/test#F3\" AND EXISTS \"http://www.test.de/test#rankDistance2\".\"http://www.test.de/test#WRook\"))))");
		for (int i = 0; i < test.size(); i++) {
			String conceptStr = test.get(i);
			oc.verifyConcept(conceptStr);
		}
		/*conceptStr = "EXISTS \"http://www.test.de/test#hasLowerRankThan\"."+
		"(\"http://www.test.de/test#WRook\""+ 
		"AND ALL \"http://www.test.de/test#fileDistanceLessThan1\".\"http://www.test.de/test#WKing\") ";
		
		System.out.println();
	}*/

	public void makeDistanceRoles(KRKPiece A, KRKPiece B) {
		int Fdist = A.getFileDistance(B);
		int Rdist = A.getRankDistance(B);
		String rdistance = "rankDistance";
		String fdistance = "fileDistance";

		kb.addABoxAxiom(new ObjectPropertyAssertion(getRole(rdistance + Rdist),
				A.id, B.id));
		kb.addABoxAxiom(new ObjectPropertyAssertion(getRole(fdistance + Fdist),
				A.id, B.id));
		
		if (A.meHasLowerFileThan(B)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.fileLessThan, A.id,
					B.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.fileHigherThan, B.id,
					A.id));		
		}
		else if (B.meHasLowerFileThan(A)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.fileLessThan, B.id,
					A.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.fileHigherThan, A.id,
					B.id));
		}

		if (A.meHasLowerRankThan(B)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.rankLessThan, A.id,
					B.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.rankHigherThan, B.id,
					A.id));
		}
		else if (B.meHasLowerRankThan(A)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.rankLessThan, B.id,
					A.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(KRKOntologyTBox.rankHigherThan, A.id,
					B.id));
			
		}
	}

	

	
	protected static void writeExampleSet(Set<Individual> s) {
		for (Individual individual : s) {
			writeToFile(workingDir+"example.txt", "+\""+individual.getName()+"\"\n");
		}
		
	} 
	

	
	

	/*protected static void writeExampleSets() {
		StringBuffer collect1 = new StringBuffer();
		StringBuffer collect2 = new StringBuffer();
		System.out.println("start writing sets");

		for (String keys : classToInd.keySet()) {
			System.out.println(keys);
			SortedSet<String> tmpset = classToInd.get(keys);
			for (String individuals : tmpset) {
				collect1.append("+\"" + individuals + "\"\n");
				collect2.append("-\"" + individuals + "\"\n");
			}

			writeToFile(workingDir+"/examples_for_" + keys + ".txt", collect1
					+ "\n\n" + collect2 + "\n");
			collect1 = new StringBuffer();
			collect2 = new StringBuffer();
		}
		// System.out.println("Sets written");
		collect1 = new StringBuffer();
		collect2 = new StringBuffer();
		for (String key : classToInd.keySet()) {

			SortedSet<String> tmpset = classToInd.get(key);

			if (key.equals("ZERO")) {*/
			//	collect1.append("/**" + key + "**/\n");
			/*	for (String individuals : tmpset) {
					collect1.append("+\"" + individuals + "\"\n");
				}

				continue;
			} else { */
		//		collect2.append("/**" + key + "**/\n");
		/*		for (String individuals : tmpset) {
					collect2.append("-\"" + individuals + "\"\n");
				}
			}

		}
		writeToFile(workingDir+"/examples_for_ZERO_and_Rest.txt", collect1
				+ "\n\n" + collect2 + "\n");
		System.out.println("Example sets written");
	}
	*/

	

	
	

	/*protected  void writeOWLFile(String filename) {

		Iterator<String> it = classSet.iterator();
		System.out.println("import(\"" + filename + "\");");
		String collect = "refinement.ignoredConcepts={";
		while (it.hasNext()) {
			String tmp = (String) it.next();
			collect += "\n\"" + getAtomicConcept(tmp).getName() + "\",";
		}
		collect = collect.substring(0, collect.length() - 1);
		System.out.println(collect + "};");

		System.out.println("Writing owl");
		File owlfile = new File(workingDir+"/" + filename);
		// System.out.println(kb.toString("http://www.test.de/test", new
		// HashMap<String, String>()));
		OWLAPIReasoner.exportKBToOWL(owlfile, kb, ontologyURI);

	}*/
	
	protected  void writeOWLFile() {
		writeOWLFile("test.owl");
	}
	
	protected  void writeOWLFile(String filename) {

		System.out.println("Writing owl");
		File owlfile = new File(workingDir+"/" + filename);
		OWLAPIReasoner.exportKBToOWL(owlfile, this.kb, ontologyURI);

	}

	

	protected static void writeToFile(String filename, String content) {
		// create the file we want to use
		File file = new File(filename);

		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename, true);
			// ObjectOutputStream o = new ObjectOutputStream(fos);
			fos.write(content.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static void addToHM(String className, Individual ind) {
		if (classToInd.get(className) == null)
			classToInd.put(className, new TreeSet<Individual>());

		classToInd.get(className).add(ind);
		indToClass.put(ind, className);

	}
	
	
	protected static Individual getIndividual(String name) {
		return new Individual(ontologyURI + "#" + name);
	}

	protected static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyURI + "#" + name);
	}
	
	protected static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyURI + "#" + name);
	}
	
	public static String[] tokenize(String s) {
		StringTokenizer st = new StringTokenizer(s, ",");

		String tmp = "";
		String[] ret = new String[7];
		int x = 0;
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();
			if (x == 6)
				tmp = tmp.toUpperCase();
			ret[x] = tmp;
			x++;
		}
		return ret;

	}
	
	static SortedSet<Individual> getNegativeExamples(String currentClass, SortedSet<Individual> allRetrieved, int howmany) {
		SortedSet<Individual> ret = new TreeSet<Individual>();
		
		//clean
		SortedSet<Individual> allPos = classToInd.get(currentClass);
		for (Individual individual : allPos) {
			if(!allRetrieved.remove(individual))System.out.println("WARNING, not all positives covered");;
		}
		
		Random r = new Random();
		double treshold = ((double)howmany)/allRetrieved.size();
		//System.out.println("treshold"+howmany);
		//System.out.println("treshold"+allRetrieved.size());
		//System.out.println("treshold"+treshold);
		int added=0;
		for (Individual oneInd : allRetrieved) {
			if(r.nextDouble()<treshold) {
				ret.add(oneInd);
				added++;
			}
		}
		System.out.println(added+" new negs added");
		return ret;
	}
	static boolean verify(String currentClass, SortedSet<Individual> allRetrieved) {
		//clean
		SortedSet<Individual> allPos = classToInd.get(currentClass);
		boolean hasAll=true;
		for (Individual individual : allPos) {
			if(!allRetrieved.contains(individual))hasAll=false;
		}
		if(hasAll && allRetrieved.size()==allPos.size()){return true;}
		else return false;
		
	}
	
	/*public SortedSet<Individual> checkAll(String concept, SortedSet<Individual> s){
	try{
		Description d = KBParser.parseConcept(concept,ontologyURI.toString()+"#");
		
	return reasoner.instanceCheck(d, s);
	}catch (Exception e) {e.printStackTrace();}
	return null;
}

public SortedSet<Individual> checkAllOWLAPI(String concept,SortedSet<Individual> s){
	try{
		Description d = KBParser.parseConcept(concept,ontologyURI.toString()+"#");
		return reasoner.instanceCheck(d, s);
	}catch (Exception e) {e.printStackTrace();}
	return null;
}

/*public SortedSet<Individual> retrieveAllowlAPI(String concept){
	try{
		Description d = KBParser.parseConcept(concept,ontologyURI.toString()+"#");
		return reasoner.retrieval(d);
	}catch (Exception e) {e.printStackTrace();}
	return null;
}*/

	
}
