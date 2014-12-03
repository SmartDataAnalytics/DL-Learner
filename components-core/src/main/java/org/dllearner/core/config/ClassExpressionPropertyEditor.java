package org.dllearner.core.config;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

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
		// we assume that the start class string is given in Manchester syntax
//		System.out.println("parser string: " + arg0);
		ManchesterOWLSyntaxClassExpressionParser parser = new ManchesterOWLSyntaxClassExpressionParser(
				new OWLDataFactoryImpl(false, false), 
				null);
		try {
			description = parser.parse(s);
//			System.out.println("parsed: " + description);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
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
