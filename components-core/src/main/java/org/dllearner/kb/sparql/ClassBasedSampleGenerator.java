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
package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.core.StringRenderer;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.mindswap.pellet.PelletOptions;
import org.openrdf.model.vocabulary.RDF;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 * Computes a sample fragment of the knowledge base given an OWL class.
 * @author Lorenz Buehmann
 *
 */
public class ClassBasedSampleGenerator extends InstanceBasedSampleGenerator{
	
	private Random rnd = new Random(12345);
	
	private int maxNrOfPosExamples = 20;
	private int maxNrOfNegExamples = 20;
	
	private boolean useNegExamples = true;
	
	private AutomaticNegativeExampleFinderSPARQL2 negExamplesFinder;
	
	private Set<OWLIndividual> posExamples;
	private Set<OWLIndividual> negExamples;

	public ClassBasedSampleGenerator(SparqlEndpointKS ks) {
		super(ks);
		
		negExamplesFinder = new AutomaticNegativeExampleFinderSPARQL2(qef);
	}

	/**
	 * Computes a sample fragment of the knowledge base by using instances of the
	 * given OWL class and also, if enabled, use some instances that do not belong to the class.
	 * @param cls the OWL class
	 * @return a sample fragment
	 */
	public OWLOntology getSample(OWLClass cls) {
		// get positive examples
		posExamples = computePosExamples(cls);
		
		// get negative examples if enabled
		negExamples = computeNegExamples(cls, posExamples);
		
		// compute sample based on positive (and negative) examples
		return getSample(Sets.union(posExamples, negExamples));
	}

	/**
	 * @param maxNrOfPosExamples the max. number of pos. examples used for sampling
	 */
	public void setMaxNrOfPosExamples(int maxNrOfPosExamples) {
		this.maxNrOfPosExamples = maxNrOfPosExamples;
	}

	/**
	 * @param maxNrOfNegExamples the max. number of neg. examples used for sampling
	 */
	public void setMaxNrOfNegExamples(int maxNrOfNegExamples) {
		this.maxNrOfNegExamples = maxNrOfNegExamples;
	}

	/**
	 * @param useNegExamples whether to use negative examples or not
	 */
	public void setUseNegExamples(boolean useNegExamples) {
		this.useNegExamples = useNegExamples;
	}
	
	/**
	 * @return the positive examples, i.e. instances of the class used to
	 * generate the sample
	 */
	public Set<OWLIndividual> getPositiveExamples() {
		return posExamples;
	}
	
	/**
	 * @return the negative examples, i.e. individuals that do not belong to the class and are used to
	 * generate the sample
	 */
	public Set<OWLIndividual> getNegativeExamples() {
		return negExamples;
	}

	/**
	 * The examples for the sample are chosen randomly, thus, the seed for shuffling can be set here.
	 * @see Random#setSeed(long)
	 * @param seed the seed
	 */
	public void setSeed(long seed) {
		rnd.setSeed(seed);
	}

	private Set<OWLIndividual> computePosExamples(OWLClass cls) {
		List<OWLIndividual> posExamples = new ArrayList<>();
		
		String query = String.format("SELECT ?s WHERE {?s a <%s>}", cls.toStringID());

		try(QueryExecution qe = qef.createQueryExecution(query)) {
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				posExamples.add(new OWLNamedIndividualImpl(IRI.create(qs.getResource("s").getURI())));
			}
		}

		Collections.shuffle(posExamples, rnd);
		
		return new TreeSet<>(posExamples.subList(0, Math.min(posExamples.size(), maxNrOfPosExamples)));
	}
	
	private Set<OWLIndividual> computeNegExamples(OWLClass cls, Set<OWLIndividual> posExamples) {
		Set<OWLIndividual> negExamples = new TreeSet<>();
		
		if(useNegExamples && maxNrOfPosExamples > 0) {
			negExamples = negExamplesFinder.getNegativeExamples(cls, posExamples, maxNrOfNegExamples);
		}
		
		return negExamples;
	}

	public static void main(String[] args) throws Exception{
		StringRenderer.setRenderer(StringRenderer.Rendering.DL_SYNTAX);

		SparqlEndpoint endpoint = SparqlEndpoint.create("http://dbpedia.org/sparql", "http://dbpedia.org");
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.setUseCache(false);
		ks.setRetryCount(0);
		ks.init();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLOntology schema = man.createOntology();//man.loadOntology(IRI.create("http://downloads.dbpedia.org/2016-10/dbpedia_2016-10.nt"));
		OWLOntology schema = man.loadOntology(IRI.create("http://downloads.dbpedia.org/2016-10/dbpedia_2016-10.nt"));

		ClassBasedSampleGenerator sampleGenerator = new ClassBasedSampleGenerator(ks);
		sampleGenerator.setUseNegExamples(false);
		sampleGenerator.setSampleDepth(2);
		sampleGenerator.addAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
		sampleGenerator.addAllowedObjectNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"));

		PelletOptions.INVALID_LITERAL_AS_INCONSISTENCY = false;
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book"));

		// generate a class based sample
		OWLOntology sample = sampleGenerator.getSample(cls);
		man.addAxioms(sample, schema.getLogicalAxioms());

		Set<String> ignoredProperties = Sets.newHashSet(
				"http://dbpedia.org/ontology/abstract","http://dbpedia.org/ontology/birthName",
				"http://dbpedia.org/ontology/wikiPageID",
				"http://dbpedia.org/ontology/wikiPageRevisionID",
				"http://dbpedia.org/ontology/wikiPageID");
		OWLOntology ont = man.createOntology(schema.getAxioms());
		man.addAxioms(ont, sample.getLogicalAxioms().stream().filter(ax -> {
			if(ax.getAxiomType() == AxiomType.OBJECT_PROPERTY_ASSERTION) {
				return true;
			} else if(ax.getAxiomType() == AxiomType.DATA_PROPERTY_ASSERTION) {
				return !ignoredProperties.contains(((OWLDataPropertyAssertionAxiom)ax).getProperty().asOWLDataProperty().toStringID());
			}
			return true;
		}).collect(Collectors.toSet()));

//		System.out.println("|Sample|=" + sample.getLogicalAxiomCount());
//		sample.getLogicalAxioms().forEach(System.out::println);

		OWLOntologyKnowledgeSource sampleKS = new OWLAPIOntology(ont);
		sampleKS.init();

		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(sampleKS);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.STRUCTURAL);
		baseReasoner.init();

		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(baseReasoner);
//                    reasoner.setReasonerComponent(baseReasoner);
		reasoner.init();

		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.setEquivalence(false);
		lp.setCheckConsistency(false);
		lp.init();

		ELLearningAlgorithm la = new ELLearningAlgorithm(lp, reasoner);
		la.setClassToDescribe(cls);
		la.setNoisePercentage(90);
		la.setMaxNrOfResults(50);
		la.setMaxExecutionTimeInSeconds(10);
//		la.setStartClass(cls);
		la.init();

		la.start();
	}
}
