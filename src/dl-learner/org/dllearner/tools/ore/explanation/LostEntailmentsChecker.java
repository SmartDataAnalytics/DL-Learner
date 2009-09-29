package org.dllearner.tools.ore.explanation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.RemoveAxiom;

import uk.ac.manchester.cs.owl.modularity.ModuleType;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;

public class LostEntailmentsChecker {
	
	
	private OWLOntology ontology;
	private Reasoner reasoner;
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
			Set<OWLClass> inc = classifier.getInconsistentClasses();
			for(OWLDescription cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl) && !cl.isOWLThing()){
					for(OWLClass sub : SetUtils.union(classifier.getDescendantClasses(cl))){
						if(!sub.isOWLNothing() && !inc.contains(sub)){
							entailmentsBefore.add(factory.getOWLSubClassAxiom(sub, cl));
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
			inc = classifier.getInconsistentClasses();
			
			for(OWLDescription cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl) && !cl.isOWLThing()){
					for(OWLClass sub : SetUtils.union(classifier.getDescendantClasses(cl))){
						if(!sub.isOWLNothing() && !inc.contains(sub)){
							entailmentsAfter.add(factory.getOWLSubClassAxiom(sub, cl));
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
			
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return impact;
	}

	public Set<OWLAxiom> computeStructuralImpact(List<OWLOntologyChange> changes) {
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
				if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAx = (OWLSubClassAxiom) axiom;
					if (subAx.getSubClass() instanceof OWLClass && subAx.getSuperClass() instanceof OWLClass) {
						OWLClass sub = (OWLClass) subAx.getSubClass();
						OWLClass sup = (OWLClass) subAx.getSuperClass();
						
						for (OWLClass desc : SetUtils.union(reasoner.getDescendantClasses(sub))) {
							
							for (OWLClass anc : SetUtils.union(reasoner.getAncestorClasses(sup))) {	
								
								if (!anc.equals(factory.getOWLThing()) && !desc.equals(factory.getOWLNothing())) {
									OWLSubClassAxiom ax = factory.getOWLSubClassAxiom(desc, anc);
									possibleLosts.add(ax);
								}
							}
						}
					}						
				}else if (axiom instanceof OWLDisjointClassesAxiom) {
					
					Set<OWLDescription> disjointClasses = ((OWLDisjointClassesAxiom) axiom).getDescriptions();
					boolean complex = false;
					for (OWLDescription dis : disjointClasses) {
						if (dis.isAnonymous()) {
							complex = true;
							break;
						}
					}
					if (!complex) {
						
						List<OWLDescription> disjoints = new ArrayList<OWLDescription>(disjointClasses);
						
							
							
						for (OWLDescription dis : new ArrayList<OWLDescription>(disjoints)) {
							if (!dis.equals(factory.getOWLNothing())) {
								disjoints.remove(dis);
								
								Set<? extends OWLDescription> descendants = SetUtils.union(reasoner.getDescendantClasses(dis.asOWLClass()));
						
								descendants.removeAll(reasoner.getEquivalentClasses(factory.getOWLNothing()));
//								if (enableImpactUnsat) {
//									descendants.addAll(((OWLClass) dis).getSubClasses(ontology));
//								}
								for (OWLDescription desc1 : descendants) {
									
									if (!desc1.equals(factory.getOWLNothing())) {
										if (enableImpactUnsat || !reasoner.getEquivalentClasses((desc1)).contains(factory.getOWLNothing())) {
											for (OWLDescription desc2 : disjoints) {								
												
												if (!desc2.equals(desc1)) {
													Set<OWLDescription> newDis = new HashSet<OWLDescription>();
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
						Set<OWLClass> superClasses = SetUtils.union(reasoner.getSuperClasses(dom));
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
						Set<OWLClass> superClasses = SetUtils.union(reasoner.getSuperClasses(dom));
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
						Set<OWLClass> superClasses = SetUtils.union(reasoner.getSuperClasses(ran));
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
				if(!reasoner.isEntailed(ax)){
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
