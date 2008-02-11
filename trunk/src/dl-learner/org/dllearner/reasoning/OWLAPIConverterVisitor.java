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

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.KBElementVisitor;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyInverse;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.core.owl.Union;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * A converter from DL-Learner format to OWL API format.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIConverterVisitor implements KBElementVisitor {

	OWLDataFactory factory;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	
	public OWLAPIConverterVisitor(OWLOntologyManager manager, OWLOntology ontology, KB kb) {
		this.manager = manager;
		this.ontology = ontology;
		factory = manager.getOWLDataFactory();
	}
	
	private void addAxiom(OWLAxiom axiom) {
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		try {
			manager.applyChange(addAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.dl.AxiomVisitor#visit(org.dllearner.core.dl.ObjectPropertyAssertion)
	 */
	public void visit(ObjectPropertyAssertion axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				URI.create(((ObjectPropertyAssertion) axiom).getRole().getName()));
		OWLIndividual i1 = factory.getOWLIndividual(
				URI.create(((ObjectPropertyAssertion) axiom).getIndividual1().getName()));
		OWLIndividual i2 = factory.getOWLIndividual(
				URI.create(((ObjectPropertyAssertion) axiom).getIndividual2().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyAssertionAxiom(i1, role, i2);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.ClassAssertionAxiom)
	 */
	public void visit(ClassAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.DoubleDatatypePropertyAssertion)
	 */
	public void visit(DoubleDatatypePropertyAssertion axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.FunctionalObjectPropertyAxiom)
	 */
	public void visit(FunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.InverseObjectPropertyAxiom)
	 */
	public void visit(InverseObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.SymmetricObjectPropertyAxiom)
	 */
	public void visit(SymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.TransitiveObjectPropertyAxiom)
	 */
	public void visit(TransitiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.EquivalentClassesAxiom)
	 */
	public void visit(EquivalentClassesAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.SubClassAxiom)
	 */
	public void visit(SubClassAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Negation)
	 */
	public void visit(Negation description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		// TODO Auto-generated method stub
		
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
	 * @see org.dllearner.core.owl.PropertyExpressionVisitor#visit(org.dllearner.core.owl.ObjectProperty)
	 */
	public void visit(ObjectProperty property) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyExpressionVisitor#visit(org.dllearner.core.owl.ObjectPropertyInverse)
	 */
	public void visit(ObjectPropertyInverse property) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyExpressionVisitor#visit(org.dllearner.core.owl.DatatypeProperty)
	 */
	public void visit(DatatypeProperty property) {
		// TODO Auto-generated method stub
		
	}

}
