package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.reasoning.OWLAPIReasoner;

public class KRK {

	// REMEMBER 
	// FILES are letters
	// RANKS are numbers
	
	private static URI ontologyURI = URI.create("http://www.test.de/test");
	static SortedSet<String> fileSet;
	static SortedSet<String> rankSet;
	static SortedSet<String> classSet;
	static HashMap<String,SortedSet<String>> classToInd;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		classToInd = new HashMap<String,SortedSet<String>>();
		fileSet = new TreeSet<String>();
		rankSet = new TreeSet<String>();
		classSet = new TreeSet<String>();
		KB kb=new KB();
	
		
		fileSet.add("a");		fileSet.add("b");		fileSet.add("c");		fileSet.add("d");		fileSet.add("e");		fileSet.add("f");		fileSet.add("g");		fileSet.add("h");
		
		for (int count = 1; count < 9; count++) {
			rankSet.add("f"+count);
		}
		
		NamedClass[] nc=new NamedClass[]{
				getAtomicConcept("Game"),
				getAtomicConcept("WKing"),
				getAtomicConcept("WRook"),
				getAtomicConcept("BKing")};
		
		ObjectProperty rank= getRole("hasRank");
		ObjectProperty file= getRole("hasFile");
		ObjectProperty piece= getRole("hasPiece");
		ObjectProperty lessThan= getRole("strictLessThan");
		//ObjectProperty rank= getRole("hasRank");
		
		Individual game;
		Individual wking;
		Individual wrook;
		Individual bking;
		
		kb.addRBoxAxiom(new TransitiveObjectPropertyAxiom(lessThan));
		
		Iterator<String> it = fileSet.iterator();
		Individual current = getIndividual(it.next());
		Individual next;
		while (it.hasNext()){
			next=getIndividual(it.next());
			kb.addABoxAxiom(new ObjectPropertyAssertion(lessThan,current,next));
			current=next;
			
		}
		
		it = rankSet.iterator();
		current = getIndividual(it.next());
		next=null;
		while (it.hasNext()){
			next=getIndividual(it.next());
			kb.addABoxAxiom(new ObjectPropertyAssertion(lessThan,current,next));
			current=next;
			
		}

		
		String fileIn = "examples/krk/krkopt.data";
		
		// Datei öffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	
		try{
		String line = "";
		String[] ar = new String[6];
	
		
		int x=0;
		while ( (line =in.readLine())  != null)
		{   x++;
		   //if(x % 3000 == 0 ) System.out.println("Currently at line "+x);
			ar = tokenize(line);
			game = getIndividual("game"+x);
			wking = getIndividual("wking"+x);
			wrook = getIndividual("wrook"+x);
			bking = getIndividual("bking"+x);
			
			classSet.add(ar[6]);
			
			//ar[0]);
			
			// CLASSES
			kb.addABoxAxiom(new ClassAssertionAxiom(nc[0],game));
			kb.addABoxAxiom(new ClassAssertionAxiom(getAtomicConcept(ar[6]),game));
			kb.addABoxAxiom(new ClassAssertionAxiom(nc[1],wking));
			kb.addABoxAxiom(new ClassAssertionAxiom(nc[2],wrook));
			kb.addABoxAxiom(new ClassAssertionAxiom(nc[3],bking));
			
			//PROPERTIES
			kb.addABoxAxiom(new ObjectPropertyAssertion(piece,game,wking));
			kb.addABoxAxiom(new ObjectPropertyAssertion(piece,game,wrook));
			kb.addABoxAxiom(new ObjectPropertyAssertion(piece,game,bking));
			
					
			kb.addABoxAxiom(new ObjectPropertyAssertion(rank,wking,getIndividual(ar[0])));
			kb.addABoxAxiom(new ObjectPropertyAssertion(file,wking,getIndividual("f"+ar[1])));
			
			kb.addABoxAxiom(new ObjectPropertyAssertion(rank,wrook,getIndividual(ar[2])));
			kb.addABoxAxiom(new ObjectPropertyAssertion(file,wrook,getIndividual("f"+ar[3])));
			
			kb.addABoxAxiom(new ObjectPropertyAssertion(rank,bking,getIndividual(ar[4])));
			kb.addABoxAxiom(new ObjectPropertyAssertion(file,bking,getIndividual("f"+ar[5])));
			
			//kb.addABoxAxiom(new ClassAssertionAxiom(new NamedClass("Game"),new Individual(names[0]+(x++))));
			//kb.addABoxAxiom(new ClassAssertionAxiom(new NamedClass("Game"),new Individual(names[0]+(x++))));
			
			
			
			//System.out.println(line);
			
		}
		System.out.println("Writing owl");
		File owlfile =  new File("examples/krk/test.owl");
		//System.out.println(kb.toString("http://www.test.de/test", new HashMap<String, String>()));
		OWLAPIReasoner.exportKBToOWL(owlfile, kb, ontologyURI);
		
		}catch (Exception e) {e.printStackTrace();}
		System.out.println("Done");
	}
	
	public static String[] tokenize (String s) {
		StringTokenizer st=new StringTokenizer(s,",");
		
		String tmp="";
		String[] ret = new String[7];
		int x = 0;
		while (st.hasMoreTokens()){
			tmp=st.nextToken();
			if(x==6)tmp=tmp.toUpperCase();
			ret[x] = tmp;
			x++;
		}
		return ret;
		
	}

	private static Individual getIndividual(String name) {
		return new Individual(ontologyURI + "#" + name);
	}

	private static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyURI + "#" + name);
	}

	@SuppressWarnings("unused")
	private static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyURI + "#" + name);
	}

	private static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyURI + "#" + name);
	}

	@SuppressWarnings("unused")
	private static String getURI(String name) {
		return ontologyURI + "#" + name;
	}
	
	@SuppressWarnings("unused")
	private static ClassAssertionAxiom getConceptAssertion(String concept, String i) {
		Individual ind = getIndividual(i);
		NamedClass c = getAtomicConcept(concept);
		return new ClassAssertionAxiom(c, ind);
	}

	@SuppressWarnings("unused")
	private static ObjectPropertyAssertion getRoleAssertion(String role, String i1, String i2) {
		Individual ind1 = getIndividual(i1);
		Individual ind2 = getIndividual(i2);
		ObjectProperty ar = getRole(role);
		return new ObjectPropertyAssertion(ar, ind1, ind2);
	}
}
