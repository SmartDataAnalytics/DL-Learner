/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.parser.ManchesterSyntaxParser;
import org.dllearner.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Parser for Manchester Syntax strings (interface to OWL API parser).
 * 
 * @author Jens Lehmann
 *
 */
public class ManchesterOWLSyntaxParser {
	
	public static OWLClassExpression getOWLAPIDescription(String manchesterSyntaxDescription) throws ParserException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ManchesterOWLSyntaxEditorParser parser = new
		ManchesterOWLSyntaxEditorParser(manager.getOWLDataFactory(), manchesterSyntaxDescription);
		return parser.parseClassExpression();
	}
	
	public static Description getDescription(String manchesterSyntaxDescription) throws ParseException {
//		OWLClassExpression d = getOWLAPIDescription(manchesterSyntaxDescription);
//		return DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(d);
		return ManchesterSyntaxParser.parseClassExpression(manchesterSyntaxDescription);
	}
	

//	public static void main(String args[]) throws ParserException {
//		Description d = ManchesterOWLSyntaxParser.getDescription("hasP SOME A");
//		SparqlQueryDescriptionConvertVisitor.getSparqlQuery(d, 10, false, false);
//	}
	
}
