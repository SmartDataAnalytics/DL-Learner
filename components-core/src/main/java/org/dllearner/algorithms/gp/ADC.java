package org.dllearner.algorithms.gp;

import java.util.Map;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;
import org.dllearner.core.owl.KBElementVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitor;

/**
 * ADC stand for "automatically defined concept". It is used for
 * concept invention in the Genetic Programming Algorithm. 
 * However, it is not used 
 * 
 * @author Jens Lehmann
 *
 */
public class ADC extends Description {

	/*
	@Override
	protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
		posSet = adcPosSet;
		negSet = adcNegSet;
	}
	*/

	private static final long serialVersionUID = -3820156025424386445L;

	public int getLength() {
		// ein ADC-Knoten hat Laenge 1, da effektiv nur ein Knoten benoetigt wird
		// um die Gesamtlaenge des gelernten Konzepts zu haben, muss man natuerlich
		// noch zusaetzliche die Laenge der ADC addieren
		return 1;
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		return "ADC";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "ADC";
	}
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString()
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public int getArity() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#accept(org.dllearner.core.owl.DescriptionVisitor)
	 */
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLDescription#accept(org.semanticweb.owl.model.OWLDescriptionVisitor)
	 */
	public void accept(OWLClassExpressionVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLDescription#asOWLClass()
	 */
	public OWLClass asOWLClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLDescription#isAnonymous()
	 */
	public boolean isAnonymous() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLDescription#isOWLNothing()
	 */
	public boolean isOWLNothing() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLDescription#isOWLThing()
	 */
	public boolean isOWLThing() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObject#accept(org.semanticweb.owl.model.OWLObjectVisitor)
	 */
	public void accept(OWLObjectVisitor arg0) {
		// TODO Auto-generated method stub
		
	}



}
