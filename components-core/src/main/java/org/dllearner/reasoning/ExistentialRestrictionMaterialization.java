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
package org.dllearner.reasoning;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Materialize the existential restrictions, i.e. for all instances x_i that belong to a concept \exists.r.C,
 * we add facts r(x_i, _:newID), C(_:newID), and this recursively.
 *
 * @author Lorenz Buehmann
 *
 */
public class ExistentialRestrictionMaterialization {
	
	
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLDataFactory df;

	public ExistentialRestrictionMaterialization(OWLOntology ontology) {
		this.ontology = ontology;
		
		OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
		reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		
		df = ontology.getOWLOntologyManager().getOWLDataFactory();
	}
	
	private Set<OWLClassExpression> getSuperClasses(OWLClass cls){
		return new SuperClassFinder().getSuperClasses(cls);
	}
	
	public Set<OWLClassExpression> materialize(String classIRI){
		return materialize(df.getOWLClass(IRI.create(classIRI)));
	}
	
	public Set<OWLClassExpression> materialize(OWLClass cls){
		return getSuperClasses(cls);
	}
	
	class SuperClassFinder extends OWLClassExpressionVisitorAdapter{
		
		private Map<OWLClass, Set<OWLClassExpression>> map = new HashMap<>();
		Stack<Set<OWLClassExpression>> stack = new Stack<>();
		OWLDataFactory df;
		boolean onlyIfExistentialOnPath = true;
		
		int indent = 0;

		public SuperClassFinder() {
			df = ontology.getOWLOntologyManager().getOWLDataFactory();
		}
		
		public Set<OWLClassExpression> getSuperClasses(OWLClass cls){
//			System.out.println("#################");
			map.clear();
			computeSuperClasses(cls);
			Set<OWLClassExpression> superClasses = map.get(cls);
			superClasses.remove(cls);
			
			// filter out non existential superclasses
			if(onlyIfExistentialOnPath){
				superClasses.removeIf(sup -> !(sup instanceof OWLObjectSomeValuesFrom || sup instanceof OWLDataAllValuesFrom));
			}
			return superClasses;
		}
		
		private void computeSuperClasses(OWLClass cls){
//			StringBuilder s = new StringBuilder();
//			for(int i = 0; i < indent; i++){
//				s.append("   ");
//			}
//			System.out.println(s + cls);
			indent++;
			Set<OWLClassExpression> superClasses = new HashSet<>();
			superClasses.add(cls);
			
			//get the directly asserted super classes
			Collection<OWLClassExpression> superClassExpressions = EntitySearcher.getSuperClasses(cls, ontology);
			
			//omit trivial super class
			superClassExpressions.remove(cls);
			
			//go subsumption hierarchy up for each directly asserted super class
			for (OWLClassExpression sup : superClassExpressions) {
				sup.accept(this);
				superClasses.addAll(stack.pop());
			}
			
			stack.push(superClasses);
			map.put(cls, superClasses);
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
		 */
		@Override
		public void visit(OWLClass ce) {
			computeSuperClasses(ce);
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
		 */
		@Override
		public void visit(OWLObjectIntersectionOf ce) {
			Set<OWLClassExpression> newIntersections = new HashSet<>();
			Set<OWLClassExpression> operands = ce.getOperands();
			for (OWLClassExpression op : operands) {
				op.accept(this);
				Set<OWLClassExpression> operandSuperClassExpressions = stack.pop();
				Set<OWLClassExpression> newOperands = new HashSet<>(operands);
				newOperands.remove(op);
				for (OWLClassExpression opSup : operandSuperClassExpressions) {
					newOperands.add(opSup);
					newIntersections.add(df.getOWLObjectIntersectionOf(newOperands));
					newOperands.remove(opSup);
				}
			}
			stack.push(newIntersections);
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
		 */
		@Override
		public void visit(OWLObjectUnionOf ce) {
			Set<OWLClassExpression> operands = ce.getOperands();
			for (OWLClassExpression op : operands) {
				op.accept(this);
			}
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
		 */
		@Override
		public void visit(OWLObjectComplementOf ce) {
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
		 */
		@Override
		public void visit(OWLObjectSomeValuesFrom ce) {
			Set<OWLClassExpression> newRestrictions = new HashSet<>();
			newRestrictions.add(ce);
			OWLClassExpression filler = ce.getFiller();
			filler.accept(this);
			Set<OWLClassExpression> fillerSuperClassExpressions = stack.pop();
			for (OWLClassExpression fillerSup : fillerSuperClassExpressions) {
				newRestrictions.add(df.getOWLObjectSomeValuesFrom(ce.getProperty(), fillerSup));
			}
			stack.push(newRestrictions);
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
		 */
		@Override
		public void visit(OWLObjectAllValuesFrom ce) {
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
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
		 */
		@Override
		public void visit(OWLObjectExactCardinality ce) {
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
		 */
		@Override
		public void visit(OWLObjectMaxCardinality ce) {
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
	}

	
	public static void main(String[] args) throws Exception{
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		String s = "@prefix : <http://example.org/> ."
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
				+ "@prefix owl: <http://www.w3.org/2002/07/owl#> ."
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":A a owl:Class . "
				+ ":B a owl:Class . "
				+ ":C a owl:Class . "
				+ ":D a owl:Class . "
				+ ":r a owl:ObjectProperty . "
				+ ":A rdfs:subClassOf [ a owl:Restriction; owl:onProperty :r; owl:someValuesFrom :B]."
				+ ":B rdfs:subClassOf :C."
				+ ":C rdfs:subClassOf [ a owl:Restriction; owl:onProperty :r; owl:someValuesFrom :D]."
				+ ":a a :A.";
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(s.getBytes()));
		ExistentialRestrictionMaterialization mat = new ExistentialRestrictionMaterialization(ontology);
		Set<OWLClassExpression> superClassExpressions = mat.materialize("http://example.org/A");
		for (OWLClassExpression sup : superClassExpressions) {
			System.out.println(sup);
		}
	}

}