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
package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OWLLiteralRenamer {
	
	private OWLDataFactory dataFactory;

	public OWLLiteralRenamer(OWLDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	public OWLLiteral rename(OWLLiteral literal){
		OWLLiteral renamedLiteral = literal;
		OWLDatatype datatype = literal.getDatatype();
		if(datatype.isRDFPlainLiteral()){
			if(literal.hasLang()){
				renamedLiteral = dataFactory.getOWLLiteral("plain", literal.getLang());
			} else {
				renamedLiteral = dataFactory.getOWLLiteral("plain");
			}
		} else {
			if(datatype.getIRI().equals(OWL2Datatype.XSD_STRING.getIRI())){
				renamedLiteral = dataFactory.getOWLLiteral("string", datatype);
			} else if(datatype.getIRI().equals(OWL2Datatype.XSD_DOUBLE.getIRI()) ||
					datatype.getIRI().equals(OWL2Datatype.XSD_DECIMAL.getIRI()) || 
					datatype.getIRI().equals(OWL2Datatype.XSD_FLOAT.getIRI())){
				renamedLiteral = dataFactory.getOWLLiteral("1.0", datatype);
			} else if(datatype.getIRI().equals(OWL2Datatype.XSD_INT.getIRI()) ||
					datatype.getIRI().equals(OWL2Datatype.XSD_INTEGER.getIRI()) ||
					datatype.getIRI().equals(OWL2Datatype.XSD_LONG.getIRI()) ||
					datatype.getIRI().equals(OWL2Datatype.XSD_BYTE.getIRI()) ||
					datatype.getIRI().equals(OWL2Datatype.XSD_SHORT.getIRI())){
				renamedLiteral = dataFactory.getOWLLiteral("1", datatype);
			}
		}
		renamedLiteral = dataFactory.getOWLLiteral("", OWL2Datatype.RDF_PLAIN_LITERAL);
		return renamedLiteral;
	}

}
