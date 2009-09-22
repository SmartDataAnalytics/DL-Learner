package org.dllearner.tools.ore.explanation;

import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owl.model.OWLAxiom;

public class Explanation implements Comparable<Explanation> {

	private Set<OWLAxiom> axioms;
	private OWLAxiom entailment;
	
	
	public Explanation(OWLAxiom entailment, Set<OWLAxiom> axioms){
		this.entailment = entailment;
		this.axioms = axioms;
	}
	
	public OWLAxiom getEntailment(){
		return entailment;
	}
	
	public Set<OWLAxiom> getAxioms(){
		return axioms;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        if(axioms.isEmpty())
            return "Explanation: <Empty>\n";
        sb.append("Explanation [");
        sb.append(entailment);
        sb.append("]\n");
        for(OWLAxiom ax : (new TreeSet<OWLAxiom>(axioms))){
            sb.append("\t");
            sb.append(ax);
            sb.append("\n");
        }

        return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		Explanation exp = (Explanation)obj;
		return exp.getEntailment().equals(entailment) && exp.getAxioms().equals(axioms);
	}

	@Override
	public int hashCode() {
		return entailment.hashCode() + axioms.hashCode();
	}

	@Override
	public int compareTo(Explanation o) {
		if(axioms.size() == o.axioms.size()){
			return 1;
		} else if(axioms.size() > o.axioms.size()){
			return 1;
		} else {
			return -1;
		}
	}
	
	
}
