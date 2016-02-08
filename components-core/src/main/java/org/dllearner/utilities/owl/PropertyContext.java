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
package org.dllearner.utilities.owl;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * A property context is a utility class which specifies the
 * position of constructs with respect to properties of a 
 * construct in a class description. For instance, the A
 * in \exists r.\exists s.A occurs in property context [r,s].
 * 
 * @author Jens Lehmann
 *
 */
public class PropertyContext extends LinkedList<OWLObjectProperty> implements Comparable<PropertyContext> {

	private static final long serialVersionUID = -4403308689522524077L;
	private static final OWLClass OWL_THING = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());
	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(@NotNull PropertyContext context) {
		// we first distinguish on size - simpler contexts come first
		int diff = context.size() - size();
		if(diff != 0) {
			return diff;
		}
			
		for(int i=0; i<size(); i++) {
			int cmp = get(i).toStringID().compareTo(context.get(i).toStringID());
			if(cmp != 0) {
				return cmp;
			}
		}
		
		return 0;
	}

	/**
	 * Transforms context [r,s] to \exists r.\exists s.\top.
	 * @return A OWLClassExpression with existential quantifiers and \top corresponding
	 * to the context.
	 */
	public OWLClassExpression toExistentialContext() {
		OWLClassExpression d = OWL_THING;
		for(int i = size()-1; i>=0; i--) {
			d = df.getOWLObjectSomeValuesFrom(get(i), d);
		}
		return d;
	}
	
}
