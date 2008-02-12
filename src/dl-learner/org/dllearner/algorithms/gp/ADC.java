package org.dllearner.algorithms.gp;

import java.util.Map;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;

public class ADC extends Description {

	/*
	@Override
	protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
		posSet = adcPosSet;
		negSet = adcNegSet;
	}
	*/

	public int getLength() {
		// ein ADC-Knoten hat Laenge 1, da effektiv nur ein Knoten benoetigt wird
		// um die Gesamtlaenge des gelernten Konzepts zu haben, muss man natuerlich
		// noch zusaetzliche die Laenge der ADC addieren
		return 1;
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		return "ADC";
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

}
