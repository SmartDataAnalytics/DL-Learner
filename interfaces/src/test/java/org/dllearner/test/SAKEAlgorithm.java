package org.dllearner.test;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.StringRenderer;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AccMethodPredAcc;
import org.dllearner.learningproblems.AccMethodPredAccOCEL;
import org.dllearner.learningproblems.AccMethodTwoValued;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Execute the SAKE Algoritnm
 */
public class SAKEAlgorithm {
	final static String NS = "http://www.ontos.com/sake/extractor#";

	@Test
	public void testSomething2() throws ComponentInitException, MalformedURLException {
		boolean sparql = true;
		StringRenderer.setRenderer(StringRenderer.Rendering.DL_SYNTAX);

		AbstractKnowledgeSource ks;
		AbstractReasonerComponent rc;
		SPARQLReasoner sparqlReasoner;
		if (sparql) {
			SparqlEndpointKS sparqlEndpointKS = new SparqlEndpointKS();
			sparqlEndpointKS.setUrl(new URL("http://sake.informatik.uni-leipzig.de:8850/sparql"));
			sparqlEndpointKS.setDefaultGraphURIs(Collections.singletonList("http://sake-projekt.de"));
			sparqlEndpointKS.setUseCache(false);
			sparqlEndpointKS.init();
			sparqlReasoner = new SPARQLReasoner(sparqlEndpointKS);
			sparqlReasoner.tryPropertyPath = true;
			ks = sparqlEndpointKS;
			rc = sparqlReasoner;
		} else {
			OWLFile owlFile = new OWLFile();
			owlFile.setFileName(System.getenv("HOME")+"/sake-ont/SAKE-event-ontology_v3-plus.ttl");
			owlFile.init();
			OWLAPIReasoner owlapiReasoner = new OWLAPIReasoner(owlFile);
			ks = owlFile;
			rc = owlapiReasoner;
		}
		rc.init();

		System.err.println(""+rc.getClassHierarchy());
		Map<OWLObjectProperty, OWLClassExpression> opRanges = rc.getObjectPropertyRanges();
		System.err.println("opRanges="+ opRanges);


		OWLDataFactory df = new OWLDataFactoryImpl();

		RhoDRDown op = new RhoDRDown();
		op.setReasoner(rc);
		//if (sparql) {

		//} else {
			op.setInstanceBasedDisjoints(false);
		//}
		op.init();

		OWLClassExpression currDomain = opRanges.get(df.getOWLObjectProperty(IRI.create(NS + "containsFailureData")));
		Set<OWLClassExpression> refinement1 = op.refine(df.getOWLThing(), 1, null,
				currDomain);
		System.err.println("domain:"+currDomain);
		System.err.println("refinement:"+ refinement1);
		for (OWLClassExpression ce : refinement1) {
			System.err.println("refinement of " + ce + ": "+op.refine(ce, 1, null, currDomain));
		}
	}

	@Test
	public void testSomething() throws ComponentInitException, MalformedURLException {
		StringRenderer.setRenderer(StringRenderer.Rendering.DL_SYNTAX);

		SparqlEndpointKS ks = new SparqlEndpointKS();
		ks.setUrl(new URL("http://sake.informatik.uni-leipzig.de:8850/sparql"));
		ks.setDefaultGraphURIs(Collections.singletonList("http://sake-projekt.de"));
		ks.setUseCache(false);
		ks.init();
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks);
		sparqlReasoner.tryPropertyPath = true;
		AbstractReasonerComponent rc = sparqlReasoner;
		rc.init();

		OWLDataFactory df = new OWLDataFactoryImpl();

		OWLClassExpression description = OWLAPIUtils.fromManchester("Event and (containsFailureData some owl:Thing)", rc, df, true);
		OWLClassExpression startClass = OWLAPIUtils.fromManchester("Event", rc, df, true);
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(rc);
		op.setLengthMetric(OWLClassExpressionLengthMetric.getOCELMetric());
		op.setStartClass(startClass);
		//op.setClassHierarchy(rc.getClassHierarchy());
		//op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
		//op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
			op.setInstanceBasedDisjoints(false);
		op.setUseCardinalityRestrictions(false);
		op.init();

		System.err.println("ce="+description);
		Set<OWLClassExpression> refinements = op.refine(description, 4);
		for (OWLClassExpression r : refinements) {
			System.err.println("`" + r);
		}

		SortedSet<OWLIndividual> nonFatalEvents = rc.getIndividuals(
				OWLAPIUtils.fromManchester("containsFailureData some (FailureData and not FailureData_999999)", rc, df, true));
		System.out.println(nonFatalEvents.size());
		SortedSet<OWLIndividual> fatalEvents = rc.getIndividuals(
				OWLAPIUtils.fromManchester("containsFailureData some FailureData_999999", rc, df, true));
		System.out.println(fatalEvents.size());
		sparqlReasoner.tryPropertyPath=false;

		ArrayList<OWLIndividual> samplingList = new ArrayList<OWLIndividual>(nonFatalEvents);
		Collections.shuffle(samplingList);

		for (int i = 0; i < 5; i++) {
			System.err.println("now using sample " + (i + 1));
//			AccMethodPredAccWeighted ac = new AccMethodPredAccWeighted(true);
//			ac.setBalanced(true);
			AccMethodTwoValued ac = new AccMethodPredAccOCEL(true);

			PosNegLPStandard lp = new PosNegLPStandard(rc);
			lp.setPositiveExamples(fatalEvents);
			HashSet<OWLIndividual> negativeExamples = new HashSet<>(samplingList.subList(i * fatalEvents.size(), (i + 1) * fatalEvents.size()));
			lp.setNegativeExamples(negativeExamples);
			System.err.println("sample=" + negativeExamples.size());
			lp.setAccuracyMethod(ac);
			lp.init();


			for (OWLClassExpression r : refinements) {
				System.err.println("`" + r + ", " + lp.getAccuracyOrTooWeak(r, 1));
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException, ComponentInitException {
		StringRenderer.setRenderer(StringRenderer.Rendering.DL_SYNTAX);

		SparqlEndpointKS ks = new SparqlEndpointKS();
		ks.setUrl(new URL("http://sake.informatik.uni-leipzig.de:8850/sparql"));
		ks.setDefaultGraphURIs(Collections.singletonList("http://sake-projekt.de"));
		ks.setUseCache(false);
		ks.init();
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks);
		sparqlReasoner.tryPropertyPath = true;
		AbstractReasonerComponent rc = sparqlReasoner;
		rc.init();

		OWLDataFactory df = new OWLDataFactoryImpl();
		System.err.println("class hierarchy="+rc.getClassHierarchy());
		System.err.println("op hierarchy="+rc.getObjectPropertyHierarchy());
		System.err.println("dp hierarchy="+rc.getDatatypePropertyHierarchy());
		Set<OWLClass> classes = rc.getClasses();
		//System.out.println("classes = " + classes);
		SortedSet<OWLIndividual> failureEvents = rc.getIndividuals(
				OWLAPIUtils.fromManchester("containsFailureData some FailureData", rc, df, true));
		System.out.println(failureEvents.size());
		SortedSet<OWLIndividual> nonFatalEvents = rc.getIndividuals(
				OWLAPIUtils.fromManchester("containsFailureData some (FailureData and not FailureData_999999)", rc, df, true));
		System.out.println(nonFatalEvents.size());
		SortedSet<OWLIndividual> fatalEvents = rc.getIndividuals(
				OWLAPIUtils.fromManchester("containsFailureData some FailureData_999999", rc, df, true));
		System.out.println(fatalEvents.size());

		ArrayList<OWLIndividual> samplingList = new ArrayList<OWLIndividual>(nonFatalEvents);
		Collections.shuffle(samplingList);

		System.err.println("failure events="+fatalEvents);

		sparqlReasoner.tryPropertyPath=false;

		for (int i = 0; i < 5; i++) {
			System.err.println("now using sample " + (i + 1));
			//AccMethodTwoValued ac = new AccMethodPredAccOCEL(true);
			AccMethodTwoValued ac = new AccMethodPredAcc(true);

			PosNegLPStandard lp = new PosNegLPStandard(rc);
			lp.setPositiveExamples(fatalEvents);
			HashSet<OWLIndividual> negativeExamples = new HashSet<>(samplingList.subList(i * fatalEvents.size(), (i + 1) * fatalEvents.size()));
			lp.setNegativeExamples(negativeExamples);
			System.err.println("sample=" + negativeExamples);
			lp.setAccuracyMethod(ac);
			lp.init();


			OWLClassExpression startClass = OWLAPIUtils.fromManchester("Event", rc, df, true);

			RhoDRDown op = new RhoDRDown();
			op.setReasoner(rc);
			op.setLengthMetric(OWLClassExpressionLengthMetric.getOCELMetric());
			op.setStartClass(startClass);
			//op.setClassHierarchy(rc.getClassHierarchy());
			//op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
			//op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
			op.setUseCardinalityRestrictions(false);
			op.setInstanceBasedDisjoints(false);
			op.setUseBooleanDatatypes(false);
			op.init();

			OEHeuristicRuntime h = new OEHeuristicRuntime();
			h.setExpansionPenaltyFactor(0.001);
			h.setStartNodeBonus(0);
			h.init();

			CELOE celoe = new CELOE(lp, rc);
			celoe.setIgnoredObjectProperties(Collections.singleton(
					df.getOWLObjectProperty(IRI.create(NS + "hasTelegram"))));
			Set<OWLClass> ignoredConcepts = new HashSet<>();
			ignoredConcepts.add((OWLClass) OWLAPIUtils.fromManchester("FailureData_999999", rc, df, true));
			ignoredConcepts.add((OWLClass) OWLAPIUtils.fromManchester("Context_11", rc, df, true));
			celoe.setOperator(op);
			celoe.setStartClass(startClass);
			celoe.setMaxExecutionTimeInSeconds(600);
			celoe.setHeuristic(h);
			celoe.setReplaceSearchTree(true);
			celoe.setWriteSearchTree(true);
			celoe.setSearchTreeFile("sake.tree");
			celoe.setMaxDepth(15);
			celoe.init();

			celoe.start();
		}
	}
}
