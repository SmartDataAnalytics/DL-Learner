/**
 * 
 */
package org.dllearner.kb.sparql;

import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractSampleGenerator {
	
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	private int sampleDepth = 2;

	protected QueryExecutionFactory qef;

	public AbstractSampleGenerator(SparqlEndpointKS ks) {
		this(ks.getQueryExecutionFactory());
	}
	
	public AbstractSampleGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen.setRecursionDepth(sampleDepth);
		cbdGen.addPropertiesToIgnore(Sets.newHashSet(OWL.sameAs.getURI()));
	}
	
	/**
	 * Computes a sample of the knowledge base, i.e. it contains only facts
	 * about the positive and negative individuals.
	 * @param posExamples
	 * @param negExamples
	 * @return
	 */
	public OWLOntology getSample(Set<OWLIndividual> individuals) {
		return OwlApiJenaUtils.getOWLOntology(getSampleModel(individuals));
	}
	
	/**
	 * @param sampleDepth the maximum sample depth to set
	 */
	public void setSampleDepth(int sampleDepth) {
		this.sampleDepth = sampleDepth;
	}
	
	/**
	 * @return the maximum sample depth
	 */
	public int getSampleDepth() {
		return sampleDepth;
	}
	
	protected Model getSampleModel(Set<OWLIndividual> individuals) {
		Model model = ModelFactory.createDefaultModel();
		for(OWLIndividual ind : individuals){
			Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID());
			model.add(cbd);
		}
		OWLEntityTypeAdder.addEntityTypes(model);
		return model;
	}

}
