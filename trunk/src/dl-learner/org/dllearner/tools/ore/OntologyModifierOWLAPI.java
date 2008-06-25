package org.dllearner.tools.ore;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.apibinding.OWLManager;
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
import org.semanticweb.owl.model.OWLOntologyChange;
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
	
	/**
	 * Adds an axiom to the ontology, using an EquivalentClassesAxiom
	 * @param newDesc new axiom to add
	 * @param oldDesc old description
	 * @return
	 */
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
	
	/**
	 * saves the ontology as RDF-file
	 */
	public void saveOntology(){
		
		try {
			manager.saveOntology(ontology);
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		URI physicalURI2 = URI.create("file:/tmp/MyOnt2.owl");
//		
//		try {
//			manager.saveOntology(ontology, new RDFXMLOntologyFormat(), physicalURI2);
//		} catch (UnknownOWLOntologyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OWLOntologyStorageException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	/**
	 * Deletes the complete individual from the ontology
	 * @param ind the individual to delete
	 */
	public List<OWLOntologyChange> deleteIndividual(Individual ind){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		
		individualOWLAPI.accept(remover);
		changes.addAll(remover.getChanges());
		try {
			manager.applyChanges(changes);
			saveOntology();
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
		
	}
	/**
	 * Removes a classAssertion 
	 * @param ind the individual which has to removed from class
	 * @param desc the class to which the individual is asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> removeClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, owlDesc);
				
		RemoveAxiom rm = new RemoveAxiom(ontology, owlCl);
		changes.add(rm);
		try {
			manager.applyChange(rm);
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
	}
	
	/**
	 * adds a classAssertion 
	 * @param ind the individual which has to be asserted to class
	 * @param desc the class to which the individual has to be asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, owlDesc);
				
		AddAxiom am = new AddAxiom(ontology, owlCl);
		
		
		changes.add(am);
		try {
			manager.applyChange(am);
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
		
	}
	
	/**
	 * removes classAssertion between individual to a old class, and creates a new classAssertion
	 * @param ind individual which has to be moved
	 * @param oldClass class where individual is asserted before
	 * @param newClass class where individual is moved to
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> moveIndividual(Individual ind, Description oldClass, Description newClass){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual( URI.create(ind.getName()));
		
		//Loeschen
		OWLDescription oldDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(oldClass);
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(individualOWLAPI, oldDesc);
		RemoveAxiom rem = new RemoveAxiom(ontology, owlCl);
		changes.add(rem);
		
		//Hinzufuegen
		
		OWLDescription newDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(newClass);
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(individualOWLAPI, newDesc);
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		changes.add(axiom);
		
		
		try {
			manager.applyChanges(changes);
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param ind the individual which property has to be removed
	 * @param objSome the property which has to be removed
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> deleteObjectProperty(Individual ind, ObjectSomeRestriction objSome){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
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
		changes.addAll(removeList);
		
		try {
			manager.applyChanges(removeList);
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param subInd the individual which is subject in the objectProperty 
	 * @param objSome the property which has to be added to subject
	 * @param objInd the individual which is object in the objectProperty 
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addObjectProperty(Individual subInd, ObjectSomeRestriction objSome, Individual objInd){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subIndividualOWLAPI = factory.getOWLIndividual( URI.create(subInd.getName()));
		OWLIndividual objIndividualOWLAPI = factory.getOWLIndividual( URI.create(objInd.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		OWLObjectPropertyAssertionAxiom objAssertion = factory.getOWLObjectPropertyAssertionAxiom(subIndividualOWLAPI, propertyOWLAPI, objIndividualOWLAPI);
		
		AddAxiom axiom = new AddAxiom(ontology, objAssertion);
		changes.add(axiom);
		try {
			manager.applyChange(axiom);
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
		

	/**
	 * undo changes of type {@link OWLOntologyChange}
	 * @param changes
	 */
	public void undoChanges(List<OWLOntologyChange> changes){
		
		
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				OWLAxiom axiom = ((RemoveAxiom)change).getAxiom();
				AddAxiom add = new AddAxiom(ontology, axiom);
				try {
					manager.applyChange(add);
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(change instanceof AddAxiom){
				OWLClassAssertionAxiom cl = (OWLClassAssertionAxiom)((AddAxiom)change).getAxiom();
				RemoveAxiom rem = new RemoveAxiom(ontology, cl);
				try {
					manager.applyChange(rem);
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	
		}
	}
	
	public boolean isComplement(Description desc1, Description desc2){
		

		OWLDescription d1 = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc1);
		OWLDescription d2 = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc2);
		OWLDescription d3 = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc1.getChild(0));
		
		for(OWLAxiom ax : ontology.getAxioms()){
			if(ax.equals(factory.getOWLEquivalentClassesAxiom(d1, d2)))
				return true;
			if(ax.equals(factory.getOWLDisjointClassesAxiom(d3, d2)))
				return true;
		}
		
//		OWLClass owlClass1 = factory.getOWLClass(URI.create(desc1.toString()));
//		OWLClass owlClass2 = factory.getOWLClass(URI.create(desc2.toString()));
//		for(OWLEquivalentClassesAxiom eq :ontology.getEquivalentClassesAxioms(owlClass1))
//			for(OWLDescription d : eq.getDescriptions())
//				System.out.println(d.isAnonymous());
//		for(OWLEquivalentClassesAxiom eq :ontology.getEquivalentClassesAxioms(owlClass2)){
//			for(OWLDescription d : eq.getDescriptions())
//				System.out.println(d.getClass());
//			for(OWLEntity e : eq.getReferencedEntities())
//				System.out.println(e);
//		}
				
		return false;
		
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
