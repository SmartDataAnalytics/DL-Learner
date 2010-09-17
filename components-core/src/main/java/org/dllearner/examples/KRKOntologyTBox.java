package org.dllearner.examples;

import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.parser.KBParser;

public class KRKOntologyTBox {
	
	private KB kb;
	static URI ontologyURI = URI.create("http://dl-learner.org/krk");
	
	static boolean useHigherThan=false;
	static boolean useInverse = false;
	
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

		
	/*public 	KRKOntologyTBox(KB kbin){
		this.kb=kbin;
		
	}*/
	
	public 	KRKOntologyTBox(){
		this.kb=new KB();
		initOntologyTBox();
		
		
	}
	
	public void addConcept(String concept){
		try{
		//make Description
		KBParser.internalNamespace = ontologyURI.toString()+"#";
		Description d = KBParser.parseConcept(concept);
		kb.addTBoxAxiom(new EquivalentClassesAxiom(getAtomicConcept("test"),d));
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initOntologyTBox(){
		
		//DISJOINTCLASSES
		SortedSet<Description> DisJointClasses1 = new TreeSet<Description>();
		DisJointClasses1.add(Piece);
		DisJointClasses1.add(Game);
		kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses1));

		SortedSet<Description> DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2 = new TreeSet<Description>();
		DisJointClasses2.add(WKing);
		DisJointClasses2.add(WRook);
		DisJointClasses2.add(BKing);
		// DisJointClasses2.add(Rank);
		// DisJointClasses2.add(File);
		// DisJointClasses2.add(Game);
		kb.addTBoxAxiom(new DisjointClassesAxiom(DisJointClasses2));
		
		/**CLASSES***/
		// all sub of piece
		kb.addTBoxAxiom(new SubClassAxiom(WKing, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(WRook, Piece));
		kb.addTBoxAxiom(new SubClassAxiom(BKing, Piece));
		
		String[] letters=new String[]{"FileA","FileB","FileC","FileD","FileE","FileF","FileG","FileH"};
		String[] numbers=new String[8];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i]="Rank"+(i+1);
		}
		//System.out.println(numbers);
		
		for (int i = 0; i < numbers.length; i++) {
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(letters[i]),Piece));
			kb.addTBoxAxiom(new SubClassAxiom(getAtomicConcept(numbers[i]),Piece));
		}
		
		
		kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(hasPiece, Game));
		kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(hasPiece, Piece));

		
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
		
		finishBackgroundForRoles();
		
		//return this.kb;
		
	}
	
	
	public void finishBackgroundForRoles() {

		for (int i = 8; i > 0; i--) {
			
			
			ObjectProperty rankDistance = getRole("rankDistance"+(i-1));
			ObjectProperty fileDistance = getRole("fileDistance"+(i-1));
			kb.addRBoxAxiom(new SymmetricObjectPropertyAxiom(rankDistance));
			kb.addRBoxAxiom(new SymmetricObjectPropertyAxiom(fileDistance));
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(rankDistance,
					KRKOntologyTBox.Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(rankDistance,
					KRKOntologyTBox.Piece));
			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(fileDistance,
					KRKOntologyTBox.Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(fileDistance,
					KRKOntologyTBox.Piece));
			
			
	
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("rankDistance" + (i - 1)), 
					getRole("rankDistanceLessThan" + i)));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("fileDistance" + (i - 1)), 
					getRole("fileDistanceLessThan" + i)));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(
					getRole("rankDistanceLessThan" + i), KRKOntologyTBox.Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(
					getRole("rankDistanceLessThan" + i), KRKOntologyTBox.Piece));

			kb.addRBoxAxiom(new ObjectPropertyDomainAxiom(
					getRole("fileDistanceLessThan" + i), KRKOntologyTBox.Piece));
			kb.addRBoxAxiom(new ObjectPropertyRangeAxiom(
					getRole("fileDistanceLessThan" + i), KRKOntologyTBox.Piece));
			
			
			if(i==1) continue;
			
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("rankDistanceLessThan" + (i - 1)),
					getRole("rankDistanceLessThan" + i)));
			kb.addRBoxAxiom(new SubObjectPropertyAxiom(
					getRole("fileDistanceLessThan" + (i - 1)),
					getRole("fileDistanceLessThan" + i)));
			
			
		}

		
	}
	
	
	protected static Individual getIndividual(String name) {
		return new Individual(ontologyURI + "#" + name);
	}

	protected static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyURI + "#" + name);
	}

	protected static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyURI + "#" + name);
	}

	protected static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyURI + "#" + name);
	}

	protected static String getURI(String name) {
		return ontologyURI + "#" + name;
	}

	protected static ClassAssertionAxiom getConceptAssertion(String concept,
			String instance) {
		Individual ind = getIndividual(instance);
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

	public KB getKb() {
		return kb;
	}
	
}
