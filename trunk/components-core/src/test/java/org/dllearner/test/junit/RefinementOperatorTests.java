/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.Helper;
import org.junit.Test;

/**
 * A suite of JUnit tests related to refinement operators.
 * 
 * @author Jens Lehmann
 *
 */
public class RefinementOperatorTests {

	private String baseURI;
	
	/**
	 * Applies the RhoDRDown operator to a concept and checks that the number of
	 * refinements is correct.
	 *
	 */
	@Test
	public void rhoDRDownTest() {
		try {
			String file = "../examples/carcinogenesis/carcinogenesis.owl";
			KnowledgeSource ks = new OWLFile(file);
			AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(ks));
			reasoner.init();
			baseURI = reasoner.getBaseURI();
//			ReasonerComponent rs = cm.reasoningService(rc);
			
			// TODO the following two lines should not be necessary
//			rs.prepareSubsumptionHierarchy();
//			rs.prepareRoleHierarchy();
			
			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.init();
			Description concept = KBParser.parseConcept(uri("Compound"));
			Set<Description> results = op.refine(concept, 4, null);

			for(Description result : results) {
				System.out.println(result);
			}
			
			int desiredResultSize = 141;
			if(results.size() != desiredResultSize) {
				System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
			}
			assertTrue(results.size()==desiredResultSize);
		} catch(ComponentInitException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void rhoDRDownTest2() throws ParseException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.EPC_OE);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		Description concept = KBParser.parseConcept("(\"http://localhost/aris/sap_model.owl#EPC\" AND EXISTS \"http://localhost/aris/sap_model.owl#hasModelElements\".\"http://localhost/aris/sap_model.owl#Object\")");
		Set<Description> results = op.refine(concept,10);

		for(Description result : results) {
			System.out.println(result.toString("http://localhost/aris/sap_model.owl#",null));
		}
			
		int desiredResultSize = 116;
		if(results.size() != desiredResultSize) {
			System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
		}
		assertTrue(results.size()==desiredResultSize);
	}
	
	@Test
	public void rhoDRDownTest3() throws ParseException, LearningProblemUnsupportedException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.KRK_ZERO_ONE);
		baseURI = reasoner.getBaseURI();
		
		// create learning algorithm in order to test under similar conditions than 
		// within a learning algorithm
		ComponentManager cm = ComponentManager.getInstance();
		AbstractLearningProblem lp = cm.learningProblem(PosNegLPStandard.class, reasoner);
		OCEL la = cm.learningAlgorithm(OCEL.class, lp, reasoner);
		
		Set<NamedClass> ignoredConcepts = new TreeSet<NamedClass>();
		ignoredConcepts.add(new NamedClass("http://www.test.de/test#ZERO"));
		ignoredConcepts.add(new NamedClass("http://www.test.de/test#ONE"));
		Set<NamedClass> usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		
		ClassHierarchy classHierarchy = reasoner.getClassHierarchy().cloneAndRestrict(usedConcepts); 
		classHierarchy.thinOutSubsumptionHierarchy();
		
		System.out.println(" UNIT TEST INCOMPLETE AFTER FRAMEWORK CHANGE, BECAUSE CLASS HIERARCHY IS NOT PASSED TO REFINEMENT OPERATOR ");
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		Description concept = KBParser.parseConcept("EXISTS \"http://www.test.de/test#hasPiece\".EXISTS \"http://www.test.de/test#hasLowerRankThan\".(\"http://www.test.de/test#WRook\" AND TOP)");
		Set<Description> results = op.refine(concept,8);

		for(Description result : results) {
			System.out.println(result.toString("http://www.test.de/test#",null));
		}
			
		int desiredResultSize = 8;
		if(results.size() != desiredResultSize) {
			System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
		}
		
		// the 8 refinements found on 2009/04/16 are as follows:
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(BKing AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WKing AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT BKing))
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT WKing))
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT WRook))
		// EXISTS hasPiece.>= 2 hasLowerRankThan.(WRook AND TOP)
		// >= 2 hasPiece.EXISTS hasLowerRankThan.(WRook AND TOP)
		
		// the 8 refinements found on 2010/08/02 are as follows:
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(BKing AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WKing AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND WRook)
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT BKing))
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT WKing))
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(WRook AND (NOT WRook))
		
		// EXISTS hasPiece.EXISTS hasLowerRankThan.(Piece AND WRook)
		// EXISTS hasPiece.>= 2 hasLowerRankThan.(WRook AND TOP)
		// >= 2 hasPiece.EXISTS hasLowerRankThan.(WRook AND TOP)		
		
		assertTrue(results.size()==desiredResultSize);
	}
			
	@Test
	public void rhoDRDownTest4() throws ParseException, LearningProblemUnsupportedException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.RHO1);
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		Description concept = KBParser.parseConcept("(car AND EXISTS hasOwner.person)");
//		Description concept = Thing.instance;
		Set<Description> refinements = op.refine(concept, 6);
		for(Description refinement : refinements) {
			System.out.println(refinement);
		}		
	}
		
	@Test
	public void rhoDRDownTest5() throws ParseException, LearningProblemUnsupportedException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.SWORE);
		reasoner.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
//		Description concept = KBParser.parseConcept("((NOT \"http://ns.softwiki.de/req/Requirement\") OR (ALL \"http://ns.softwiki.de/req/isCreatedBy\".(NOT \"http://ns.softwiki.de/req/Creditor\")))");
		Description concept = KBParser.parseConcept("(NOT \"http://ns.softwiki.de/req/Requirement\" OR ALL \"http://ns.softwiki.de/req/isCreatedBy\".NOT \"http://ns.softwiki.de/req/Creditor\")");
		System.out.println(concept);
		Set<Description> refinements = op.refine(concept, 7);
		for(Description refinement : refinements) {
			System.out.println(refinement);
		}		
	}	
	
	@Test
	public void invertedOperatorTest() throws ParseException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.RHO1);
		reasoner.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.setDropDisjuncts(true);
		op.init();
		
		RefinementOperator operator = new OperatorInverter(op);
		Description concept = KBParser.parseConcept("(limo AND EXISTS hasOwner.man)");
		Set<Description> refinements = operator.refine(concept, 6);
		for(Description refinement : refinements) {
			System.out.println(refinement);
		}		
		// we should get four upward refinements 
		// (replacing limo => car, man => person, or drop one of the intersects)
		assertTrue(refinements.size()==4);
	}
	
	@Test
	public void rhoDownTestPellet() throws ComponentInitException {
		Logger.getRootLogger().setLevel(Level.TRACE);
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER);
		reasoner.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		NamedClass nc = new NamedClass("http://example.com/father#male");
		Set<Description> refinements = op.refine(nc, 5);
		for(Description refinement : refinements) {
			System.out.println(refinement);
		}		
		// refinements should be as follows:
		//		(male AND (NOT male)) 
		//		(male AND (female OR female)) 
		//		(female AND male AND male)
		//		(male AND ALL hasChild.TOP) 
		//		(male AND (female OR male)) 
		//		(male AND male AND male) 
		//		(male AND (NOT female)) 
		//		(male AND EXISTS hasChild.TOP) 
//		System.out.println(rs);
//		System.out.println("most general properties: " + rs.getMostGeneralProperties());
		System.out.println(reasoner.getObjectPropertyHierarchy());
		assertTrue(refinements.size()==8);		
	}
	
	private String uri(String name) {
		return "\""+baseURI+name+"\"";
	}
}
