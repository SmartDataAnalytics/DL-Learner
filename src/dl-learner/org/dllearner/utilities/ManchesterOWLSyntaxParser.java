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
package org.dllearner.utilities;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.expression.ParserException;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * Parser for Manchester Syntax strings (interface to OWL API parser).
 * TODO: Currently, this outputs an OWL API OWLDescription, but there
 * is no converter from OWL API descriptions to DL-Learner descriptions
 * at the moment.
 * 
 * @author Jens Lehmann
 *
 */
public class ManchesterOWLSyntaxParser {
	
	public OWLDescription getDescription(String manchesterSyntaxDescription) throws ParserException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		ManchesterOWLSyntaxEditorParser parser = new
		ManchesterOWLSyntaxEditorParser(manager.getOWLDataFactory(), manchesterSyntaxDescription);

		return parser.parseDescription();
	}

}
