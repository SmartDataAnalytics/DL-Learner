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
import java.util.Iterator;
import java.util.Set;

import org.dllearner.core.owl.AsymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.DisjointDatatypePropertyAxiom;
import org.dllearner.core.owl.DisjointObjectPropertyAxiom;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.EquivalentDatatypePropertiesAxiom;
import org.dllearner.core.owl.EquivalentObjectPropertiesAxiom;
import org.dllearner.core.owl.FunctionalDatatypePropertyAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.InverseFunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.ReflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubDatatypePropertyAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class DLLearnerAxiomConvertVisitor implements OWLAxiomVisitor{
	
	private Axiom axiom;
	
	public Axiom getDLLearnerAxiom() {
		return axiom;
	}

	public static Axiom getDLLearnerAxiom(OWLAxiom axiom) {
		DLLearnerAxiomConvertVisitor converter = new DLLearnerAxiomConvertVisitor();
		axiom.accept(converter);
		return converter.getDLLearnerAxiom();
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDeclarationAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLSubClassOfAxiom ax) {
		axiom = new SubClassAxiom(DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(ax.getSubClass()),
				DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(ax.getSuperClass()));
		
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom ax) {
		axiom = new AsymmetricObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom ax) {
		axiom = new ReflexiveObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLDisjointClassesAxiom ax) {
		Set<Description> descriptions = new HashSet<Description>();
		for(OWLClassExpression expr : ax.getClassExpressions()){
			descriptions.add(DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(expr));
		}
		axiom = new DisjointClassesAxiom(descriptions);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom ax) {
		axiom = new DatatypePropertyDomainAxiom(new DatatypeProperty(ax.getProperty().asOWLDataProperty().toStringID()),
				DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(ax.getDomain()));
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom ax) {
		axiom = new ObjectPropertyDomainAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()),
				DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(ax.getDomain()));
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom ax) {
		Set<ObjectProperty> properties = new HashSet<ObjectProperty>();
		for(OWLObjectPropertyExpression expr : ax.getProperties()){
			properties.add(new ObjectProperty(expr.asOWLObjectProperty().toStringID()));
		}
		axiom = new EquivalentObjectPropertiesAxiom(properties);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom ax) {
		Iterator<OWLDataPropertyExpression> iter = ax.getProperties().iterator();
		DatatypeProperty p1 = new DatatypeProperty(iter.next().asOWLDataProperty().toStringID());
		DatatypeProperty p2 = new DatatypeProperty(iter.next().asOWLDataProperty().toStringID());
		axiom = new DisjointDatatypePropertyAxiom(p1, p2);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom ax) {
		Iterator<OWLObjectPropertyExpression> iter = ax.getProperties().iterator();
		ObjectProperty p1 = new ObjectProperty(iter.next().asOWLObjectProperty().toStringID());
		ObjectProperty p2 = new ObjectProperty(iter.next().asOWLObjectProperty().toStringID());
		axiom = new DisjointObjectPropertyAxiom(p1, p2);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom ax) {
		axiom = new ObjectPropertyRangeAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()),
				DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(ax.getRange()));
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom ax) {
		axiom = new FunctionalObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom ax) {
		axiom = new SubObjectPropertyAxiom(new ObjectProperty(ax.getSubProperty().asOWLObjectProperty().toStringID()),
				new ObjectProperty(ax.getSuperProperty().asOWLObjectProperty().toStringID()));
		
	}

	@Override
	public void visit(OWLDisjointUnionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom ax) {
		axiom = new SymmetricObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom ax) {
		axiom = new DatatypePropertyRangeAxiom(new DatatypeProperty(ax.getProperty().asOWLDataProperty().toStringID()),
				new Datatype(ax.getRange().asOWLDatatype().toStringID()));
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom ax) {
		axiom = new FunctionalDatatypePropertyAxiom(new DatatypeProperty(ax.getProperty().asOWLDataProperty().toStringID()));
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom ax) {
		Iterator<OWLDataPropertyExpression> iter = ax.getProperties().iterator();
		DatatypeProperty p1 = new DatatypeProperty(iter.next().asOWLDataProperty().toStringID());
		DatatypeProperty p2 = new DatatypeProperty(iter.next().asOWLDataProperty().toStringID());
		axiom = new EquivalentDatatypePropertiesAxiom(p1, p2);
	}

	@Override
	public void visit(OWLClassAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom ax) {
		Iterator<OWLClassExpression> iter = ax.getClassExpressions().iterator();
		Description d1 = DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(iter.next());
		Description d2 = DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(iter.next());
		axiom = new EquivalentClassesAxiom(d1, d2);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom ax) {
		axiom = new TransitiveObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom ax) {
		axiom = new IrreflexiveObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom ax) {
		axiom = new SubDatatypePropertyAxiom(new DatatypeProperty(ax.getSubProperty().asOWLDataProperty().toStringID()),
				new DatatypeProperty(ax.getSuperProperty().asOWLDataProperty().toStringID()));
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom ax) {
		axiom = new InverseFunctionalObjectPropertyAxiom(new ObjectProperty(ax.getProperty().asOWLObjectProperty().toStringID()));
	}

	@Override
	public void visit(OWLSameIndividualAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLHasKeyAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLRule arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
