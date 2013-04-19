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
