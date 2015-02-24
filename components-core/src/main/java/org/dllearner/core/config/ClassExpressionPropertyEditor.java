package org.dllearner.core.config;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class ClassExpressionPropertyEditor implements PropertyEditor {

	private OWLClassExpression description;
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAsText() {
		return OWLAPIRenderers.toManchesterOWLSyntax(description);
	}

	@Override
	public Component getCustomEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJavaInitializationString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue() {
		return description;
	}

	@Override
	public boolean isPaintable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void paintValue(Graphics arg0, Rectangle arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		description = new OWLClassImpl(IRI.create(s));
		
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

	@Override
	public void setValue(Object arg0) {
		description = (OWLClassExpression) arg0;
	}

	@Override
	public boolean supportsCustomEditor() {
		// TODO Auto-generated method stub
		return false;
	}

}
