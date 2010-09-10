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

import java.util.ArrayList;
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
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

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
	
	private List<OWLOntologyChange> globalChanges;
	
	
	
	public OntologyModifier(PelletReasoner reasoner){
		this.reasoner = reasoner;
		this.manager = reasoner.getOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontology = (reasoner.getOWLAPIOntologies());
		
		globalChanges = new ArrayList<OWLOntologyChange>();
		
	}
	
	/**
	 * Adds an EquivalentClassesAxiom axiom to the ontology. 
	 * @param newDesc new axiom to add
	 * @param oldDesc old description
	 */
	public OWLOntologyChange addAxiomToOWL(Description newDesc, Description oldDesc){
		
		
		OWLClassExpression newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(newDesc);
		OWLClassExpression oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(oldDesc);
		
		Set<OWLClassExpression> ds = new HashSet<OWLClassExpression>();
		ds.add(newConceptOWLAPI);
		ds.add(oldConceptOWLAPI);
		
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		
		
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
			globalChanges.add(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return axiom;
		
	}
	
	public void addEquivalentClassDescription(NamedClass old, Description newDesc){
		OWLClassExpression oldOWLAPIDesc = OWLAPIConverter.getOWLAPIDescription(old);
		OWLClassExpression newOWLAPIDesc = OWLAPIConverter.getOWLAPIDescription(newDesc);
		OWLEquivalentClassesAxiom equivAxiom = factory.getOWLEquivalentClassesAxiom(oldOWLAPIDesc, newOWLAPIDesc);
		AddAxiom add = new AddAxiom(ontology, equivAxiom);
		try {
			manager.applyChange(add);
			globalChanges.add(add);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSuperClassDescription(NamedClass old, Description newDesc){
		OWLClassExpression subClass = OWLAPIConverter.getOWLAPIDescription(old);
		OWLClassExpression superClass = OWLAPIConverter.getOWLAPIDescription(newDesc);
		OWLSubClassOfAxiom subAxiom = factory.getOWLSubClassOfAxiom(subClass, superClass);
		AddAxiom add = new AddAxiom(ontology, subAxiom);
		try {
			manager.applyChange(add);
			globalChanges.add(add);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Rewrite ontology by replacing old class with new learned class description.
	 * @param newDesc
	 * @param oldClass
	 */
	public List<OWLOntologyChange> rewriteClassDescription(NamedClass newDesc, Description oldClass){
		OWLClassExpression newClassDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(newDesc);
//		OWLClassExpression oldClassDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(oldClass);
		
		OWLClass oldClassOWL = factory.getOWLClass(IRI.create(oldClass.toString()));
		
		Set<OWLEquivalentClassesAxiom> equivalenceAxioms = ontology.getEquivalentClassesAxioms(oldClassOWL);
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		//add old equivalence axioms to changes
		for(OWLEquivalentClassesAxiom eqAxiom : equivalenceAxioms){
			changes.add(new RemoveAxiom(ontology, eqAxiom));
			
		}
		
		//create and add new equivalence axiom to changes
		
		Set<OWLClassExpression> newEquivalenceDesc = new HashSet<OWLClassExpression>();
		newEquivalenceDesc.add(newClassDesc);
		newEquivalenceDesc.add(oldClassOWL);
		OWLAxiom equivalenceAxiom = factory.getOWLEquivalentClassesAxiom(newEquivalenceDesc);
		AddAxiom addAxiom = new AddAxiom(ontology, equivalenceAxiom);
		changes.add(addAxiom);
		
		//apply changes to ontology
		try {
			manager.applyChanges(changes);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			System.err.println("Error: rewriting class description failed");
			e.printStackTrace();
		}
		
		return changes;
		
	}
	
	
	/**
	 * Deletes the complete individual from the ontology.
	 * @param ind the individual to delete
	 */
	public List<OWLOntologyChange> deleteIndividual(Individual ind){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLNamedIndividual individualOWLAPI = factory.getOWLNamedIndividual(IRI.create(ind.getName()));
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		
		individualOWLAPI.accept(remover);
		changes.addAll(remover.getChanges());
		try {
			reasoner.updateCWAOntology(changes);
			manager.applyChanges(changes);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changes;
		
	}
	/**
	 * Removes a classAssertion. 
	 * @param ind the individual which has to removed from class
	 * @param desc the class to which the individual is asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> removeClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLNamedIndividual(IRI.create(ind.getName()));
		OWLClassExpression owlDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(owlDesc, individualOWLAPI);
				
		RemoveAxiom rm = new RemoveAxiom(ontology, owlCl);
		changes.add(rm);
		try {
			reasoner.updateCWAOntology(changes);
			manager.applyChange(rm);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changes;
	}
	
	/**
	 * Adds a classAssertion. 
	 * @param ind the individual which has to be asserted to class
	 * @param desc the class to which the individual has to be asserted
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addClassAssertion(Individual ind, Description desc){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = factory.getOWLNamedIndividual(IRI.create(ind.getName()));
		OWLClassExpression owlDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc);
		
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(owlDesc, individualOWLAPI);
				
		AddAxiom am = new AddAxiom(ontology, owlCl);
		changes.add(am);
		try {
			reasoner.updateCWAOntology(changes);
			manager.applyChange(am);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		return changes;
		
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
		
		OWLIndividual individualOWLAPI = factory.getOWLNamedIndividual(IRI.create(ind.getName()));
		
		//Loeschen
		OWLClassExpression oldDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(oldClass);
		OWLClassAssertionAxiom owlCl = factory.getOWLClassAssertionAxiom(oldDesc, individualOWLAPI);
		RemoveAxiom rem = new RemoveAxiom(ontology, owlCl);
		changes.add(rem);
		
		//Hinzufuegen
		
		OWLClassExpression newDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(newClass);
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(newDesc, individualOWLAPI);
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		changes.add(axiom);
		
		
		try {
			reasoner.updateCWAOntology(changes);
			manager.applyChanges(changes);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return changes;
		
		
	}
	
	/**
	 * Removes all object property assertions with role, and it's inverse.
	 * @param ind the individual which property has to be removed
	 * @param objSome the property which has to be removed
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> deleteObjectProperty(Individual ind, ObjectPropertyExpression property){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual individualOWLAPI = OWLAPIConverter.getOWLAPIIndividual(ind);
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(IRI.create(property.getName()));
		
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
			reasoner.updateCWAOntology(changes);
			manager.applyChanges(changes);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return changes;
		
	}
	
		
	/**
	 * Removes an object property assertion from the ontology if the axiom is existing in the ontology.
	 * @param subject
	 * @param objSome
	 * @param object
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> removeObjectPropertyAssertion(Individual subject, ObjectPropertyExpression property, Individual object){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subjectOWLAPI = factory.getOWLNamedIndividual(IRI.create(subject.getName()));
		OWLIndividual objectOWLAPI = factory.getOWLNamedIndividual(IRI.create(object.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(IRI.create(property.getName()));
		
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(subjectOWLAPI);
	
		RemoveAxiom remove = null;
		for(OWLObjectPropertyAssertionAxiom o :properties){System.out.println(o);
			if((o.getProperty().equals(propertyOWLAPI)) && (o.getSubject().equals(subjectOWLAPI)) && (o.getObject().equals(objectOWLAPI))){ 
				remove = new RemoveAxiom(ontology, o);
			}
		}
		
			
		
		changes.add(remove);
		
		try {
			if(remove != null){
				reasoner.updateCWAOntology(changes);
				manager.applyChange(remove);
				globalChanges.addAll(changes);
			}
			
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changes;
		
	}
	
	public List<OWLOntologyChange> removeAllObjectPropertyAssertions(Individual subject, ObjectPropertyExpression property,Set<Individual> objects){
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subjectOWLAPI = factory.getOWLNamedIndividual(IRI.create(subject.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(IRI.create(property.getName()));
		Set<OWLIndividual> objectsOWLAPI = new HashSet<OWLIndividual>();
		for(Individual ind : objects){
			objectsOWLAPI.add(OWLAPIConverter.getOWLAPIIndividual(ind));
		}
		
		
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(subjectOWLAPI);
		
		for(OWLObjectPropertyAssertionAxiom o :properties){
			if((o.getProperty().equals(propertyOWLAPI)) && objectsOWLAPI.contains(o.getObject())){ 
				changes.add(new RemoveAxiom(ontology, o));
			}
		}
		
		try {	
				reasoner.updateCWAOntology(changes);
				manager.applyChanges(changes);
				globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changes;
	}
	
	/**
	 * Adds an object property assertion to the ontology.
	 * @param subInd the individual which is subject in the objectProperty 
	 * @param objSome the property which has to be added to subject
	 * @param objInd the individual which is object in the objectProperty 
	 * @return changes that have been done
	 */
	public List<OWLOntologyChange> addObjectProperty(Individual subInd, ObjectPropertyExpression property, Individual objInd){
		
		List<OWLOntologyChange> changes = new LinkedList<OWLOntologyChange>();
		
		OWLIndividual subjectOWLAPI = factory.getOWLNamedIndividual(IRI.create(subInd.getName()));
		OWLIndividual objectOWLAPI = factory.getOWLNamedIndividual(IRI.create(objInd.getName()));
		OWLObjectProperty propertyOWLAPI = factory.getOWLObjectProperty(IRI.create(property.getName()));
		
		OWLObjectPropertyAssertionAxiom objAssertion = factory.getOWLObjectPropertyAssertionAxiom(propertyOWLAPI, subjectOWLAPI, objectOWLAPI);
		AddAxiom axiom = new AddAxiom(ontology, objAssertion);
		changes.add(axiom);
		try {
			reasoner.updateCWAOntology(changes);
			manager.applyChange(axiom);
			globalChanges.addAll(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changes;
	}
	
	public void applyOntologyChanges(List<OWLOntologyChange> changes){
		try {
			manager.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		globalChanges.addAll(changes);
	}

	/**
	 * undo changes of type {@link OWLOntologyChange}.
	 * @param changes
	 */
	public void undoChanges(Collection<OWLOntologyChange> changes){
		
		List<OWLOntologyChange> undoChanges = new ArrayList<OWLOntologyChange>(changes.size());
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				AddAxiom add = new AddAxiom(ontology, change.getAxiom());
				undoChanges.add(add);
			} else if(change instanceof AddAxiom){
				RemoveAxiom remove = new RemoveAxiom(ontology, change.getAxiom());
				undoChanges.add(remove);
			}
	
		}
		try {
			reasoner.updateCWAOntology(undoChanges);
			manager.applyChanges(undoChanges);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		globalChanges.removeAll(changes);
	
		
	}
	
	public List<OWLOntologyChange> getChanges(){
		return globalChanges;
	}
	
	/**
	 * checks whether desc1 and desc2 are disjoint.
	 * @param desc1 class 1
	 * @param desc2 class 2
	 */
	public boolean isComplement(Description desc1, Description desc2){

		OWLClass owlClass1 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc1).asOWLClass();
		OWLClass owlClass2 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc2).asOWLClass();
		
		//superclasses and class1
//		Set<OWLClassExpression> superClasses1 = owlClass1.getSuperClasses(ontology);
		Set<OWLClassExpression> superClasses1 = new HashSet<OWLClassExpression>();
		for(Description d1 : reasoner.getSuperClasses(desc1)){
			superClasses1.add(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d1));
		}
		superClasses1.add(owlClass1);
//		System.out.println(desc1 + "::" + superClasses1);
		
		//superclasses and class2
//		Set<OWLClassExpression> superClasses2 = owlClass2.getSuperClasses(ontology);
		Set<OWLClassExpression> superClasses2 = new HashSet<OWLClassExpression>();
		for(Description d2 : reasoner.getSuperClasses(desc2)){
			superClasses2.add(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d2));
		}
		superClasses2.add(owlClass2);
		
//		System.out.println("superklassen von " + desc2 + " sind: "  + superClasses2);
		for(OWLClassExpression o1 : superClasses1){
			
			OWLClassExpression negO1 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(new Negation(new NamedClass(o1.toString())));
			for(OWLClassExpression o2 : superClasses2){

				OWLClassExpression negO2 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(new Negation(new NamedClass(o2.toString())));
				
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
//			for(OWLClassExpression o1 : superClasses1){
//				
//				OWLClassExpression negO1 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(new Negation(new NamedClass(o1.toString())));
//				for(OWLClassExpression o2 : superClasses2){
//
//					OWLClassExpression negO2 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(new Negation(new NamedClass(o2.toString())));
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
		Set<OWLObjectPropertyAssertionAxiom> owlObjectProperties = ontology.getObjectPropertyAssertionAxioms(factory.getOWLNamedIndividual(IRI.create(ind.getName())));
		
		
		for(OWLObjectPropertyAssertionAxiom o : owlObjectProperties){
			ObjectProperty ob = new ObjectProperty(o.getProperty().asOWLObjectProperty().getIRI().toString());
			Individual obj = new Individual(o.getObject().asOWLNamedIndividual().getIRI().toString());
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
	
}
