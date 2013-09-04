/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleWordSenseDisambiguation extends WordSenseDisambiguation{
	
	private IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private OWLAnnotationProperty annotationProperty = df.getRDFSLabel();

	/**
	 * @param ontology
	 */
	public SimpleWordSenseDisambiguation(OWLOntology ontology) {
		super(ontology);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.WordSenseDisambiguation#disambiguate(org.dllearner.algorithms.isle.index.Annotation, java.util.Set)
	 */
	@Override
	public SemanticAnnotation disambiguate(Annotation annotation, Set<Entity> candidateEntities) {
		String token = annotation.getToken();
		//check if annotation token matches label of entity or the part behind #(resp. /)
		for (Entity entity : candidateEntities) {
			Set<String> labels = getLabels(entity);
			for (String label : labels) {
				if(label.equals(token)){
					return new SemanticAnnotation(annotation, entity);
				}
			}
			String shortForm = sfp.getShortForm(IRI.create(entity.getURI()));
			if(annotation.equals(shortForm)){
				return new SemanticAnnotation(annotation, entity);
			}
		}
		return null;
	}
	
	private Set<String> getLabels(Entity entity){
		Set<String> labels = new HashSet<String>();
		OWLEntity owlEntity = OWLAPIConverter.getOWLAPIEntity(entity);
		Set<OWLAnnotationAssertionAxiom> axioms = ontology.getAnnotationAssertionAxioms(owlEntity.getIRI());
		for (OWLAnnotationAssertionAxiom annotation : axioms) {
			if(annotation.getProperty().equals(annotationProperty)){
				if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    labels.add(val.getLiteral());
                }
			}
		}
		return labels;
	}
	
	private Set<String> getRelatedWordPhrases(Entity entity){
		//add the labels if exist
		Set<String> relatedWordPhrases = new HashSet<String>();
		OWLEntity owlEntity = OWLAPIConverter.getOWLAPIEntity(entity);
		Set<OWLAnnotationAssertionAxiom> axioms = ontology.getAnnotationAssertionAxioms(owlEntity.getIRI());
		for (OWLAnnotationAssertionAxiom annotation : axioms) {
			if(annotation.getProperty().equals(annotationProperty)){
				if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    relatedWordPhrases.add(val.getLiteral());
                }
			}
		}
		//add the short form of the URI if no labels are available
		if(relatedWordPhrases.isEmpty()){
			relatedWordPhrases.add(sfp.getShortForm(IRI.create(entity.getURI())));
		}
		return relatedWordPhrases;
	}

}
