package org.dllearner.algorithms.properties;

import java.util.List;

import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.kb.SparqlEndpointKS;

public class TransitivePropertyAxiomLearner extends Component implements AxiomLearningAlgorithm {
	
	private String propertyToDescribe;
	
	public String getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(String propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	public TransitivePropertyAxiomLearner(SparqlEndpointKS ks){
		
	}
	
	@Override
	public void start() {
	//select COUNT(?o),COUNT(?o2) WHERE {?s dbpedia-owl:influencedBy ?o. ?o dbpedia-owl:influencedBy ?o1. OPTIONAL{?s dbpedia-owl:influencedBy ?o1. ?s dbpedia-owl:influencedBy ?o2}} 

	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}

}
