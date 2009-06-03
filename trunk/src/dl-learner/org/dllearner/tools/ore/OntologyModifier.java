/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */

package org.dllearner.tools.ore;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.debugging.BlackBoxOWLDebugger;
import org.semanticweb.owl.debugging.OWLDebugger;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.OWLSatisfiabilityChecker;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
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

/**
 * This class provides several methods to modify the ontology by using OWL-API.
 * @author Lorenz Buehmann
 *
 */
public class OntologyModifier {

	private OWLOntology ontology;
	private PelletReasoner reasoner;
	private OWLDataFactory factory;
	private OWLOntologyManager manager;
	
	
	
	public OntologyModifier(PelletReasoner reasoner){
		this.reasoner = reasoner;
		this.manager = reasoner.getOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontology = (reasoner.getOWLAPIOntologies());
		
	}
	
	/**
	 * Adds an EquivalentClassesAxiom axiom to the ontology. 
	 * @param newDesc new axiom to add
	 * @param oldDesc old description
	 */
	public OWLOntologyChange addAxiomToOWL(Description newDesc, Description oldDesc){
		
		
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
		return axiom;
		
	}
	
	/**
	 * Rewrite ontology by replacing old class with new learned class description.
	 * @param newDesc
	 * @param oldClass
	 */
	public List<OWLOntologyChange> rewriteClassDescription(Description newDesc, Description oldClass){
		OWLDescription newClassDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(newDesc);
//		OWLDescription oldClassDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(oldClass);
		
		OWLClass oldClassOWL = factory.getOWLClass(URI.create(oldClass.toString()));
		
		Set<OWLEquivalentClassesAxiom> equivalenceAxioms = ontology.getEquivalentClassesAxioms(oldClassOWL);
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		//add old equivalence axioms to changes
		for(OWLEquivalentClassesAxiom eqAxiom : equivalenceAxioms){
			changes.add(new RemoveAxiom(ontology, eqAxiom));
			
		}
		
		//create and add new equivalence axiom to changes
		
		Set<OWLDescription> newEquivalenceDesc = new HashSet<OWLDescription>();
		newEquivalenceDesc.add(newClassDesc);
		newEquivalenceDesc.add(oldClassOWL);
		OWLAxiom equivalenceAxiom = factory.getOWLEquivalentClassesAxiom(newEquivalenceDesc);
		AddAxiom addAxiom = new AddAxiom(ontology, equivalenceAxiom);
		changes.add(addAxiom);
		
		//apply changes to ontology
		try {
			manager.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			System.err.println("Error: rewriting class description failed");
			e.printStackTrace();
		}
		
		return changes;
		
	}
	
	/**
	 * Saves the ontology as RDF-file.
	 */
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
	 * Deletes the complete individual from the ontology.
	 * @param ind the individual to delete
	 */
	public List<OWLOntologyChange> deleteIndividual(Individual ind){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual(URI.create(ind.getName()));
		
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
	 * Removes a classAssertion. 
	 * @param ind the individual which has to removed from class
	 * @param desc the class to which the individual is asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> removeClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual(URI.create(ind.getName()));
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
	 * Adds a classAssertion. 
	 * @param ind the individual which has to be asserted to class
	 * @param desc the class to which the individual has to be asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual(URI.create(ind.getName()));
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
	 * Removes classAssertion between individual to a old class, and creates a new classAssertion.
	 * @param ind individual which has to be moved
	 * @param oldClass class where individual is asserted before
	 * @param newClass class where individual is moved to
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> moveIndividual(Individual ind, Description oldClass, Description newClass){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual(URI.create(ind.getName()));
		
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
			saveOntology();
			
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Removes all object property assertions with role, and it's inverse.
	 * @param ind the individual which property has to be removed
	 * @param objSome the property which has to be removed
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> deleteObjectProperty(Individual ind, ObjectQuantorRestriction objSome){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLIndividual(URI.create(ind.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(individualOWLAPI);
		Set<OWLInverseObjectPropertiesAxiom> invProperties = ontology.getInverseObjectPropertyAxioms(propertyOWLAPI);
		
		OWLObjectPropertyExpression invProperty = null;
		
		for(OWLInverseObjectPropertiesAxiom inv : invProperties){
			if(propertyOWLAPI.equals(inv.getSecondProperty())){
				invProperty  = inv.getFirstProperty();
			} else{
				invProperty = inv.getSecondProperty();
			}
		}
		
		
		List<RemoveAxiom> removeList = new LinkedList<RemoveAxiom>();
		
		for(OWLObjectPropertyAssertionAxiom o :properties){
			if((o.getProperty().equals(propertyOWLAPI)) && (o.getSubject().equals(individualOWLAPI))){ 
				removeList.add(new RemoveAxiom(ontology, o));
			}
			if(invProperty != null){
				for(OWLObjectPropertyAssertionAxiom ob :ontology.getObjectPropertyAssertionAxioms(o.getObject())){
					if(ob.getProperty().equals(invProperty) && ob.getObject().equals(individualOWLAPI)){
						removeList.add(new RemoveAxiom(ontology, ob));
					}
				}
			}
			
			
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
	 * Removes an object property assertion from the ontology if the axiom is existing in the ontology.
	 * @param subject
	 * @param objSome
	 * @param object
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> removeObjectPropertyAssertion(Individual subject, ObjectQuantorRestriction objSome, Individual object){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subjectOWLAPI = factory.getOWLIndividual(URI.create(subject.getName()));
		OWLIndividual objectOWLAPI = factory.getOWLIndividual(URI.create(object.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(subjectOWLAPI);
		
		RemoveAxiom remove = null;
		for(OWLObjectPropertyAssertionAxiom o :properties){
			if((o.getProperty().equals(propertyOWLAPI)) && (o.getSubject().equals(subjectOWLAPI)) && (o.getObject().equals(objectOWLAPI))){ 
				remove = new RemoveAxiom(ontology, o);
			}
		}
		
			
		
		changes.add(remove);
		
		try {
			if(remove != null){
				manager.applyChange(remove);
				
			}
			return changes;
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Adds an object property assertion to the ontology.
	 * @param subInd the individual which is subject in the objectProperty 
	 * @param objSome the property which has to be added to subject
	 * @param objInd the individual which is object in the objectProperty 
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addObjectProperty(Individual subInd, ObjectQuantorRestriction objSome, Individual objInd){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subjectOWLAPI = factory.getOWLIndividual(URI.create(subInd.getName()));
		OWLIndividual objectOWLAPI = factory.getOWLIndividual(URI.create(objInd.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(URI.create(objSome.getRole().getName()));
		
		OWLObjectPropertyAssertionAxiom objAssertion = factory.getOWLObjectPropertyAssertionAxiom(subjectOWLAPI, propertyOWLAPI, objectOWLAPI);
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
	 * undo changes of type {@link OWLOntologyChange}.
	 * @param changes
	 */
	public void undoChanges(Collection<OWLOntologyChange> changes){
		
		
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				AddAxiom add = new AddAxiom(ontology, change.getAxiom());
				try {
					manager.applyChange(add);
				} catch (OWLOntologyChangeException e) {
					e.printStackTrace();
				}
			} else if(change instanceof AddAxiom){
				RemoveAxiom remove = new RemoveAxiom(ontology, change.getAxiom());
				try {
					manager.applyChange(remove);
				} catch (OWLOntologyChangeException e) {
					e.printStackTrace();
				}
			}
	
		}
	
		
	}
	
	/**
	 * checks whether desc1 and desc2 are disjoint.
	 * @param desc1 class 1
	 * @param desc2 class 2
	 */
	public boolean isComplement(Description desc1, Description desc2){

		OWLClass owlClass1 = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc1).asOWLClass();
		OWLClass owlClass2 = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc2).asOWLClass();
		
		//superclasses and class1
//		Set<OWLDescription> superClasses1 = owlClass1.getSuperClasses(ontology);
		Set<OWLDescription> superClasses1 = new HashSet<OWLDescription>();
		for(Description d1 : reasoner.getSuperClasses(desc1)){
			superClasses1.add(OWLAPIDescriptionConvertVisitor.getOWLDescription(d1));
		}
		superClasses1.add(owlClass1);
//		System.out.println(desc1 + "::" + superClasses1);
		
		//superclasses and class2
//		Set<OWLDescription> superClasses2 = owlClass2.getSuperClasses(ontology);
		Set<OWLDescription> superClasses2 = new HashSet<OWLDescription>();
		for(Description d2 : reasoner.getSuperClasses(desc2)){
			superClasses2.add(OWLAPIDescriptionConvertVisitor.getOWLDescription(d2));
		}
		superClasses2.add(owlClass2);
		
//		System.out.println("superklassen von " + desc2 + " sind: "  + superClasses2);
		for(OWLDescription o1 : superClasses1){
			
			OWLDescription negO1 = OWLAPIDescriptionConvertVisitor.getOWLDescription(new Negation(new NamedClass(o1.toString())));
			for(OWLDescription o2 : superClasses2){

				OWLDescription negO2 = OWLAPIDescriptionConvertVisitor.getOWLDescription(new Negation(new NamedClass(o2.toString())));
				
				if(ontology.containsAxiom(factory.getOWLDisjointClassesAxiom(o1, o2))){
					return true;
				}else if(ontology.containsAxiom(factory.getOWLDisjointClassesAxiom(o2, o1))){
					return true;
				}else if(ontology.containsAxiom(factory.getOWLEquivalentClassesAxiom(o1, negO2))){
					return true;
				}else if(ontology.containsAxiom(factory.getOWLEquivalentClassesAxiom(o2, negO1))){
					return true;
				}
			}
		
		}
		
//		for(OWLAxiom ax : ontology.getAxioms()){
//			
//			for(OWLDescription o1 : superClasses1){
//				
//				OWLDescription negO1 = OWLAPIDescriptionConvertVisitor.getOWLDescription(new Negation(new NamedClass(o1.toString())));
//				for(OWLDescription o2 : superClasses2){
//
//					OWLDescription negO2 = OWLAPIDescriptionConvertVisitor.getOWLDescription(new Negation(new NamedClass(o2.toString())));
//					
//					if(ax.toString().equals(factory.getOWLDisjointClassesAxiom(o1, o2).toString())){
//						return true;
//					}else if(ax.toString().equals(factory.getOWLDisjointClassesAxiom(o2, o1).toString())){
//						return true;
//					}else if(ax.toString().equals(factory.getOWLEquivalentClassesAxiom(o1, negO2).toString())){
//						return true;
//					}else if(ax.toString().equals(factory.getOWLEquivalentClassesAxiom(o2, negO1).toString())){
//						return true;
//					}
//				}
//			
//			}
//		}
			
		
	
		return false;
		
	}
	
	/**
	 * Returns object properties for an individual.
	 * @param ind
	 */
	public Set<ObjectPropertyAssertion> getObjectProperties(Individual ind){
		Set<ObjectPropertyAssertion> objectProperties = new HashSet<ObjectPropertyAssertion>();
		Set<OWLObjectPropertyAssertionAxiom> owlObjectProperties = ontology.getObjectPropertyAssertionAxioms(factory.getOWLIndividual(URI.create(ind.getName())));
		
		
		for(OWLObjectPropertyAssertionAxiom o : owlObjectProperties){
			ObjectProperty ob = new ObjectProperty(o.getProperty().asOWLObjectProperty().getURI().toString());
			Individual obj = new Individual(o.getObject().getURI().toString());
			objectProperties.add(new ObjectPropertyAssertion(ob, ind, obj));
		
		}
		return objectProperties;
	}
	
	/**
	 * Returns the actual ontology.
	 * @return ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	
/**
 * Prints reasons for inconsistent classes.
 */
	public void reason(){
//		reasoner.getInconsistencyReasons(ontology);
		
		       

	        /* Create a satisfiability checker */
	        OWLSatisfiabilityChecker checker = new Reasoner(manager);
	        try {
				checker.loadOntologies(Collections.singleton(ontology));
			} catch (OWLReasonerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        OWLDebugger debugger = new BlackBoxOWLDebugger(manager, ontology, checker);
	        
	        for(OWLClass owlClass : reasoner.getInconsistentOWLClasses()){
	        /* Find the sets of support and print them */
		        Set<Set<OWLAxiom>> allsos = null;
				try {
					allsos = debugger.getAllSOSForIncosistentClass(owlClass);
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
		        for (Set<OWLAxiom> sos : allsos){
		            System.out.println(sos);
		        }
	        }

		

	}
	
	

	
	
	
	
	
}
