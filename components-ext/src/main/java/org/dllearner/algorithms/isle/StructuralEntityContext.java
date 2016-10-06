/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class StructuralEntityContext {
	
	private static OWLDataFactory df = new OWLDataFactoryImpl();
	private static Set<OWLAnnotationProperty> annotationProperties = Sets.newHashSet(
			df.getRDFSLabel(),
			df.getRDFSComment());
	private static Set<String> languages = Sets.newHashSet("en");
	
	/**
	 * Returns a set of words that describe entities related to the given entity.
	 * @param ontology
	 * @param entity
	 * @return
	 */
	public static Set<String> getContextInNaturalLanguage(OWLOntology ontology, OWLEntity entity){
		Set<String> context = new HashSet<>();
		
		Set<OWLEntity> contextEntities = getContext(ontology, entity);
		//add annotations for each entity
		for (OWLEntity contextEntity : contextEntities) {
			context.addAll(getAnnotations(ontology, contextEntity));
		}
		
		return context;
	}
	
	/**
	 * Returns a set of entities that are structural related to the given entity.
	 * @param ontology
	 * @param entity
	 * @return
	 */
	public static Set<OWLEntity> getContext(OWLOntology ontology, OWLEntity entity){
		
		Set<OWLEntity> context;
		if(entity.isOWLClass()){
			context = getContext(ontology, entity.asOWLClass());
		} else if(entity.isOWLObjectProperty()){
			context = getContext(ontology, entity.asOWLObjectProperty());
		} else if(entity.isOWLDataProperty()){
			context = getContext(ontology, entity.asOWLDataProperty());
		} else {
			throw new UnsupportedOperationException("Unsupported entity type: " + entity);
		}
		
		context.add(entity);
		
		return context;
	}
	
	public static Set<OWLEntity> getContext(OWLOntology ontology, OWLObjectProperty property){
		Set<OWLEntity> context = new HashSet<>();
		
		Set<OWLAxiom> relatedAxioms = new HashSet<>();
		relatedAxioms.addAll(ontology.getObjectSubPropertyAxiomsForSubProperty(property));
		relatedAxioms.addAll(ontology.getEquivalentObjectPropertiesAxioms(property));
		relatedAxioms.addAll(ontology.getObjectPropertyDomainAxioms(property));
		relatedAxioms.addAll(ontology.getObjectPropertyRangeAxioms(property));
				
		for (OWLAxiom axiom : relatedAxioms) {
			context.addAll(axiom.getSignature());
		}
		
		return context;
	}
	
	public static Set<OWLEntity> getContext(OWLOntology ontology, OWLDataProperty property){
		Set<OWLEntity> context = new HashSet<>();
		
		Set<OWLAxiom> relatedAxioms = new HashSet<>();
		relatedAxioms.addAll(ontology.getDataSubPropertyAxiomsForSubProperty(property));
		relatedAxioms.addAll(ontology.getEquivalentDataPropertiesAxioms(property));
		relatedAxioms.addAll(ontology.getDataPropertyDomainAxioms(property));
		
		for (OWLAxiom axiom : relatedAxioms) {
			context.addAll(axiom.getSignature());
		}
		
		return context;
	}
	
	public static Set<OWLEntity> getContext(OWLOntology ontology, OWLClass cls){
		Set<OWLEntity> context = new HashSet<>();
		
		Set<OWLAxiom> relatedAxioms = new HashSet<>();
		relatedAxioms.addAll(ontology.getSubClassAxiomsForSubClass(cls));
		relatedAxioms.addAll(ontology.getEquivalentClassesAxioms(cls));
		
		//axioms where cls is domain of a property
		Set<OWLAxiom> domainAxioms = new HashSet<>();
		domainAxioms.addAll(ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN));
		domainAxioms.addAll(ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN));
		for (Iterator<OWLAxiom> iterator = domainAxioms.iterator(); iterator.hasNext();) {
			OWLAxiom axiom = iterator.next();
			if(!axiom.getSignature().contains(cls)){
				iterator.remove();
			}
		}
		relatedAxioms.addAll(domainAxioms);
		
		//axioms where cls is range of a object property
		Set<OWLAxiom> rangeAxioms = new HashSet<>();
		rangeAxioms.addAll(ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE));
		for (Iterator<OWLAxiom> iterator = rangeAxioms.iterator(); iterator.hasNext();) {
			OWLAxiom axiom = iterator.next();
			if(!axiom.getSignature().contains(cls)){
				iterator.remove();
			}
		}
		relatedAxioms.addAll(rangeAxioms);
		
		for (OWLAxiom axiom : relatedAxioms) {
			context.addAll(axiom.getSignature());
		}
		
		return context;
	}
	
	private static Set<String> getAnnotations(OWLOntology ontology, OWLEntity entity){
		Set<String> annotations = new HashSet<>();
		Set<OWLAnnotationAssertionAxiom> axioms = ontology.getAnnotationAssertionAxioms(entity.getIRI());
		for (OWLAnnotationAssertionAxiom annotation : axioms) {
			if(annotationProperties.contains(annotation.getProperty())){
				if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    if(val.getLang() != null && !val.getLang().isEmpty()){
                    	if(languages.contains(val.getLang())){
                    		if(!val.getLiteral().isEmpty()){
                    			annotations.add(val.getLiteral());
                    		}
                    	}
                    } else {
                    	if(!val.getLiteral().isEmpty()){
                			annotations.add(val.getLiteral());
                		}
                    }
                }
			}
		}
		return annotations;
	}

}
