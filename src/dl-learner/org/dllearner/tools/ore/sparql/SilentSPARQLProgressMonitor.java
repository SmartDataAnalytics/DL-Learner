package org.dllearner.tools.ore.sparql;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public class SilentSPARQLProgressMonitor implements SPARQLProgressMonitor{

	@Override
	public void inconsistencyFound(Set<OWLAxiom> explanation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIndeterminate(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgress(long progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSize(long size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStarted() {
		// TODO Auto-generated method stub
		
	}

}
