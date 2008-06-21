package org.dllearner.tools.ore;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.OWLEntityRemover;

public class OntologyModifierOWLAPI {

	OWLOntology ontology;
	OWLAPIReasoner reasoner;
	OWLDataFactory factory;
	OWLOntologyManager manager;
	
	
	public OntologyModifierOWLAPI(OWLAPIReasoner reasoner){
		this.reasoner = reasoner;
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontology = reasoner.getOWLAPIOntologies().get(0);
	}
	
	
	public void addAxiomToOWL(Description newDesc, Description oldDesc){
		OWLDescription newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(newDesc);
		OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(oldDesc);
		
		Set<OWLDescription> ds = new HashSet<OWLDescription>();
		ds.add(newConceptOWLAPI);
		ds.add(oldConceptOWLAPI);
		
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		
		
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void saveOntology(){
		URI physicalURI2 = URI.create("file:/tmp/MyOnt2.owl");
		
		try {
			manager.saveOntology(ontology, new RDFXMLOntologyFormat(), physicalURI2);
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	/**
	 * Deletes the complete individual from the ontology
	 * @param ind
	 */
	public void deleteIndividual(Individual ind){
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		
		individualOWLAPI.accept(remover);
		
		try {
			manager.applyChanges(remover.getChanges());
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		remover.reset();
		
		
	}
	/**
	 * Removes a classAssertion 
	 * @param ind
	 * @param desc
	 */
	public void removeClassAssertion(Individual ind, Description desc){
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, owlDesc);
				
		RemoveAxiom rm = new RemoveAxiom(ontology, owlCl);
		
		try {
			manager.applyChange(rm);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * adds a classAssertion 
	 * @param ind
	 * @param desc
	 */
	public void addClassAssertion(Individual ind, Description desc){
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, owlDesc);
				
		AddAxiom am = new AddAxiom(ontology, owlCl);
		
		try {
			manager.applyChange(am);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * removes classAssertion between individual to a old class, and creates a new classAssertion
	 * @param ind individual which has to be moved
	 * @param oldClass class where individual is asserted before
	 * @param newClass class where individual is moved to
	 */
	public void moveIndividual(Individual ind, Description oldClass, Description newClass){
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		
		//Loeschen
		removeClassAssertion(ind, oldClass);
		
		//Hinzufuegen
		
		OWLDescription newClassOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(newClass);
	
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(individualOWLAPI, newClassOWLAPI);
		
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveOntology();
		
	}
	
	public void deleteObjectProperty(Individual ind, ObjectSomeRestriction objSome){
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(individualOWLAPI);
		Set<OWLInverseObjectPropertiesAxiom> invProperties = ontology.getInverseObjectPropertyAxioms(propertyOWLAPI);
		
		OWLObjectPropertyExpression invProperty = null;
		
		for(OWLInverseObjectPropertiesAxiom inv : invProperties)
			if(propertyOWLAPI.equals(inv.getSecondProperty()))
				invProperty  = inv.getFirstProperty();
			else
				invProperty = inv.getSecondProperty();
		
		
		List<RemoveAxiom> removeList = new LinkedList<RemoveAxiom>();
		
		for(OWLObjectPropertyAssertionAxiom o :properties){
			if( (o.getProperty().equals(propertyOWLAPI)) && (o.getSubject().equals(individualOWLAPI))) 
				removeList.add(new RemoveAxiom(ontology, o));
			if(invProperty != null)
				for(OWLObjectPropertyAssertionAxiom ob :ontology.getObjectPropertyAssertionAxioms(o.getObject()))
					if(ob.getProperty().equals(invProperty) && ob.getObject().equals(individualOWLAPI))
						removeList.add(new RemoveAxiom(ontology, ob));
			
			
		}
		
		try {
			manager.applyChanges(removeList);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void addObjectProperty(Individual subInd, ObjectSomeRestriction objSome, Individual objInd){
		
		OWLIndividual subIndividualOWLAPI = factory.getOWLIndividual( URI.create(subInd.getName()));
		OWLIndividual objIndividualOWLAPI = factory.getOWLIndividual( URI.create(objInd.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		OWLObjectPropertyAssertionAxiom objAssertion = factory.getOWLObjectPropertyAssertionAxiom(subIndividualOWLAPI, propertyOWLAPI, objIndividualOWLAPI);
		
		AddAxiom axiom = new AddAxiom(ontology, objAssertion);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void refreshReasoner(){
		Set<KnowledgeSource> s = new HashSet<KnowledgeSource>();
		s.add(new OWLAPIOntology(ontology));
		this.reasoner = new OWLAPIReasoner(s);
		reasoner.init();
	}
	
	public boolean checkInstanceNewOntology(Description desc, Individual ind){
		Set<KnowledgeSource> s = new HashSet<KnowledgeSource>();
		s.add(new OWLAPIOntology(ontology));
		OWLAPIReasoner r = new OWLAPIReasoner(s);
		r.init();
		boolean check = r.instanceCheck(desc, ind);

		
		
		return check;
	}
	
	public Set<Description> getCriticalDescriptions(Individual ind, Description desc){
		Set<KnowledgeSource> s = new HashSet<KnowledgeSource>();
		s.add(new OWLAPIOntology(ontology));
		OWLAPIReasoner r = new OWLAPIReasoner(s);
		r.init();
		
		Set<Description> criticals = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
		if(r.instanceCheck(desc, ind)){
			System.out.println("wahr");
			if(children.size() >= 2){
				
				if(desc instanceof Intersection){
					for(Description d: children)
						criticals.addAll(getCriticalDescriptions(ind, d));
				
				}
				else if(desc instanceof Union){
					for(Description d: children)
						if(reasoner.instanceCheck(d, ind))
							criticals.addAll(getCriticalDescriptions(ind, d));
				}
			}
			else
				criticals.add(desc);
		}
	
	
	return criticals;
}


	
	
//	public OWLOntology copyOntology(){
//		try{
//			OWLOntology ontologyCopy = manager.createOntology(ontology.getURI());
//		
//		 
//		Set<OWLAxiom> axioms = ontology.getAxioms();
//		List<AddAxiom> changes = new LinkedList<AddAxiom>();
//		for(OWLAxiom a : axioms)
//			changes.add(new AddAxiom(ontologyCopy, a));
//			
//		manager.applyChanges(changes);
//		
//		return ontologyCopy;
//		
//		}catch(OWLException e){
//			e.printStackTrace();
//			return null;
//		}
//		
//		
//	}
	
	
	
	
	
}
