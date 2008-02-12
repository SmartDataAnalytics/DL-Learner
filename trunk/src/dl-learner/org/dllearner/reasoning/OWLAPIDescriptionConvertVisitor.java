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
package org.dllearner.reasoning;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.dllearner.algorithms.gp.ADC;
import org.dllearner.core.owl.DatatypeExactCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMaxCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMinCardinalityRestriction;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 * Converter from DL-Learner descriptions to OWL API descriptions based
 * on the visitor pattern.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIDescriptionConvertVisitor implements DescriptionVisitor {

	// private OWLDescription description;
	private Stack<OWLDescription> stack = new Stack<OWLDescription>();
	
	private OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	public OWLDescription getOWLDescription() {
		return stack.pop();
	}
	
	/**
	 * Converts a DL-Learner description into an OWL API decription.
	 * @param description DL-Learner description.
	 * @return Corresponding OWL API description.
	 */
	public static OWLDescription getOWLDescription(Description description) {
		OWLAPIDescriptionConvertVisitor converter = new OWLAPIDescriptionConvertVisitor();
		description.accept(converter);
		return converter.getOWLDescription();
	}
	
	/**
	 * Used for testing the OWL API converter.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Description d = KBParser.parseConcept("(male AND (rich OR NOT stupid))");
			OWLDescription od = OWLAPIDescriptionConvertVisitor.getOWLDescription(d);
			System.out.println(d);
			System.out.println(od);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Negation)
	 */
	public void visit(Negation description) {
		description.getChild(0).accept(this);
		OWLDescription d = stack.pop();
		stack.push(factory.getOWLObjectComplementOf(d));		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				URI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLDescription d = stack.pop();
		stack.push(factory.getOWLObjectAllRestriction(role, d));		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				URI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLDescription d = stack.pop();
		stack.push(factory.getOWLObjectSomeRestriction(role, d));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		stack.push(factory.getOWLNothing());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		stack.push(factory.getOWLThing());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
		for(Description child : description.getChildren()) {
			child.accept(this);
			descriptions.add(stack.pop());
		}
		stack.push(factory.getOWLObjectIntersectionOf(descriptions));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
		for(Description child : description.getChildren()) {
			child.accept(this);
			descriptions.add(stack.pop());
		}
		stack.push(factory.getOWLObjectUnionOf(descriptions));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMinCardinalityRestriction)
	 */
	public void visit(ObjectMinCardinalityRestriction description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		stack.push(factory.getOWLClass(URI.create(description.getName())));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.algorithms.gp.ADC)
	 */
	public void visit(ADC description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMinCardinalityRestriction)
	 */
	public void visit(DatatypeMinCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeExactCardinalityRestriction)
	 */
	public void visit(DatatypeExactCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMaxCardinalityRestriction)
	 */
	public void visit(DatatypeMaxCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeSomeRestriction)
	 */
	public void visit(DatatypeSomeRestriction description) {
		// TODO Auto-generated method stub
		
	}

}
