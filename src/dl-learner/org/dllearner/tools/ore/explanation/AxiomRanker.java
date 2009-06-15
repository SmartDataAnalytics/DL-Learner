package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AxiomType;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.RemoveAxiom;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModuleExtractor;
import com.clarkparsia.modularity.ModuleExtractorFactory;

public class AxiomRanker {
	
	private Map axiomSOSMap;
	
	private OWLOntology ontology;
	private Reasoner reasoner;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private IncrementalClassifier classifier;
	
	boolean enableImpactUnsat;
	
	public AxiomRanker(OWLOntology ont, Reasoner reasoner, OWLOntologyManager mng){
		this.ontology = ont;
		this.reasoner = reasoner;
		this.manager = mng;
		this.factory = manager.getOWLDataFactory();
		ModuleExtractor extractor = ModuleExtractorFactory.createModuleExtractor();
		classifier = new IncrementalClassifier(manager, reasoner, extractor);
	}
	
	public Set<OWLAxiom> computeImpactOnRemoval(OWLAxiom ax){
		Set<OWLAxiom> impact = new HashSet<OWLAxiom>();
		
		try {
//			IncrementalClassifier classifier = new IncrementalClassifier(manager);
//			classifier.loadOntology(ontology);
//			classifier.classify();
			Set<OWLClass> inc = classifier.getInconsistentClasses();
			for(OWLDescription cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl)){
					for(OWLClass sup : SetUtils.union(classifier.getAncestorClasses(cl))){
						if(!sup.equals(factory.getOWLThing())){
							for(OWLClass sub : SetUtils.union(classifier.getDescendantClasses(cl))){
								if(!sub.equals(factory.getOWLNothing())){
									impact.add(factory.getOWLSubClassAxiom(cl, sup));
								}
							}
							
						}
					}
					
				}
			}
			
			
			manager.applyChange(new RemoveAxiom(ontology, ax));
			classifier.classify();
			inc = classifier.getInconsistentClasses();
			
			for(OWLDescription cl : ontology.getClassesInSignature()){
				if(!inc.contains(cl)){
					for(OWLClass sup : SetUtils.union(classifier.getAncestorClasses(cl))){
						if(!sup.equals(factory.getOWLThing())){
							for(OWLClass sub : SetUtils.union(classifier.getDescendantClasses(cl))){
								if(!sub.equals(factory.getOWLNothing())){
									impact.remove(factory.getOWLSubClassAxiom(cl, sup));
								}
							}
							
						}
					}
					
				}
			}
			manager.addAxiom(ontology, ax);classifier.classify();
			
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return impact;
	}

	public Set<OWLAxiom> computeImpactSOS(OWLAxiom axiom) {
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		if (axiom instanceof OWLSubClassAxiom) {
			OWLSubClassAxiom subAx = (OWLSubClassAxiom) axiom;
			if (subAx.getSubClass() instanceof OWLClass && subAx.getSuperClass() instanceof OWLClass) {
				OWLClass sub = (OWLClass) subAx.getSubClass();
				OWLClass sup = (OWLClass) subAx.getSuperClass();
				
				for (OWLClass desc : SetUtils.union(reasoner.getDescendantClasses(sub))) {
					
					for (OWLClass anc : SetUtils.union(reasoner.getAncestorClasses(sup))) {	
						
						if (!anc.equals(factory.getOWLThing()) && !desc.equals(factory.getOWLNothing())) {
							OWLSubClassAxiom ax = factory.getOWLSubClassAxiom(desc, anc);
							result.add(ax);
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
//						if (enableImpactUnsat) {
//							descendants.addAll(((OWLClass) dis).getSubClasses(ontology));
//						}
						for (OWLDescription desc1 : descendants) {
							
							if (!desc1.equals(factory.getOWLNothing())) {
								if (enableImpactUnsat || !reasoner.getEquivalentClasses((desc1)).contains(factory.getOWLNothing())) {
									for (OWLDescription desc2 : disjoints) {								
										
										if (!desc2.equals(desc1)) {
											Set<OWLDescription> newDis = new HashSet<OWLDescription>();
											newDis.add(desc1);
											newDis.add(desc2);
											OWLDisjointClassesAxiom ax = factory.getOWLDisjointClassesAxiom(newDis);
											result.add(ax);
										}
									}
								}
							}
						}
						
							disjoints.add(dis);
						
						
					}							
				}
//				return result;
			}
		}
		else if (axiom instanceof OWLObjectPropertyDomainAxiom) {
			OWLObjectPropertyDomainAxiom pd = (OWLObjectPropertyDomainAxiom) axiom;
			
			if (pd.getDomain() instanceof OWLClass) {
				OWLClass dom = (OWLClass) pd.getDomain();
				Set<OWLClass> superClasses = SetUtils.union(reasoner.getSuperClasses(dom));
				for (OWLClass sup : superClasses) {
					
					OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom( pd.getProperty(), sup);
					result.add(ax);
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
					result.add(ax);
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
					result.add(ax);
				}
			}
		}
		for(OWLAxiom ax : result){
			System.out.println(reasoner.isEntailed(axiom));
		}
		return result;
	}
	

	

	
	public static void main(String[] args){
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory dFactory = manager.getOWLDataFactory();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI.create("file:examples/ore/koala.owl"));
			PelletReasonerFactory factory = new PelletReasonerFactory();
			Reasoner reasoner = factory.createReasoner(manager);
			reasoner.loadOntology(ontology);
			reasoner.classify();
			
			
			OWLClass cl1 = dFactory.getOWLClass(URI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#koala"));
			OWLClass cl2 = dFactory.getOWLClass(URI.create("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#person"));
			OWLAxiom ax = dFactory.getOWLSubClassAxiom(cl1, dFactory.getOWLObjectComplementOf(cl2));
			Set<OWLClass> before = null;
			Set<OWLClass> after = null;
			
				before = SetUtils.union(reasoner.getSuperClasses(cl1));
				manager.applyChange(new RemoveAxiom(ontology, ax));
				after = SetUtils.union(reasoner.getSuperClasses(cl1));
				System.out.println(SetUtils.difference(before, after));
			
			System.out.println(cl1.getSuperClasses(ontology));
			System.out.println(after);
			
			
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
