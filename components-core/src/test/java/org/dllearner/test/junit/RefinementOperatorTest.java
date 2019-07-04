/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

/**
 * A suite of JUnit tests related to refinement operators.
 * 
 * @author Jens Lehmann
 *
 */
public class RefinementOperatorTest {

	private String baseURI;
	
	/**
	 * Applies the RhoDRDown operator to a concept and checks that the number of
	 * refinements is correct.
	 *
	 */
	@Test
	public void rhoDRDownTest() {
		try {
			StringRenderer.setRenderer(Rendering.DL_SYNTAX);
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
			OWLClassExpressionLengthMetric metric = new OWLClassExpressionLengthMetric();
			metric.dataHasValueLength = 1;
			op.setLengthMetric(metric);
			op.init();
			OWLClassExpression concept = KBParser.parseConcept(uri("Compound"));
			Set<OWLClassExpression> results = op.refine(concept, 4, null);

			for(OWLClassExpression result : results) {
				System.out.println(result);
			}
			
			int desiredResultSize = 141;
			assertTrue(results.size() + " results found, but should be " + desiredResultSize + ".",results.size()==desiredResultSize);
		} catch(ComponentInitException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void rhoDRDownTest2() throws ParseException, ComponentInitException {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.EPC_OE);
		baseURI = reasoner.getBaseURI();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		OWLClassExpression concept = KBParser.parseConcept("(\"http://localhost/aris/sap_model.owl#EPC\" AND EXISTS \"http://localhost/aris/sap_model.owl#hasModelElements\".\"http://localhost/aris/sap_model.owl#Object\")");
		Set<OWLClassExpression> results = op.refine(concept,10);

		for(OWLClassExpression result : results) {
			System.out.println(result);
		}
			
		int desiredResultSize = 107;
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
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		OCEL la = new OCEL(lp, reasoner);
		
		Set<OWLClass> ignoredConcepts = new TreeSet<>();
		ignoredConcepts.add(new OWLClassImpl(IRI.create("http://www.test.de/test#ZERO")));
		ignoredConcepts.add(new OWLClassImpl(IRI.create("http://www.test.de/test#ONE")));
		Set<OWLClass> usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		
		ClassHierarchy classHierarchy = (ClassHierarchy) reasoner.getClassHierarchy().cloneAndRestrict(new HashSet<OWLClassExpression>(usedConcepts)); 
		classHierarchy.thinOutSubsumptionHierarchy();
		
		System.out.println(" UNIT TEST INCOMPLETE AFTER FRAMEWORK CHANGE, BECAUSE CLASS HIERARCHY IS NOT PASSED TO REFINEMENT OPERATOR ");
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
		OWLClassExpression concept = KBParser.parseConcept("EXISTS \"http://www.test.de/test#hasPiece\".EXISTS \"http://www.test.de/test#hasLowerRankThan\".(\"http://www.test.de/test#WRook\" AND TOP)");
		Set<OWLClassExpression> results = op.refine(concept,8);

		for(OWLClassExpression result : results) {
			System.out.println(result);
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
		
		OWLClassExpression concept = KBParser.parseConcept("(car AND EXISTS hasOwner.person)");
//		Description concept = Thing.instance;
		Set<OWLClassExpression> refinements = op.refine(concept, 6);
		for(OWLClassExpression refinement : refinements) {
			System.out.println(refinement);
		}		
	}
		
	@Test
	public void rhoDRDownTest5() throws ParseException, LearningProblemUnsupportedException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.SWORE);

		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.init();
		
//		Description concept = KBParser.parseConcept("((NOT \"http://ns.softwiki.de/req/Requirement\") OR (ALL \"http://ns.softwiki.de/req/isCreatedBy\".(NOT \"http://ns.softwiki.de/req/Creditor\")))");
		OWLClassExpression concept = KBParser.parseConcept("(NOT \"http://ns.softwiki.de/req/Requirement\" OR ALL \"http://ns.softwiki.de/req/isCreatedBy\".NOT \"http://ns.softwiki.de/req/Creditor\")");
		System.out.println(concept);
		Set<OWLClassExpression> refinements = op.refine(concept, 8);
		for(OWLClassExpression refinement : refinements) {
			System.out.println(refinement);
		}		
	}	
	

	/**
	 * Applies the RhoDRDown operator to a concept and checks that the number of
	 * refinements does not exceed the requested length.
	 *
	 */
	@Test
	public void rhoDRDownTest6() {
		try {
			String file = "../examples/carcinogenesis/carcinogenesis.owl";
			KnowledgeSource ks = new OWLFile(file);
			AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(ks));
			reasoner.init();
			baseURI = reasoner.getBaseURI();

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.init();
			OWLClassExpression concept = KBParser.parseConcept(uri("Compound"));
			int maxLength = 4;
			Set<OWLClassExpression> results = op.refine(concept, maxLength, null);

			int tooLong = 0;
			for(OWLClassExpression result : results) {
				if (OWLClassExpressionUtils.getLength(result) > maxLength) {
					tooLong++;
				}
			}

			if(tooLong!= 0) {
				System.out.println(tooLong + " refinements were longer than " + maxLength);
			}
			assertTrue(tooLong==0);
		} catch(ComponentInitException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void rhoDRDownTest7() {
		try {
			StringRenderer.setRenderer(Rendering.DL_SYNTAX);
			String file = "../examples/family/father_oe_inv.ttl";
			KnowledgeSource ks = new OWLFile(file);
			AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(ks));
			reasoner.init();
			baseURI = reasoner.getBaseURI();

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setUseInverse(true);
			op.setUseHasValueConstructor(true);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.init();
			OWLClassExpression concept = KBParser.parseConcept(uri("person"));
			Set<OWLClassExpression> results = op.refine(concept, 5, null);

			for(OWLClassExpression result : results) {
				System.out.println(result);
			}

			int desiredResultSize = 11;
			if(results.size() != desiredResultSize) {
				System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
			}
			assertTrue(results.size()==desiredResultSize);
		} catch(ComponentInitException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHasValue() {
		try {
			StringRenderer.setRenderer(Rendering.DL_SYNTAX);

			String s = "@prefix : <http://test.org/> . @prefix owl: <http://www.w3.org/2002/07/owl#> ." +
					":p a owl:ObjectProperty .";
			for (int i = 0; i < 10; i++) {
				s += ":a" + i + ":p :b .";
			}
			for (int i = 0; i < 20; i++) {
				s += ":a" + i + " :p :c .";
			}
			for (int i = 0; i < 10; i++) {
				s += ":y  :p :x" + i + " .";
			}
			for (int i = 0; i < 20; i++) {
				s += ":z  :p :x" + i + " .";
			}
			OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(s.getBytes()));
			KnowledgeSource ks = new OWLAPIOntology(ont);
			ks.init();

//			ont.getLogicalAxioms().forEach(System.out::println);

			AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(ks));
			reasoner.init();
			baseURI = reasoner.getBaseURI();

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setUseInverse(true);
			op.setUseHasValueConstructor(true);
			op.setFrequencyThreshold(5);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.init();
			OWLClassExpression concept = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());
			Set<OWLClassExpression> results = op.refine(concept, 5, null);

			for(OWLClassExpression result : results) {
				System.out.println(result);
			}

			int desiredResultSize = 9;
			if(results.size() != desiredResultSize) {
				System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
			}
			assertTrue(results.size()==desiredResultSize);
		} catch(ComponentInitException | OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHasSelf() {
		try {
			StringRenderer.setRenderer(Rendering.DL_SYNTAX);

			String s = "@prefix : <http://test.org/> . @prefix owl: <http://www.w3.org/2002/07/owl#> ." +
					":p a owl:ObjectProperty .";
			for (int i = 0; i < 10; i++) {
				s += ":a" + i + ":p :a .";
			}
			for (int i = 10; i < 20; i++) {
				s += ":a" + i + " :p :b .";
			}

			OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(s.getBytes()));
			KnowledgeSource ks = new OWLAPIOntology(ont);
			ks.init();

//			ont.getLogicalAxioms().forEach(System.out::println);

			AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(ks));
			reasoner.init();
			baseURI = reasoner.getBaseURI();

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setUseInverse(true);
			op.setUseHasSelf(true);
			op.setFrequencyThreshold(5);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.init();
			OWLClassExpression concept = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());
			Set<OWLClassExpression> results = op.refine(concept, 5, null);

			for(OWLClassExpression result : results) {
				System.out.println(result);
			}

			int desiredResultSize = 6;
			if(results.size() != desiredResultSize) {
				System.out.println(results.size() + " results found, but should be " + desiredResultSize + ".");
			}
			assertTrue(results.size()==desiredResultSize);
		} catch(ComponentInitException | OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void invertedOperatorTest() throws ParseException, ComponentInitException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.RHO1);

		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setSubHierarchy(reasoner.getClassHierarchy());
		op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		op.setDropDisjuncts(true);
		op.init();
		
		LengthLimitedRefinementOperator operator = new OperatorInverter(op);
		OWLClassExpression concept = KBParser.parseConcept("(limo AND EXISTS hasOwner.man)");
		Set<OWLClassExpression> refinements = operator.refine(concept, 6);
		for(OWLClassExpression refinement : refinements) {
			System.out.println(refinement);
		}		
		// we should get four upward refinements 
		// (replacing limo => car, man => person, or drop one of the intersects)
		assertTrue(refinements.size()==4);
	}
	
	@Test
	public void rhoDownTestPellet() throws ComponentInitException {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		Level oldLevel = Logger.getRootLogger().getLevel();
		try {
			Logger.getRootLogger().setLevel(Level.TRACE);
			AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER);
			reasoner.init();

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(reasoner);
			op.setUseSomeOnly(false);
			op.setSubHierarchy(reasoner.getClassHierarchy());
			op.setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			op.setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
			op.setUseSomeOnly(false);
			op.init();

			OWLClass nc = new OWLClassImpl(IRI.create("http://example.com/father#male"));
			Set<OWLClassExpression> refinements = op.refine(nc, 5);
			for (OWLClassExpression refinement : refinements) {
				System.out.println(refinement);
			}
			// refinements should be as follows:
			//	male ⊓ male
			//	male ⊓ (male   ⊔ male)
			//	male ⊓ (female ⊔ male)
			//	male ⊓ (female ⊔ female)
			//	male ⊓ (¬male)
			//	male ⊓ (¬female)
			//	male ⊓ (∃ hasChild.⊤)
			//	male ⊓ (∀ hasChild.⊤)
//		System.out.println(rs);
//		System.out.println("most general properties: " + rs.getMostGeneralProperties());
			System.out.println(reasoner.getObjectPropertyHierarchy());
			assertTrue(refinements.size() + " results found, but should be " + 8 + ".", refinements.size() == 8);
		} finally {
			Logger.getRootLogger().setLevel(oldLevel);
		}
	}
	
	private String uri(String name) {
		return "\""+baseURI+name+"\"";
	}
}
