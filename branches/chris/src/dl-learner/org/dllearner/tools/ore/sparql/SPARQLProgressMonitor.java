package org.dllearner.tools.ore.sparql;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.util.ProgressMonitor;

public interface SPARQLProgressMonitor extends ProgressMonitor{
	
	public void inconsistencyFound(Set<OWLAxiom> explanation);

}
