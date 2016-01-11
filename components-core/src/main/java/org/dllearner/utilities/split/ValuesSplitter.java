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
package org.dllearner.utilities.split;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.Component;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * A values splitter is supposed to return a sorted list of split literals
 * for a data property <code>p</code>, such that those split values can be used
 * in a refinement
 * operator to generate facet restrictions on the value space of <code>p</code>,
 * e.g. <code> p some int[>=100] </code>
 * 
 * @author Lorenz Buehmann
 *
 */
public interface ValuesSplitter extends Component {

	/**
	 * Computes split literals for all applicable data properties in the
	 * ontology
	 * 
	 * @return a map of data properties and their splitting values
	 */
	Map<OWLDataProperty, List<OWLLiteral>> computeSplits();

	/**
	 * Computes split literals for the given data property
	 * 
	 * @param dp the data property
	 * @return a list of split literals
	 */
	List<OWLLiteral> computeSplits(OWLDataProperty dp);

	/**
	 * @return the supported datatypes
	 */
	Set<OWLDatatype> getDatatypes();

}
