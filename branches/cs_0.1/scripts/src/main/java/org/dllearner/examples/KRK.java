package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.owl.OntologyCloser;
import org.semanticweb.owlapi.model.IRI;

/*
 * Structure
 * 
 * 
 * 
 * 
 * */

public class KRK {

	// REMEMBER
	// FILES are letters
	// RANKS are numbers

	// FLAGS
	// 
	// turn off to not write the owl, needs about 30 seconds or more
	static boolean writeOWL = true;
	static boolean writeKB = false;
	static boolean useTransitivity = false;
	static boolean useHigherThan = false;
	static boolean writeExampleSets = false;
	static boolean writeConciseOWLAllDifferent = false;
	
	
	static boolean closeKB=false;
	static boolean closeConcise= true && closeKB;
	static boolean writeClosedOWL = true && closeKB;
	static boolean verifySomeConcepts = false; // && closeKB; <-- && closeKB does not make sense
	

	static boolean useInverse = false;
	// dependent, love and marriage, horse and carriage
	static boolean useTripleSubProps = useInverse && false;
	
	
	static String workingDir = "examples/krk";
	//static String fileIn = workingDir+"/krkopt_no_draw.data";
	static String fileIn = workingDir+"/krkopt.data";
	static String owlfilename = "complete_nodraw.owl";
	

	static IRI ontologyIRI = IRI.create("http://dl-learner.org/krk");
	// static SortedSet<String> fileSet = new TreeSet<String>();
	static SortedSet<String> allInstances = new TreeSet<String>();
	static SortedSet<String> classSet = new TreeSet<String>();
	static SortedSet<String> symmetricRoleSet = new TreeSet<String>();

	static HashMap<String, SortedSet<String>> classToInd = new HashMap<String, SortedSet<String>>();

	// static LinkedList<String> words;
	static KB kb;

	static NamedClass Game = getAtomicConcept("Game");
	static NamedClass WKing = getAtomicConcept("WKing");
	static NamedClass WRook = getAtomicConcept("WRook");
	static NamedClass BKing = getAtomicConcept("BKing");
	// had to rename, too much similarity to java.io.File
	static NamedClass FileData = getAtomicConcept("File");
	static NamedClass Rank = getAtomicConcept("Rank");
	static NamedClass Piece = getAtomicConcept("Piece");

	static ObjectProperty hasPiece = getRole("hasPiece");
	static ObjectProperty hasWKing = getRole("hasWKing");
	static ObjectProperty hasWRook = getRole("hasWRook");
	static ObjectProperty hasBKing = getRole("hasBKing");

	static ObjectProperty hasPieceInv = getRole("hasGame");
	static ObjectProperty hasWKingInv = getRole("hasWKingInv");
	static ObjectProperty hasWRookInv = getRole("hasWRookInv");
	static ObjectProperty hasBKingInv = getRole("hasBKingInv");

	static ObjectProperty rankLessThan = getRole("hasLowerRankThan");
	static ObjectProperty fileLessThan = getRole("hasLowerFileThan");
	
	static ObjectProperty rankHigherThan = getRole("hasHigherRankThan");
	static ObjectProperty fileHigherThan = getRole("hasHigherFileThan");

	// static ObjectProperty lessThan = getRole("strictLessThan");

	// static ObjectProperty hasRankInv = getRole("hasRankInv");
	// static ObjectProperty hasFileInv = getRole("hasFileInv");
	//
	// static ObjectProperty lessThanInv = getRole("strictLessThanInv");

	// static HashMap<String,SortedSet<String>> classToInd;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start");
		workingDir = workingDir + File.separator;
		if (!new File(workingDir).exists()) {
			System.out.println("Created directory: " + workingDir + " : " + new File(workingDir).mkdir()
							+ ".");
		}
		
		// Datei �ffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// classToInd = new HashMap<String,SortedSet<String>>();
		init();

		Individual gameind;
		Individual wkingind;
		Individual wrookind;
		Individual bkingind;
		
		
		
		
		
		

		try {
			String line = "";
			String[] ar = new String[6];
//			String currentclass="";

			int x = 0;
			while ((line = in.readLine()) != null) {
				x++;
				if (x % 3000 == 0)
					System.out.println("Currently at line" + x);
				ar = tokenize(line);

//				currentclass = ar[6];
				
				gameind = getIndividual("game" + x);
				wkingind = getIndividual("wking_" + ar[0] + ar[1] + "_" + x);
				wrookind = getIndividual("wrook_" + ar[2] + ar[3] + "_" + x);
				bkingind = getIndividual("bking_" + ar[4] + ar[5] + "_" + x);
				
				
				
				allInstances.add(gameind+"");
				allInstances.add(wkingind+"");
				allInstances.add(wrookind+"");
				allInstances.add(bkingind+"");

				// if (x == 1)
				// currentClass = ar[6];

				// save it for examplegeneration
				addToHM(ar[6], gameind.getName());
				// .add(gameind.getName());
				classSet.add(ar[6]);

				// CLASSES
				kb.addABoxAxiom(new ClassAssertionAxiom(Game, gameind));
				kb.addABoxAxiom(new ClassAssertionAxiom(
						getAtomicConcept(ar[6]), gameind));
				kb.addABoxAxiom(new ClassAssertionAxiom(WKing, wkingind));
				kb.addABoxAxiom(new ClassAssertionAxiom(WRook, wrookind));
				kb.addABoxAxiom(new ClassAssertionAxiom(BKing, bkingind));

				
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
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
						wkingind));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
						wrookind));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
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

				// FILERANK
				/*
				 * kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile,
				 * wkingind, getIndividual(ar[0]))); kb.addABoxAxiom(new
				 * ObjectPropertyAssertion(hasRank, wkingind, getIndividual("r" +
				 * ar[1])));
				 * 
				 * kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile,
				 * wrookind, getIndividual(ar[2]))); kb.addABoxAxiom(new
				 * ObjectPropertyAssertion(hasRank, wrookind, getIndividual("r" +
				 * ar[3])));
				 * 
				 * kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile,
				 * bkingind, getIndividual(ar[4]))); kb.addABoxAxiom(new
				 * ObjectPropertyAssertion(hasRank, bkingind, getIndividual("r" +
				 * ar[5])));
				 */

			}// endWhile

			finishBackgroundForRoles();
			System.out.println("Finished Background");
			// WRITE
			if(writeExampleSets)writeExampleSets();
			if(writeConciseOWLAllDifferent)writeConciseOWLAllDifferent();
			if (writeOWL)writeOWLFile(owlfilename);
			if(writeKB)writeKBFile("test.kb");
				
			OntologyCloser oc = null;
			String kbaddition= "_Closed";
			if(closeKB) {
				oc= new OntologyCloser(kb);
				if(closeConcise) {
					oc.applyNumberRestrictionsConcise();
					kbaddition = "_CloseConcise";
				}
				else oc.applyNumberRestrictions();
			}
			
		
			if (verifySomeConcepts)	{
				
				oc.updateReasoner();
				verifySomeConcepts(oc);
			}
			if (writeClosedOWL) writeOWLFile("test"+kbaddition+".owl");

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}// end main
	
	
	
	static void makeOntology(){
		
		
		
	}
	
	
	protected static void verifySomeConcepts(OntologyCloser oc) {
		
		ArrayList<String> test=new ArrayList<String>();
			
		for (int i = 0; i < test.size(); i++) {
			String conceptStr = test.get(i);
			oc.verifyConcept(conceptStr);
		}
		
		
		System.out.println();
	}

	static void makeDistanceRoles(KRKPiece A, KRKPiece B) {
		int Fdist = A.getFileDistance(B);
		int Rdist = A.getRankDistance(B);
		String rdistance = "rankDistance";
		String fdistance = "fileDistance";

		kb.addABoxAxiom(new ObjectPropertyAssertion(getRole(rdistance + Rdist),
				A.id, B.id));
		symmetricRoleSet.add(rdistance + Rdist);
		kb.addABoxAxiom(new ObjectPropertyAssertion(getRole(fdistance + Fdist),
				A.id, B.id));
		symmetricRoleSet.add(fdistance + Fdist);
		
		

		if (A.meHasLowerFileThan(B)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(fileLessThan, A.id,
					B.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(fileHigherThan, B.id,
					A.id));
			
		}
		// 18:00
		else if (B.meHasLowerFileThan(A)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(fileLessThan, B.id,
					A.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(fileHigherThan, A.id,
					B.id));
		}

		if (A.meHasLowerRankThan(B)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(rankLessThan, A.id,
					B.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(rankHigherThan, B.id,
					A.id));
		}
		else if (B.meHasLowerRankThan(A)){
			kb.addABoxAxiom(new ObjectPropertyAssertion(rankLessThan, B.id,
					A.id));
			if(useHigherThan)kb.addABoxAxiom(new ObjectPropertyAssertion(rankHigherThan, A.id,
					B.id));
			
		}
	}

	public static void init() {
		kb = new KB();
		initClassHierarchy();
		initDisJointClasses();

		initClassesForRankAndFile();
		initDomainRangeForRoles();
	}

	public static void initDisJointClasses() {
		SortedSet<Description> DisJointClasses1 = new TreeSet<Description>();
		DisJointClasses1.add(Piece);
		// DisJointClasses1.add(Rank);
		// DisJointClasses1.add(File);
		DisJointClasses1.add(Game);
		//kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses1));

		SortedSet<Description> DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2.add(WKing);
		DisJointClasses2.add(WRook);
		DisJointClasses2.add(BKing);
		// DisJointClasses2.add(Rank);
		// DisJointClasses2.add(File);
		// DisJointClasses2.add(Game);

		//kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses2));
	}

	public static void initClassHierarchy() {

		// all sub of piece
		kb.addTBoxAxiom(new SubClassAxiom(WKing, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(WRook, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(BKing, Piece));
		
		String[] letters=new String[]{"FileA","FileB","FileC","FileD","FileE","FileF","FileG","FileH"};
		String[] numbers=new String[8];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i]="Rank"+i;
		}
		//System.out.println(numbers);
		
		for (int i = 0; i < numbers.length; i++) {
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(letters[i]),Piece));
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(letters[i]),Piece));
		}
		
	}

	static void initClassesForRankAndFile() {

		/*
		 * fileSet.add("a"); fileSet.add("b"); fileSet.add("c");
		 * fileSet.add("d"); fileSet.add("e"); fileSet.add("f");
		 * fileSet.add("g"); fileSet.add("h"); for (int count = 1; count < 9;
		 * count++) { rankSet.add("r" + count); }
		 * 
		 * for (String oneFile : fileSet) { kb.addTBoxAxiom(new
		 * SubClassAxiom(getAtomicConcept(oneFile.toUpperCase()),File));
		 * kb.addABoxAxiom(new
		 * ClassAssertionAxiom(getAtomicConcept(oneFile.toUpperCase()),getIndividual(oneFile))); }
		 * for (String oneRank : rankSet) { kb.addTBoxAxiom(new
		 * SubClassAxiom(getAtomicConcept(oneRank.toUpperCase()),Rank));
		 * kb.addABoxAxiom(new
		 * ClassAssertionAxiom(getAtomicConcept(oneRank.toUpperCase()),getIndividual(oneRank))); }
		 */

	}// end init

	static void initDomainRangeForRoles() {

		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasPiece, Game));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasPiece, Piece));

		if (useTripleSubProps) {
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasWKing, Game));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasWKing, WKing));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasWRook, Game));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasWRook, WRook));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasBKing, Game));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasBKing, BKing));
		}

		if (useInverse) {
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasPieceInv, Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasPieceInv, Game));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasWKingInv, WKing));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasWKingInv, Game));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasWRookInv, WRook));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasWRookInv, Game));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasBKingInv, BKing));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasBKingInv, Game));
		}

		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(rankLessThan, Piece));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(rankLessThan, Piece));
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(fileLessThan, Piece));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(fileLessThan, Piece));
		
		if(useHigherThan) {
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(rankHigherThan, Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(rankHigherThan, Piece));
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(fileHigherThan, Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(fileHigherThan, Piece));
		}
	}

	static void finishBackgroundForRoles() {

		if (useTransitivity) {
			kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(rankLessThan));
			kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(fileLessThan));
			if(useHigherThan) { 
				kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(rankHigherThan));
				kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(fileHigherThan));
			}
			
		}
		if (useInverse)
		// INVERSE
		{
			kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasPiece,
					hasPieceInv));
			kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasWKing,
					hasWKingInv));
			kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasWRook,
					hasWRookInv));
			kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasBKing,
					hasBKingInv));
		}

		if (useTripleSubProps) {
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(hasWKing, hasPiece));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(hasWRook, hasPiece));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(hasBKing, hasPiece));
		}

		for (String oneRole : symmetricRoleSet) {
			kb.addRBoxAxiom(new SymmetricObjectPropertyAxiom(getRole(oneRole)));
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(getRole(oneRole),
					Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(getRole(oneRole),
					Piece));
		}

		for (int i = 8; i > 0; i--) {
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("rankDistanceLessThan" + (i - 1)),
					getRole("rankDistanceLessThan" + i)));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("fileDistanceLessThan" + (i - 1)),
					getRole("fileDistanceLessThan" + i)));

			kb.addRBoxAxiom(new SubObjectPropertyAxiom(getRole("rankDistance"
					+ (i - 1)), getRole("rankDistanceLessThan" + i)));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(getRole("fileDistance"
					+ (i - 1)), getRole("fileDistanceLessThan" + i)));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(
					getRole("rankDistanceLessThan" + i), Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(
					getRole("rankDistanceLessThan" + i), Piece));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(
					getRole("fileDistanceLessThan" + i), Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(
					getRole("fileDistanceLessThan" + i), Piece));
		}

		// kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(rankLessThan, Piece));
		// kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(fileLessThan, Piece));
	}

	/*
	 * static void initBackgroundForRankAndFileRoles() {
	 * 
	 * kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(lessThan));
	 * kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(lessThan, new Union(Rank,
	 * File)));
	 * 
	 * kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(lessThan, new Union(Rank,
	 * File))); kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasRank, Piece));
	 * kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasRank, Rank));
	 * kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasFile, Piece));
	 * kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasFile, File));
	 * 
	 * 
	 * 
	 * kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasRank,hasRankInv));
	 * kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasFile,hasFileInv));
	 * kb.addRBoxAxiom(new InverseObjectPropertyAxiom(lessThan,lessThanInv)); //
	 * assigning lessthan to file Iterator<String> it = fileSet.iterator();
	 * Individual current = getIndividual(it.next()); Individual next; while
	 * (it.hasNext()) { next = getIndividual(it.next()); kb .addABoxAxiom(new
	 * ObjectPropertyAssertion(lessThan, current, next)); current = next; } //
	 * assigning lessthan to rank it = rankSet.iterator(); current =
	 * getIndividual(it.next()); next = null; while (it.hasNext()) { next =
	 * getIndividual(it.next()); kb .addABoxAxiom(new
	 * ObjectPropertyAssertion(lessThan, current, next)); current = next; } //
	 * new PropertyRangeAxiom(rank, (PropertyRange) nc[5]);
	 * 
	 * String str = "hasDistanceOf"; String str2 = "hasDistanceLessThan"; //
	 * ObjectProperty tmp; String[] rankarray = new String[8]; String[]
	 * filearray = new String[8]; rankSet.toArray(rankarray);
	 * fileSet.toArray(filearray); // assigning has DistanceOf to Rank and File
	 * for (int count = 0; count < filearray.length; count++) { for (int inner =
	 * count + 1, dist = 1; inner < filearray.length; inner++, dist++) { //
	 * ObjectProperty op = getRole(str+inner);
	 * kb.addABoxAxiom(getRoleAssertion(str + dist, rankarray[count],
	 * rankarray[inner])); kb.addABoxAxiom(getRoleAssertion(str + dist,
	 * filearray[count], filearray[inner])); } kb.addRBoxAxiom(new
	 * ObjectPropertyDomainAxiom(getRole(str+(count+1)), new Union(Rank,File)));
	 * kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(getRole(str+(count+1)), new
	 * Union(Rank,File))); kb.addRBoxAxiom(new
	 * ObjectPropertyDomainAxiom(getRole(str2+(count+1)), new
	 * Union(Rank,File))); kb.addRBoxAxiom(new
	 * ObjectPropertyRangeAxiom(getRole(str2+(count+1)), new Union(Rank,File))); } //
	 * make it symmetric + subproperty of for (int count = 1; count < 8;
	 * count++) { kb.addRBoxAxiom(new SymmetricObjectPropertyAxiom(getRole(str +
	 * count))); kb.addRBoxAxiom(new SubObjectPropertyAxiom(getRole(str +
	 * count), getRole(str2 + (count + 1)))); kb.addRBoxAxiom(new
	 * SubObjectPropertyAxiom(getRole(str2 + count), getRole(str2 + (count +
	 * 1)))); } }
	 */
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

	protected static void writeExampleSets() {
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

			if (key.equals("ZERO")) {
				collect1.append("/**" + key + "**/\n");
				for (String individuals : tmpset) {
					collect1.append("+\"" + individuals + "\"\n");
				}

				continue;
			} else {
				collect2.append("/**" + key + "**/\n");
				for (String individuals : tmpset) {
					collect2.append("-\"" + individuals + "\"\n");
				}
			}

		}
		writeToFile(workingDir+"/examples_for_ZERO_and_Rest.txt", collect1
				+ "\n\n" + collect2 + "\n");
		System.out.println("Example sets written");
	}
	

	protected static void writeConciseOWLAllDifferent() {
		StringBuffer collect = new StringBuffer();
		System.out.println("start writing OWLAllDifferent");
		collect.append("<owl:AllDifferent>\n" +
				"<owl:distinctMembers rdf:parseType=\"Collection\">\n");
		
		for (String inst : allInstances) {
			collect.append("<owl:Thing rdf:about=\"" + inst + "\" />\n");	
		}
		collect.append("</owl:distinctMembers>"+
 			"</owl:AllDifferent>");
		
		writeToFile(workingDir+"/owlAllDifferent.txt", collect.toString());
	   
	}

	
	

	protected static void writeOWLFile(String filename) {

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
		OWLAPIReasoner.exportKBToOWL(owlfile, kb, ontologyIRI);

	}
	
	protected static void writeKBFile(String filename) {

		System.out.println("Writing kb");
		try{
		FileWriter fw = new FileWriter(workingDir+"/" + filename,false); 
		fw.write(kb.toKBSyntaxString(ontologyIRI.toString(), null));
		fw.flush();
		}catch (Exception e) {e.printStackTrace();}
		System.out.println("done writing kb");
	

	}

	protected static Individual getIndividual(String name) {
		return new Individual(ontologyIRI + "#" + name);
	}

	protected static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyIRI + "#" + name);
	}

	protected static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyIRI + "#" + name);
	}

	protected static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyIRI + "#" + name);
	}

	protected static String getURI(String name) {
		return ontologyIRI + "#" + name;
	}

	protected static ClassAssertionAxiom getConceptAssertion(String concept,
			String i) {
		Individual ind = getIndividual(i);
		NamedClass c = getAtomicConcept(concept);
		return new ClassAssertionAxiom(c, ind);
	}

	protected static ObjectPropertyAssertion getRoleAssertion(String role,
			String i1, String i2) {
		Individual ind1 = getIndividual(i1);
		Individual ind2 = getIndividual(i2);
		ObjectProperty ar = getRole(role);
		return new ObjectPropertyAssertion(ar, ind1, ind2);
	}

	protected static void writeToFile(String filename, String content) {
		// create the file we want to use
		File file = new File(filename);

		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename, false);
			// ObjectOutputStream o = new ObjectOutputStream(fos);
			fos.write(content.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static void addToHM(String className, String ind) {
		if (classToInd.get(className) == null)
			classToInd.put(className, new TreeSet<String>());

		classToInd.get(className).add(ind);

	}

}
