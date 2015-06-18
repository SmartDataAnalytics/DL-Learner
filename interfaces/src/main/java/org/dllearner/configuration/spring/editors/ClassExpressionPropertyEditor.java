package org.dllearner.configuration.spring.editors;

import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Basic Property Editor for OWL class expressions.  
 * Doesn't have GUI support yet but we could add that later if we wanted.
 * @author Lorenz Buehmann
 *
 */
public class ClassExpressionPropertyEditor extends AbstractPropertyEditor<OWLClassExpression>{
	
	@Override
	public String getAsText() {
		return OWLAPIRenderers.toManchesterOWLSyntax(value);
	}

	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		value = new OWLClassImpl(IRI.create(s));
		IRI iri = IRI.create("http://dllearner.org/dummy/", s);
		value = new OWLClassImpl(iri);
		
		// TODO seems like the parser needs the ontology to parse class expressions
		// because there is no lookahead, thus, it has to be known in advance
		// which type of entity a token belongs to
		
		/*
		// we assume that the start class string is given in Manchester syntax
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setStringToParse(s);
		try {
			description = parser.parseClassExpression();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		*/
	}
}
