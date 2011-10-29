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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.dllearner.algorithms.gp.ADC;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeExactCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMaxCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMinCardinalityRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.OWL2Datatype;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectOneOf;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.SimpleDoubleDataRange;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.UntypedConstant;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * Converter from DL-Learner descriptions to OWL API descriptions based
 * on the visitor pattern.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIDescriptionConvertVisitor implements DescriptionVisitor {

	// private OWLDescription description;
	private Stack<OWLClassExpression> stack = new Stack<OWLClassExpression>();
	
	private OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	public OWLClassExpression getOWLClassExpression() {
		return stack.pop();
	}
	
	/**
	 * Converts a DL-Learner description into an OWL API decription.
	 * @param description DL-Learner description.
	 * @return Corresponding OWL API description.
	 */
	public static OWLClassExpression getOWLClassExpression(Description description) {
		OWLAPIDescriptionConvertVisitor converter = new OWLAPIDescriptionConvertVisitor();
		description.accept(converter);
		return converter.getOWLClassExpression();
	}
	
	/**
	 * Used for testing the OWL API converter.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Description d = KBParser.parseConcept("(male AND (rich OR NOT stupid))");
			OWLClassExpression od = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d);
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
		OWLClassExpression d = stack.pop();
		stack.push(factory.getOWLObjectComplementOf(d));		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLClassExpression d = stack.pop();
		stack.push(factory.getOWLObjectAllValuesFrom(role, d));		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLClassExpression d = stack.pop();
		stack.push(factory.getOWLObjectSomeValuesFrom(role, d));
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
		Set<OWLClassExpression> descriptions = new HashSet<OWLClassExpression>();
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
		Set<OWLClassExpression> descriptions = new HashSet<OWLClassExpression>();
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
		// TODO Taken from ObjectSomeRestriction above, hope its correct
		//throw new Error("OWLAPIDescriptionConverter: not implemented");
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLClassExpression d = stack.pop();
		int minmax = description.getCardinality();
		stack.push(factory.getOWLObjectMinCardinality(minmax, role, d));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		// TODO Taken from ObjectSomeRestriction above, hope its correct
		//throw new Error("OWLAPIDescriptionConverter: not implemented");
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLClassExpression d = stack.pop();
		int minmax = description.getCardinality();
		stack.push(factory.getOWLObjectExactCardinality(minmax, role, d));
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		// TODO Taken from ObjectSomeRestriction above, hope its correct
		//throw new Error("OWLAPIDescriptionConverter: not implemented");
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(description.getRole().getName()));
		description.getChild(0).accept(this);
		OWLClassExpression d = stack.pop();
		int minmax = description.getCardinality();
		stack.push(factory.getOWLObjectMaxCardinality(minmax, role, d));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(((ObjectProperty)description.getRestrictedPropertyExpression()).getName()));
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(description.getIndividual().getName()));
		stack.push(factory.getOWLObjectHasValue(role, i));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		// convert OWL constant to OWL API constant
		Constant c = description.getValue();
		OWLLiteral constant = convertConstant(c);
		/*
		if(c instanceof TypedConstant) {
			Datatype dt = ((TypedConstant)c).getDatatype();
			OWLDataType odt = convertDatatype(dt);
			constant = factory.getOWLTypedConstant(c.getLiteral(), odt);
		} else {
			UntypedConstant uc = (UntypedConstant) c;
			if(uc.hasLang()) {
				constant = factory.getOWLUntypedConstant(uc.getLiteral(), uc.getLang());
			} else {
				constant = factory.getOWLUntypedConstant(uc.getLiteral());
			}
		}
		*/
				
		// get datatype property
		DatatypeProperty dtp = description.getRestrictedPropertyExpression();
		OWLDataProperty prop = factory.getOWLDataProperty(IRI.create(dtp.getName()));
		
		stack.push(factory.getOWLDataHasValue(prop, constant));	
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		stack.push(factory.getOWLClass(IRI.create(description.getName())));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.algorithms.gp.ADC)
	 */
	public void visit(ADC description) {
		// TODO Auto-generated method stub
		throw new Error("OWLAPIDescriptionConverter: not implemented");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMinCardinalityRestriction)
	 */
	public void visit(DatatypeMinCardinalityRestriction description) {
		// TODO Auto-generated method stub
		throw new Error("OWLAPIDescriptionConverter: not implemented");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeExactCardinalityRestriction)
	 */
	public void visit(DatatypeExactCardinalityRestriction description) {
		// TODO Auto-generated method stub
		throw new Error("OWLAPIDescriptionConverter: not implemented");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMaxCardinalityRestriction)
	 */
	public void visit(DatatypeMaxCardinalityRestriction description) {
		// TODO Auto-generated method stub
		throw new Error("OWLAPIDescriptionConverter: not implemented");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeSomeRestriction)
	 */
	public void visit(DatatypeSomeRestriction description) {
		
		// TODO: currently works only for double min/max
		
		DatatypeProperty dp = (DatatypeProperty) description.getRestrictedPropertyExpression();
		// currently only double restrictions implemented
		SimpleDoubleDataRange dr = (SimpleDoubleDataRange) description.getDataRange();
		double value = dr.getValue();
		
		OWLDatatype doubleDataType = factory.getOWLDatatype(XSDVocabulary.DOUBLE.getIRI());
        OWLLiteral constant = factory.getOWLLiteral(value);

        OWLFacet facet;
        if(dr instanceof DoubleMinValue)
        	facet = OWLFacet.MIN_INCLUSIVE;
        else 
        	facet = OWLFacet.MAX_INCLUSIVE;
        
        OWLDataRange owlDataRange = factory.getOWLDatatypeRestriction(doubleDataType, facet, constant);
        OWLDataProperty odp = factory.getOWLDataProperty(IRI.create(dp.getName()));
        OWLClassExpression d = factory.getOWLDataSomeValuesFrom(odp, owlDataRange);

		stack.push(d);	
	}

	public OWLDatatype convertDatatype(Datatype datatype) {
		if(datatype.equals(OWL2Datatype.BOOLEAN.getDatatype()))
			return factory.getBooleanOWLDatatype();
		else if(datatype.equals(OWL2Datatype.INT.getDatatype()))
			return factory.getIntegerOWLDatatype();
		else if(datatype.equals(OWL2Datatype.DOUBLE.getDatatype()))
			return factory.getDoubleOWLDatatype();		
		else if(datatype.equals(OWL2Datatype.STRING.getDatatype()))
			return factory.getOWLDatatype(IRI.create(OWL2Datatype.STRING.getURI()));		
		
		throw new Error("OWLAPIDescriptionConverter: datatype "+datatype+" not implemented");			
	}
	
	private OWLLiteral convertConstant(Constant constant) {
		OWLLiteral owlConstant;
		if(constant instanceof TypedConstant) {
			Datatype dt = ((TypedConstant)constant).getDatatype();
			OWLDatatype odt = convertDatatype(dt);
			owlConstant = factory.getOWLLiteral(constant.getLiteral(), odt);
		} else {
			UntypedConstant uc = (UntypedConstant) constant;
			if(uc.hasLang()) {
				owlConstant = factory.getOWLLiteral(uc.getLiteral(), uc.getLang());
			} else {
				owlConstant = factory.getOWLLiteral(uc.getLiteral(), "");
			}
		}
		return owlConstant;
	}

	@Override
	public void visit(ObjectOneOf description) {
		stack.push(factory.getOWLObjectOneOf(OWLAPIConverter.getOWLAPIIndividuals(description.getIndividuals())));
		
	}
}
