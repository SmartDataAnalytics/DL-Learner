/**
 * 
 */
package org.dllearner.reasoning;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class ExistentialRestrictionMaterialization {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExistentialRestrictionMaterialization.SuperClassFinder.class);
	
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLDataFactory df;
	
	Set<OWLClassExpression> visited = new HashSet<>();

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
		Set<OWLClassExpression> superClasses = getSuperClasses(cls);
		return superClasses;
	}
	
	class SuperClassFinder extends OWLClassExpressionVisitorAdapter{
		
		private Map<OWLClass, Set<OWLClassExpression>> map = new HashMap<>();
		Stack<Set<OWLClassExpression>> stack = new Stack<Set<OWLClassExpression>>();
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
			
			//filter out non existential superclasses
			if(onlyIfExistentialOnPath){
				for (Iterator<OWLClassExpression> iterator = superClasses.iterator(); iterator.hasNext();) {
					OWLClassExpression sup = iterator.next();
					if (!(sup instanceof OWLObjectSomeValuesFrom || sup instanceof OWLDataAllValuesFrom)) {
						iterator.remove();
					}
				}
			}
			return superClasses;
		}
		
		private void computeSuperClasses(OWLClass cls){
			visited.add(cls);
			
			String s = "";
			for(int i = 0; i < indent; i++){
				s += "   ";
			}
//			System.out.println(s + cls);
			indent++;
			Set<OWLClassExpression> superClasses = new HashSet<OWLClassExpression>();
			superClasses.add(cls);
			
			//get the directly asserted super classes
			Set<OWLClassExpression> superClassExpressions = cls.getSuperClasses(ontology);
			
			//omit trivial super class
			superClassExpressions.remove(cls);
			
			//go subsumption hierarchy up for each directly asserted super class
			for (OWLClassExpression sup : superClassExpressions) {
				if(!visited.contains(sup)){
					sup.accept(this);
					superClasses.addAll(stack.pop());
				} else {
					LOGGER.warn("Cycle detected:" + sup + " in " + visited);
				}
			}
			
			stack.push(superClasses);
			map.put(cls, superClasses);
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
		 */
		@Override
		public void visit(OWLClass ce) {
			if(!visited.contains(ce)){
				visited.add(ce);
				computeSuperClasses(ce);
			}
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
		 */
		@Override
		public void visit(OWLObjectIntersectionOf ce) {
			Set<OWLClassExpression> newIntersections = new HashSet<OWLClassExpression>();
			Set<OWLClassExpression> operands = ce.getOperands();
			for (OWLClassExpression op : operands) {
				op.accept(this);
				Set<OWLClassExpression> operandSuperClassExpressions = stack.pop();
				Set<OWLClassExpression> newOperands = new HashSet<OWLClassExpression>(operands);
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
			Set<OWLClassExpression> newRestrictions = new HashSet<OWLClassExpression>();
			newRestrictions.add(ce);
			OWLClassExpression filler = ce.getFiller();
			
			if(!visited.contains(filler)){
				filler.accept(this);
				Set<OWLClassExpression> fillerSuperClassExpressions = stack.pop();
				for (OWLClassExpression fillerSup : fillerSuperClassExpressions) {
					newRestrictions.add(df.getOWLObjectSomeValuesFrom(ce.getProperty(), fillerSup));
				}
				stack.push(newRestrictions);
			} else {
				//TODO how to handle cycles?
				LOGGER.warn("Cycle detected:" + filler + " in " + visited);
			}
			
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
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
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
				+ ":D rdfs:subClassOf :A ."
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
