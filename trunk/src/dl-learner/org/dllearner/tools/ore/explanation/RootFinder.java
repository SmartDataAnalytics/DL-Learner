package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.AxiomType;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLQuantifiedRestriction;
import org.semanticweb.owl.model.RemoveAxiom;

public class RootFinder implements OWLDescriptionVisitor{

	private OWLOntologyManager manager;
	private OWLReasoner reasoner;
	private OWLReasonerFactory reasonerFactory;
	private Set<OWLClass> rootClasses;
	private Set<OWLClass> depend2Classes;
	private OWLOntology ontology;
	
	int depth;
	private Map<Integer, Set<OWLObjectAllRestriction>> depth2UniversalRestrictionPropertyMap;
    private Map<Integer, Set<OWLObjectPropertyExpression>> depth2ExistsRestrictionPropertyMap;
    
    private Map<OWLClass, Set<OWLClass>> child2Parents;
    private Map<OWLClass, Set<OWLClass>> parent2Children;
	
	public RootFinder(OWLOntologyManager manager, OWLReasoner reasoner, OWLReasonerFactory reasonerFactory){
		this.manager = manager;
		this.reasoner = reasoner;
		this.reasonerFactory = reasonerFactory;
		try {
			this.ontology = manager.createOntology(URI.create("all"), reasoner.getLoadedOntologies());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		rootClasses = new HashSet<OWLClass>();
		depend2Classes = new HashSet<OWLClass>();
		depth2UniversalRestrictionPropertyMap = new HashMap<Integer, Set<OWLObjectAllRestriction>>();
		depth2ExistsRestrictionPropertyMap = new HashMap<Integer, Set<OWLObjectPropertyExpression>>();
		
		child2Parents = new HashMap<OWLClass, Set<OWLClass>>();
		parent2Children = new HashMap<OWLClass, Set<OWLClass>>();
	}
	
	public Set<OWLClass> getRootClasses(){
		computePossibleRoots();
		pruneRoots();
		rootClasses.remove(manager.getOWLDataFactory().getOWLNothing());
//        for(OWLClass child : child2Parents.keySet()){
//        	for(OWLClass par : get(child, child2Parents)){
//        		get(par, parent2Childs).add(child);
//        	}
//        }
		return rootClasses;
	}
	
	private void computePossibleRoots(){
		try {
			for(OWLClass cls : reasoner.getInconsistentClasses()){
				reset();
				for(OWLDescription equi : cls.getEquivalentClasses(ontology)){
					equi.accept(this);
				}
				for(OWLDescription sup : cls.getSuperClasses(ontology)){
					sup.accept(this);
				}
				for(Integer depth : depth2UniversalRestrictionPropertyMap.keySet()){
					Set<OWLObjectPropertyExpression> successors = depth2ExistsRestrictionPropertyMap.get(depth);
					if(successors != null){
						for(OWLObjectAllRestriction all : depth2UniversalRestrictionPropertyMap.get(depth)){
							if(successors.contains(all.getProperty())){
								depend2Classes.add(all.getFiller().asOWLClass());
							}
						}
					}
				}
				child2Parents.put(cls, depend2Classes);
				if(depend2Classes.isEmpty()){
					rootClasses.add(cls);	
				}
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}
	
	private void pruneRoots() {

		try {
			Set<OWLClass> roots = new HashSet<OWLClass>(rootClasses);
			List<OWLOntologyChange> appliedChanges = new ArrayList<OWLOntologyChange>();
			Set<OWLClass> potentialRoots = new HashSet<OWLClass>();
			for (OWLDisjointClassesAxiom dis : new ArrayList<OWLDisjointClassesAxiom>(
					ontology.getAxioms(AxiomType.DISJOINT_CLASSES))) {
				for (OWLClass cls : roots) {
					if (dis.getSignature().contains(cls)) {
						OWLOntologyChange rem = new RemoveAxiom(ontology, dis);
						manager.applyChange(rem);
						appliedChanges.add(rem);
						for (OWLEntity ent : dis.getSignature()) {
							if (ent.isOWLClass()) {
								potentialRoots.add(ent.asOWLClass());
							}
						}
					}
				}
			}
			for (OWLClass cls : roots) {
				manager.addAxiom(ontology, manager.getOWLDataFactory()
						.getOWLDeclarationAxiom(cls));
			}
			OWLReasoner checker = reasonerFactory.createReasoner(manager);
			checker.loadOntologies(Collections.singleton(ontology));
			for (OWLClass root : new ArrayList<OWLClass>(roots)) {
				if (!potentialRoots.contains(root) && checker.isSatisfiable(root)) {
					rootClasses.remove(root);
				}
			}
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}
	
	private void reset(){
		depend2Classes.clear();
		depth2ExistsRestrictionPropertyMap.clear();
		depth2UniversalRestrictionPropertyMap.clear();
	}
	
	private void checkObjectRestriction(OWLQuantifiedRestriction<OWLObjectPropertyExpression,OWLDescription> restr){
		OWLDescription filler = restr.getFiller();
		try {
			if(filler.isAnonymous()){
				depth++;
				filler.accept(this);
				depth--;
			} else {
				if(!reasoner.isSatisfiable(filler)){
					depend2Classes.add(filler.asOWLClass());
					if(restr instanceof OWLObjectAllRestriction){
						addAllRestrictionProperty((OWLObjectAllRestriction) restr);
						return;
					}
				}
				addExistsRestrictionProperty(restr.getProperty());
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
		
	}
	
	private void addExistsRestrictionProperty(OWLObjectPropertyExpression expr){
		Set<OWLObjectPropertyExpression> properties = depth2ExistsRestrictionPropertyMap.get(Integer.valueOf(depth));
		if(properties == null){
			properties = new HashSet<OWLObjectPropertyExpression>();
			depth2ExistsRestrictionPropertyMap.put(Integer.valueOf(depth), properties);
		}
		properties.add(expr);
	}
	
	private void addAllRestrictionProperty(OWLObjectAllRestriction all){
		Set<OWLObjectAllRestriction> properties = depth2UniversalRestrictionPropertyMap.get(Integer.valueOf(depth));
		if(properties == null){
			properties = new HashSet<OWLObjectAllRestriction>();
			depth2UniversalRestrictionPropertyMap.put(Integer.valueOf(depth), properties);
		}
		properties.add(all);
	}
	
	
	@Override
	public void visit(OWLClass cls) {
		
		try {
			if(!reasoner.isSatisfiable(cls)) {
				depend2Classes.add(cls);
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void visit(OWLObjectIntersectionOf and) {
		
		try {
			for(OWLDescription op : and.getOperands()) {
				if(op.isAnonymous()){
					op.accept(this);
				} else if(!reasoner.isSatisfiable(op)) {
					depend2Classes.add(op.asOWLClass());				
				}
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}				
	}

	@Override
	public void visit(OWLObjectUnionOf or) {
		
		try {
			for(OWLDescription op : or.getOperands()){
				if(reasoner.isSatisfiable(op)){
					return;
				}
			}
			for(OWLDescription op : or.getOperands()){
				if(op.isAnonymous()){
					op.accept(this);
				} else {
					depend2Classes.add(op.asOWLClass());
				}
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void visit(OWLObjectAllRestriction all) {
		checkObjectRestriction(all);		
	}
	
	@Override
	public void visit(OWLObjectMinCardinalityRestriction min) {
		checkObjectRestriction(min);	
	}
	
	@Override
	public void visit(OWLObjectExactCardinalityRestriction exact) {
		checkObjectRestriction(exact);		
	}
	
	@Override
	public void visit(OWLObjectSelfRestriction self) {
		addExistsRestrictionProperty(self.getProperty());	
	}
	
	@Override
	public void visit(OWLObjectSomeRestriction some) {
		checkObjectRestriction(some);		
	}

	@Override
	public void visit(OWLObjectComplementOf arg0) {	
	}

	@Override
	public void visit(OWLObjectValueRestriction arg0) {
	}

	@Override
	public void visit(OWLObjectMaxCardinalityRestriction arg0) {		
	}

	@Override
	public void visit(OWLObjectOneOf arg0) {
	}

	@Override
	public void visit(OWLDataSomeRestriction arg0) {
	}

	@Override
	public void visit(OWLDataAllRestriction arg0) {
	}

	@Override
	public void visit(OWLDataValueRestriction arg0) {
	}

	@Override
	public void visit(OWLDataMinCardinalityRestriction arg0) {
	}

	@Override
	public void visit(OWLDataExactCardinalityRestriction arg0) {
	}

	@Override
	public void visit(OWLDataMaxCardinalityRestriction arg0) {	
	}

	public static void main(String[] args){
    	try {
			String	file	= "file:examples/ore/tambis.owl";
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI.create(file));
			OWLReasonerFactory factory = new PelletReasonerFactory();
			OWLReasoner reasoner = factory.createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(ontology));
			UnsatRootClassesFinder strReasoner = new UnsatRootClassesFinder(manager, reasoner, factory);
			System.out.println(strReasoner.getRootUnsatisfiableClasses());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (ExplanationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
    }
}
