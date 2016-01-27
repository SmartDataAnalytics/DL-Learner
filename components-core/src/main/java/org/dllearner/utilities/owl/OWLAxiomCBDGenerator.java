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
package org.dllearner.utilities.owl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import javax.annotation.Nonnull;

/**
 * Generates Concise Bounded Descriptions on the OWL axiom level.
 * @author Lorenz Buehmann
 *
 */
public class OWLAxiomCBDGenerator implements OWLAxiomVisitor, OWLClassExpressionVisitor, OWLIndividualVisitor, OWLPropertyExpressionVisitor{
	
	private OWLOntology ontology;
	
	private int maxDepth;
	private int currentDepth;
	
	private Set<OWLAxiom> cbdAxioms;
	
	private Set<OWLClass> visitedClasses;
	private Set<OWLProperty> visitedProperties;
	private Set<OWLIndividual> visitedIndividuals;
	
	private boolean fetchCompleteRelatedTBox = false;
	private boolean inTBox = false;
	private boolean allowPunning = true;
	
	private OWLClass currentClass;
	private OWLObjectProperty currentObjectProperty;
	private OWLDataProperty currentDataProperty;
	
	private Set<IRI> punningClasses;
	
	private boolean subsumptionDown = false;
	
	public OWLAxiomCBDGenerator(OWLOntology ontology) {
		this.ontology = ontology;
		
		punningClasses = OWLPunningDetector.getPunningIRIs(ontology);
	}
	
	public Set<OWLAxiom> getCBD(OWLIndividual ind, int maxDepth){
		this.maxDepth = maxDepth;
		
		cbdAxioms = new HashSet<>();
		visitedClasses = new HashSet<>();
		visitedProperties = new HashSet<>();
		visitedIndividuals = new HashSet<>();
		
		// we start with the directly related axioms
		currentDepth = 0;
		ind.accept(this);
		
		return cbdAxioms;
	}
	
	/**
	 * If enabled, all TBox axioms that are related to classes and properties
	 * and useful for reasoning are retrieved independently from the CBD maximum depth.
	 * @param fetchCompleteRelatedTBox the fetchCompleteRelatedTBox to set
	 */
	public void setFetchCompleteRelatedTBox(boolean fetchCompleteRelatedTBox) {
		this.fetchCompleteRelatedTBox = fetchCompleteRelatedTBox;
	}
	
	/**
	 * If enabled, ABox axioms for classes are also retrieved.
	 * @param allowPunning the allowPunning to set
	 */
	public void setAllowPunning(boolean allowPunning) {
		this.allowPunning = allowPunning;
	}
	
	private String indent(){
		String s = "";
		for(int i = 1; i < currentDepth; i++){
			s+= "   ";
		}
		return s;
	}
	
	public void add(OWLAxiom axiom){
		cbdAxioms.add(axiom);
//		System.out.println(indent() + axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void visit(OWLClass cls) {
		if(!visitedClasses.contains(cls)){
			visitedClasses.add(cls);
			
			currentDepth++;
			Set<OWLClassAxiom> axioms = new HashSet<>();//.getAxioms(cls);
			if(subsumptionDown){
				axioms.addAll(ontology.getSubClassAxiomsForSuperClass(cls));
			} else {
				axioms.addAll(ontology.getSubClassAxiomsForSubClass(cls));
			}
			axioms.addAll(ontology.getEquivalentClassesAxioms(cls));
			axioms.addAll(ontology.getDisjointClassesAxioms(cls));
			axioms.addAll(ontology.getDisjointUnionAxioms(cls));
			
//			axioms = ontology.getAxioms(cls);
//			System.out.println(axioms);
			for (OWLClassAxiom ax : axioms) {
				ax.accept(this);
			}
			currentDepth--;
			
			// handle punning if enabled
			if(allowPunning && punningClasses.contains(cls.getIRI())){
				boolean inTBoxBefore = inTBox;
				inTBox = true;
				new OWLNamedIndividualImpl(cls.getIRI()).accept(this);
				inTBox = inTBoxBefore;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLIndividualVisitor#visit(org.semanticweb.owlapi.model.OWLNamedIndividual)
	 */
	@Override
	public void visit(OWLNamedIndividual individual) {
		if(!visitedIndividuals.contains(individual)){
			visitedIndividuals.add(individual);
			
			currentDepth++;
			Set<OWLIndividualAxiom> axioms = ontology.getAxioms(individual, Imports.INCLUDED);
			for (OWLIndividualAxiom ax : axioms) {
				ax.accept(this);
			}
			currentDepth--;
			
			// handle punning if enabled
			if(allowPunning && punningClasses.contains(individual.getIRI())){
				boolean inTBoxBefore = inTBox;
				inTBox = true;
				new OWLClassImpl(individual.getIRI()).accept(this);
				inTBox = inTBoxBefore;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public void visit(OWLObjectProperty property) {
		if(!visitedProperties.contains(property)){
			visitedProperties.add(property);
			
			Set<OWLObjectPropertyAxiom> axioms = ontology.getAxioms(property, Imports.INCLUDED);
			for (OWLObjectPropertyAxiom ax : axioms) {
				ax.accept(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectInverseOf)
	 */
	@Override
	public void visit(OWLObjectInverseOf property) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public void visit(OWLDataProperty property) {
		if(!visitedProperties.contains(property)){
			visitedProperties.add(property);
			
			Set<OWLDataPropertyAxiom> axioms = ontology.getAxioms(property, Imports.INCLUDED);
			for (OWLDataPropertyAxiom ax : axioms) {
				ax.accept(this);
			}
		}
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty property) {

	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLClassAssertionAxiom)
	 */
	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			boolean wasInTBox = inTBox;
			inTBox = true;
			OWLClassExpression ce = axiom.getClassExpression();
			ce.accept(this);
//			currentDepth--;
			inTBox = wasInTBox;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom)
	 */
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			//get schema axioms for the property
			OWLObjectPropertyExpression property = axiom.getProperty();
			property.accept(this);
//			currentDepth--;
		}
		if((inTBox && fetchCompleteRelatedTBox) || currentDepth < maxDepth){
//			currentDepth++;
			//get the next hop based on the object
			OWLIndividual object = axiom.getObject();
			object.accept(this);
//			currentDepth--;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom)
	 */
	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		add(axiom);
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom)
	 */
	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom)
	 */
	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubClassOfAxiom)
	 */
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
			OWLClassExpression cls;
			if(subsumptionDown){
				cls = axiom.getSubClass();
			} else {
				cls = axiom.getSuperClass();
			}
			cls.accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointClassesAxiom)
	 */
	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			subsumptionDown = true;
			Set<OWLClassExpression> disjointClasses = axiom.getClassExpressions();
			for (OWLClassExpression dis : disjointClasses) {
				dis.accept(this);
			}
			subsumptionDown = false;
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom)
	 */
	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			OWLClassExpression domain = axiom.getDomain();
			domain.accept(this);
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom)
	 */
	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			OWLClassExpression domain = axiom.getDomain();
			domain.accept(this);
//			currentDepth--;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom)
	 */
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
			superProperty.accept(this);
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom)
	 */
	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
			for (OWLObjectPropertyExpression prop : properties) {
				prop.accept(this);
			}
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom)
	 */
	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			Set<OWLDataPropertyExpression> properties = axiom.getProperties();
			for (OWLDataPropertyExpression prop : properties) {
				prop.accept(this);
			}
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom)
	 */
	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
			for (OWLObjectPropertyExpression prop : properties) {
				prop.accept(this);
			}
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom)
	 */
	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		add(axiom);
		if(fetchCompleteRelatedTBox || currentDepth < maxDepth){
//			currentDepth++;
			OWLClassExpression range = axiom.getRange();
			range.accept(this);
//			currentDepth--;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDisjointUnionAxiom)
	 */
	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom)
	 */
	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom)
	 */
	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom)
	 */
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom)
	 */
	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom)
	 */
	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom)
	 */
	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom)
	 */
	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSameIndividualAxiom)
	 */
	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom)
	 */
	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom)
	 */
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLHasKeyAxiom)
	 */
	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom)
	 */
	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		add(axiom);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.SWRLRule)
	 */
	@Override
	public void visit(SWRLRule rule) {
		add(rule);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		for (OWLClassExpression operand : ce.getOperands()) {
			operand.accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public void visit(OWLObjectUnionOf ce) {
		for (OWLClassExpression operand : ce.getOperands()) {
			operand.accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public void visit(OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		System.out.println(ce);
		ce.getFiller().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		ce.getFiller().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public void visit(OWLObjectHasValue ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public void visit(OWLObjectMinCardinality ce) {
		ce.getFiller().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public void visit(OWLObjectExactCardinality ce) {
		ce.getFiller().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		ce.getFiller().accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public void visit(OWLObjectHasSelf ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public void visit(OWLObjectOneOf ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public void visit(OWLDataAllValuesFrom ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Override
	public void visit(OWLDataHasValue ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public void visit(OWLDataMinCardinality ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public void visit(OWLDataExactCardinality ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public void visit(OWLDataMaxCardinality ce) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLIndividualVisitor#visit(org.semanticweb.owlapi.model.OWLAnonymousIndividual)
	 */
	@Override
	public void visit(OWLAnonymousIndividual individual) {
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom)
	 */
	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom)
	 */
	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom)
	 */
	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAnnotationAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom)
	 */
	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLAxiomVisitor#visit(org.semanticweb.owlapi.model.OWLDeclarationAxiom)
	 */
	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		add(axiom);
	}
}
