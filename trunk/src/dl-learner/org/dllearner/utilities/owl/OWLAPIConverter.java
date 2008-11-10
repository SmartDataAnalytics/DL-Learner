/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.utilities.owl;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;

/**
 * A collection of methods for exchanging objects between OWL API and
 * DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public final class OWLAPIConverter {

	/**
	 * Converts a DL-Learner axiom into an OWL API axiom.
	 * 
	 * @see OWLAPIAxiomConvertVisitor#convertAxiom(Axiom)
	 * @param axiom The axiom to convert.
	 * @return An OWL API axiom.
	 */
	public static OWLAxiom convertAxiom(Axiom axiom) {
		return OWLAPIAxiomConvertVisitor.convertAxiom(axiom);
	}	
	
	/**
	 * Converts a DL-Learner description into an OWL API description.
	 * 
	 * @see OWLAPIDescriptionConvertVisitor#getOWLDescription(Description)
	 * @param description DL-Learner description.
	 * @return Corresponding OWL API description.
	 */
	public static OWLDescription getOWLDescription(Description description) {
		return OWLAPIDescriptionConvertVisitor.getOWLDescription(description);
	}	
	
	
}
