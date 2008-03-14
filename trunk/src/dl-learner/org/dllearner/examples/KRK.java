package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DisjointClassesAxiom;
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
import org.dllearner.core.owl.Union;
import org.dllearner.reasoning.OWLAPIReasoner;

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

	static URI ontologyURI = URI.create("http://www.test.de/test");
	static SortedSet<String> fileSet = new TreeSet<String>();
	static SortedSet<String> rankSet = new TreeSet<String>();
	static SortedSet<String> classSet = new TreeSet<String>();
	// static SortedSet<String> instances = new TreeSet<String>();
	static HashMap<String, SortedSet<String>> classToInd = new HashMap<String, SortedSet<String>>();

	// static LinkedList<String> words;
	static KB kb;

	static NamedClass Game = getAtomicConcept("Game");
	static NamedClass WKing = getAtomicConcept("WKing");
	static NamedClass WRook = getAtomicConcept("WRook");
	static NamedClass BKing = getAtomicConcept("BKing");
	static NamedClass File = getAtomicConcept("File");
	static NamedClass Rank = getAtomicConcept("Rank");
	static NamedClass Piece = getAtomicConcept("Piece");

	static ObjectProperty hasRank = getRole("hasRank");
	static ObjectProperty hasFile = getRole("hasFile");
	static ObjectProperty hasPiece = getRole("hasPiece");
	static ObjectProperty lessThan = getRole("strictLessThan");

	
	static ObjectProperty hasRankInv = getRole("hasRankInv");
	static ObjectProperty hasFileInv = getRole("hasFileInv");
	static ObjectProperty hasPieceInv = getRole("hasPieceInv");
	static ObjectProperty lessThanInv = getRole("strictLessThanInv");

	// static HashMap<String,SortedSet<String>> classToInd;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// turn off to not write the owl, needs about 30 seconds or more
		boolean writeOWL = true;

		// classToInd = new HashMap<String,SortedSet<String>>();
		initVarsAndClasses();
		initBackgroundRoles();

		Individual gameind;
		Individual wkingind;
		Individual wrookind;
		Individual bkingind;
		//String currentClass = "";

		String fileIn = "examples/krk/krkopt.data";

		// Datei öffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			String line = "";
			String[] ar = new String[6];

			int x = 0;
			while ((line = in.readLine()) != null) {
				x++;
				// if(x % 3000 == 0 ) System.out.println("Currently at line
				// "+x);
				ar = tokenize(line);

				gameind = getIndividual("game" + x);
				wkingind = getIndividual("wking" + x);
				wrookind = getIndividual("wrook" + x);
				bkingind = getIndividual("bking" + x);

				//if (x == 1)
					//currentClass = ar[6];

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

				// PROPERTIES
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
						wkingind));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
						wrookind));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasPiece, gameind,
						bkingind));

				kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile, wkingind,
						getIndividual(ar[0])));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasRank, wkingind,
						getIndividual("r" + ar[1])));

				kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile, wrookind,
						getIndividual(ar[2])));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasRank, wrookind,
						getIndividual("r" + ar[3])));

				kb.addABoxAxiom(new ObjectPropertyAssertion(hasFile, bkingind,
						getIndividual(ar[4])));
				kb.addABoxAxiom(new ObjectPropertyAssertion(hasRank, bkingind,
						getIndividual("r" + ar[5])));

				// kb.addABoxAxiom(new ClassAssertionAxiom(new
				// NamedClass("Game"),new Individual(names[0]+(x++))));
				// kb.addABoxAxiom(new ClassAssertionAxiom(new
				// NamedClass("Game"),new Individual(names[0]+(x++))));

				// System.out.println(line);

				// instances

			}// endWhile

			String collect1 = "", collect2 = "";

			for (String keys : classToInd.keySet()) {
				SortedSet<String> tmpset = classToInd.get(keys);
				for (String individuals : tmpset) {
					collect1 += "+\"" + individuals + "\"\n";
					collect2 += "-\"" + individuals + "\"\n";
				}

				writeToFile("examples/krk/" + keys + ".txt", collect1 + "\n\n"
						+ collect2 + "\n");
				collect1 = "";
				collect2 = "";
			}

			String outputfile = "test.owl";
			Iterator<String> it = classSet.iterator();
			System.out.println("import(\"" + outputfile + "\");");
			String collect = "refinement.ignoredConcepts={";
			while (it.hasNext()) {
				String tmp = (String) it.next();
				collect += "\n\"" + getAtomicConcept(tmp).getName() + "\",";
			}
			collect = collect.substring(0, collect.length() - 1);
			System.out.println(collect + "};");

			System.out.println("Writing owl");
			File owlfile = new File("examples/krk/" + outputfile);
			// System.out.println(kb.toString("http://www.test.de/test", new
			// HashMap<String, String>()));
			if (writeOWL)
				OWLAPIReasoner.exportKBToOWL(owlfile, kb, ontologyURI);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}// end main

	static void initVarsAndClasses() {
		kb = new KB();
		fileSet.add("a");
		fileSet.add("b");
		fileSet.add("c");
		fileSet.add("d");
		fileSet.add("e");
		fileSet.add("f");
		fileSet.add("g");
		fileSet.add("h");
		for (int count = 1; count < 9; count++) {
			rankSet.add("r" + count);
		}

		for (String oneFile : fileSet) {
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(oneFile.toUpperCase()),File));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept(oneFile.toUpperCase()),getIndividual(oneFile)));
		}
		for (String oneRank : rankSet) {
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(oneRank.toUpperCase()),Rank));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept(oneRank.toUpperCase()),getIndividual(oneRank)));
		}
		
		SortedSet<Description> DisJointClasses1 = new TreeSet<Description>();
		DisJointClasses1.add(Piece);
		DisJointClasses1.add(Rank);
		DisJointClasses1.add(File);
		DisJointClasses1.add(Game);
		kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses1));

		SortedSet<Description> DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2.add(WKing);
		DisJointClasses2.add(WRook);
		DisJointClasses2.add(BKing);
		//DisJointClasses2.add(Rank);
		//DisJointClasses2.add(File);
		//DisJointClasses2.add(Game);

		kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses2));

		// all sub of piece
		kb.addTBoxAxiom(new SubClassAxiom(WKing, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(WRook, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(BKing, Piece));

		// Classes for files
		//Iterator<String> it = fileSet.iterator();
		//Individual next;
		//while (it.hasNext()) {
			//next = getIndividual(it.next());
			//kb.addABoxAxiom(new ClassAssertionAxiom(File, next));
		//}

		// Classes for rank
		/*it = rankSet.iterator();
		while (it.hasNext()) {
			next = getIndividual(it.next());
			kb.addABoxAxiom(new ClassAssertionAxiom(Rank, next));
		}*/

	}// end init

	static void initBackgroundRoles() {

		kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(lessThan));
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(lessThan, new Union(Rank,
				File)));

		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(lessThan, new Union(Rank,
				File)));
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasRank, Piece));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasRank, Rank));
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasFile, Piece));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasFile, File));
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasPiece, Game));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasPiece, Piece));
		
		kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasPiece,hasPieceInv));
		kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasRank,hasRankInv));
		kb.addRBoxAxiom(new InverseObjectPropertyAxiom(hasFile,hasFileInv));
		kb.addRBoxAxiom(new InverseObjectPropertyAxiom(lessThan,lessThanInv));
		
		// assigning lessthan to file
		Iterator<String> it = fileSet.iterator();
		Individual current = getIndividual(it.next());
		Individual next;
		while (it.hasNext()) {
			next = getIndividual(it.next());
			kb
					.addABoxAxiom(new ObjectPropertyAssertion(lessThan,
							current, next));
			current = next;

		}
		// assigning lessthan to rank
		it = rankSet.iterator();
		current = getIndividual(it.next());
		next = null;
		while (it.hasNext()) {
			next = getIndividual(it.next());
			kb
					.addABoxAxiom(new ObjectPropertyAssertion(lessThan,
							current, next));
			current = next;

		}

		// new PropertyRangeAxiom(rank, (PropertyRange) nc[5]);

		String str = "hasDistanceOf";
		String str2 = "hasDistanceLessThan";
		// ObjectProperty tmp;
		String[] rankarray = new String[8];
		String[] filearray = new String[8];
		rankSet.toArray(rankarray);
		fileSet.toArray(filearray);

		// assigning has DistanceOf to Rank and File
		for (int count = 0; count < filearray.length; count++) {
			for (int inner = count + 1, dist = 1; inner < filearray.length; inner++, dist++) {
				// ObjectProperty op = getRole(str+inner);
				kb.addABoxAxiom(getRoleAssertion(str + dist, rankarray[count],
						rankarray[inner]));
				kb.addABoxAxiom(getRoleAssertion(str + dist, filearray[count],
						filearray[inner]));

			}
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(getRole(str+(count+1)), new Union(Rank,File)));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(getRole(str+(count+1)), new Union(Rank,File)));
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(getRole(str2+(count+1)), new Union(Rank,File)));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(getRole(str2+(count+1)), new Union(Rank,File)));
		}
		// make it symmetric + subproperty of
		for (int count = 1; count < 8; count++) {
			kb.addRBoxAxiom(new SymmetricObjectPropertyAxiom(getRole(str
					+ count)));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(getRole(str + count),
					getRole(str2 + (count + 1))));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(getRole(str2 + count),
					getRole(str2 + (count + 1))));
		}

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

	protected static Individual getIndividual(String name) {
		return new Individual(ontologyURI + "#" + name);
	}

	protected static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyURI + "#" + name);
	}

	@SuppressWarnings("unused")
	protected static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyURI + "#" + name);
	}

	protected static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyURI + "#" + name);
	}

	@SuppressWarnings("unused")
	protected static String getURI(String name) {
		return ontologyURI + "#" + name;
	}

	@SuppressWarnings("unused")
	protected static ClassAssertionAxiom getConceptAssertion(String concept,
			String i) {
		Individual ind = getIndividual(i);
		NamedClass c = getAtomicConcept(concept);
		return new ClassAssertionAxiom(c, ind);
	}

	@SuppressWarnings("unused")
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
