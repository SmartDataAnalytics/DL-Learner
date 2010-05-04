package org.dllearner.tools.ore.explanation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class LostEntailmentsChecker {
	
	
	private OWLOntology ontology;
	private PelletReasoner reasoner;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private IncrementalClassifier classifier;
	
	boolean enableImpactUnsat;
	
	public LostEntailmentsChecker(OWLOntology ont, IncrementalClassifier classifier, OWLOntologyManager mng){
		this.ontology = ont;
		this.classifier = classifier;
		this.manager = mng;
		this.factory = manager.getOWLDataFactory();
		this.reasoner = classifier.getReasoner();
		
	}
	
	public List<Set<OWLAxiom>> computeClassificationImpact(List<OWLOntologyChange> changes){
		List<Set<OWLAxiom>> impact = new ArrayList<Set<OWLAxiom>>(2);
		Set<OWLAxiom> entailmentsBefore = new HashSet<OWLAxiom>();
		Set<OWLAxiom> entailmentsAfter = new HashSet<OWLAxiom>();
		Set<OWLAxiom> lostEntailments = new HashSet<OWLAxiom>();
		Set<OWLAxiom> addedEntailents = new HashSet<OWLAxiom>();
		
		try{
			Set<OWLClass> inc = classifier.getUnsatisfiableClasses().getEntitiesMinusBottom();
			for(OWLClass cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl) && !cl.isOWLThing()){
					for(OWLClass sub : classifier.getSubClasses(cl, false).getFlattened()){
						if(!sub.isOWLNothing() && !inc.contains(sub)){
							entailmentsBefore.add(factory.getOWLSubClassOfAxiom(sub, cl));
						}
					}
					for(OWLClass equ : classifier.getEquivalentClasses(cl)){
						if(!equ.isOWLNothing() && ! inc.contains(equ)){
							entailmentsBefore.add(factory.getOWLEquivalentClassesAxiom(equ, cl));
						}
					}
				}
			}
			
			
			manager.applyChanges(changes);
			
			classifier.classify();
			inc = classifier.getUnsatisfiableClasses().getEntitiesMinusBottom();
			
			for(OWLClass cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl) && !cl.isOWLThing()){
					for(OWLClass sub : classifier.getSubClasses(cl, false).getFlattened()){
						if(!sub.isOWLNothing() && !inc.contains(sub)){
							entailmentsAfter.add(factory.getOWLSubClassOfAxiom(sub, cl));
						}
					}
				}
				for(OWLClass equ : classifier.getEquivalentClasses(cl)){
					if(!equ.isOWLNothing() && ! inc.contains(equ)){
						entailmentsAfter.add(factory.getOWLEquivalentClassesAxiom(equ, cl));
					}
				}
			}
			lostEntailments = SetUtils.difference(entailmentsBefore, entailmentsAfter);
			addedEntailents = SetUtils.difference(entailmentsAfter, entailmentsBefore);
			
			for(OWLOntologyChange change : changes){
				if(change instanceof RemoveAxiom){
					lostEntailments.remove(change.getAxiom());
				}
			}
			impact.add(0, lostEntailments);
			impact.add(1, addedEntailents);
			manager.applyChanges(getInverseChanges(changes));
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return impact;
	}

	public Set<OWLAxiom> computeStructuralImpact(List<OWLOntologyChange> changes) {
		Map<OWLClass, Set<OWLClass>> subsumptionHierarchyUp = new HashMap<OWLClass, Set<OWLClass>>();
		Map<OWLClass, Set<OWLClass>> subsumptionHierarchyDown = new HashMap<OWLClass, Set<OWLClass>>();
		System.out.println("Computing structural impact");
		System.out.println("Refreshing reasoner");
		reasoner.refresh();
		System.out.println("Reasoner refreshed");
		Set<OWLAxiom> possibleLosts = new HashSet<OWLAxiom>();
		Set<OWLAxiom> realLosts = new HashSet<OWLAxiom>();
		OWLAxiom axiom;
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				axiom = change.getAxiom();
				if (axiom instanceof OWLSubClassOfAxiom) {
					OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) axiom;
					if (subAx.getSubClass() instanceof OWLClass && subAx.getSuperClass() instanceof OWLClass) {
						OWLClass sub = (OWLClass) subAx.getSubClass();
						OWLClass sup = (OWLClass) subAx.getSuperClass();
						Set<OWLClass> descendants = subsumptionHierarchyDown.get(sub);
						if(descendants == null){
							descendants = reasoner.getSubClasses(sub, false).getFlattened();
							subsumptionHierarchyDown.put(sub, descendants);
						}
						for (OWLClass desc : descendants) {
							Set<OWLClass> ancestors = subsumptionHierarchyUp.get(sub);
							if(ancestors == null){
								ancestors = reasoner.getSuperClasses(sup, false).getFlattened();
								subsumptionHierarchyUp.put(sup, ancestors);
							}
							for (OWLClass anc : ancestors) {	
								
								if (!anc.equals(factory.getOWLThing()) && !desc.equals(factory.getOWLNothing())) {
									OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(desc, anc);
									possibleLosts.add(ax);
								}
							}
						}
					}						
				}else if (axiom instanceof OWLDisjointClassesAxiom) {
					
					Set<OWLClassExpression> disjointClasses = ((OWLDisjointClassesAxiom) axiom).getClassExpressions();
					boolean complex = false;
					for (OWLClassExpression dis : disjointClasses) {
						if (dis.isAnonymous()) {
							complex = true;
							break;
						}
					}
					if (!complex) {
						
						List<OWLClassExpression> disjoints = new ArrayList<OWLClassExpression>(disjointClasses);
						
							
							
						for (OWLClassExpression dis : new ArrayList<OWLClassExpression>(disjoints)) {
							if (!dis.equals(factory.getOWLNothing())) {
								disjoints.remove(dis);
								
								Set<? extends OWLClassExpression> descendants = reasoner.getSubClasses(dis.asOWLClass(),false).getFlattened();
						
								descendants.removeAll(reasoner.getEquivalentClasses(factory.getOWLNothing()).getEntities());
//								if (enableImpactUnsat) {
//									descendants.addAll(((OWLClass) dis).getSubClasses(ontology));
//								}
								for (OWLClassExpression desc1 : descendants) {
									
									if (!desc1.equals(factory.getOWLNothing())) {
										if (enableImpactUnsat || !reasoner.getEquivalentClasses((desc1)).contains(factory.getOWLNothing())) {
											for (OWLClassExpression desc2 : disjoints) {								
												
												if (!desc2.equals(desc1)) {
													Set<OWLClassExpression> newDis = new HashSet<OWLClassExpression>();
													newDis.add(desc1);
													newDis.add(desc2);
													OWLDisjointClassesAxiom ax = factory.getOWLDisjointClassesAxiom(newDis);
													possibleLosts.add(ax);
												}
											}
										}
									}
								}
								
									disjoints.add(dis);
								
								
							}							
						}
//						return result;
					}
				}
				else if (axiom instanceof OWLObjectPropertyDomainAxiom) {
					OWLObjectPropertyDomainAxiom pd = (OWLObjectPropertyDomainAxiom) axiom;
					
					if (pd.getDomain() instanceof OWLClass) {
						OWLClass dom = (OWLClass) pd.getDomain();
						Set<OWLClass> superClasses = reasoner.getSuperClasses(dom, false).getFlattened();
						for (OWLClass sup : superClasses) {
							
							OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom( pd.getProperty(), sup);
							possibleLosts.add(ax);
						}
					}
				}
				else if (axiom instanceof OWLDataPropertyDomainAxiom) {
					OWLDataPropertyDomainAxiom pd = (OWLDataPropertyDomainAxiom) axiom;
					
					if (pd.getDomain() instanceof OWLClass) {
						OWLClass dom = (OWLClass) pd.getDomain();
						Set<OWLClass> superClasses = reasoner.getSuperClasses(dom, false).getFlattened();
						for (OWLClass sup : superClasses) {
							
							OWLDataPropertyDomainAxiom ax = factory.getOWLDataPropertyDomainAxiom( pd.getProperty(), sup);
							possibleLosts.add(ax);
						}
					}
				}
				else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
					OWLObjectPropertyRangeAxiom pd = (OWLObjectPropertyRangeAxiom) axiom;
					
					if (pd.getRange() instanceof OWLClass) {
						OWLClass ran = (OWLClass) pd.getRange();
						Set<OWLClass> superClasses = reasoner.getSuperClasses(ran, false).getFlattened();
						for (OWLClass sup : superClasses) {
							
							OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(pd.getProperty(), sup);
							possibleLosts.add(ax);
						}
					}
				}
			}
			
		}
		
		for(OWLAxiom ax : possibleLosts){
			try {
				manager.applyChanges(changes);
				if(!classifier.getReasoner().isEntailed(ax)){
					realLosts.add(ax);
				}
				manager.applyChanges(getInverseChanges(changes));
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return realLosts;
	}
	
	private List<OWLOntologyChange> getInverseChanges(List<OWLOntologyChange> changes){
		List<OWLOntologyChange> inverseChanges = new ArrayList<OWLOntologyChange>(changes.size());
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				inverseChanges.add(new AddAxiom(change.getOntology(), change.getAxiom()));
			} else {
				inverseChanges.add(new RemoveAxiom(change.getOntology(), change.getAxiom()));
			}
		}
		return inverseChanges;
	}
	
}
