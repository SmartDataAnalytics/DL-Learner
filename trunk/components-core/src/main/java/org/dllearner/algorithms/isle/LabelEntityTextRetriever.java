/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.owl.Entity;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


/**
 * @author Lorenz Buehmann
 *
 */
public class LabelEntityTextRetriever implements EntityTextRetriever{
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	private OWLAnnotationProperty label = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	
	private String language = "en";
	private double weight = 1d;
	
	private boolean useShortFormFallback = true;
	private IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();

	public LabelEntityTextRetriever(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	public LabelEntityTextRetriever(OWLAPIOntology ontology) {
		this.ontology = ontology.createOWLOntology(manager);
	}
	
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * Whether to use the short form of the IRI as fallback, if no label is given.
	 * @param useShortFormFallback the useShortFormFallback to set
	 */
	public void setUseShortFormFallback(boolean useShortFormFallback) {
		this.useShortFormFallback = useShortFormFallback;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.EntityTextRetriever#getRelevantText(org.dllearner.core.owl.Entity)
	 */
	@Override
	public Map<String, Double> getRelevantText(Entity entity) {
		Map<String, Double> textWithWeight = new HashMap<String, Double>();
		
		OWLEntity e = OWLAPIConverter.getOWLAPIEntity(entity);
		
		Set<OWLAnnotation> annotations = e.getAnnotations(ontology, label);
		for (OWLAnnotation annotation : annotations) {
			if (annotation.getValue() instanceof OWLLiteral) {
	            OWLLiteral val = (OWLLiteral) annotation.getValue();
	            if (val.hasLang(language)) {
	            	String label = val.getLiteral();
	            	textWithWeight.put(label, weight);
	            }
	        }
		}
		
		if(textWithWeight.isEmpty() && useShortFormFallback){
			textWithWeight.put(sfp.getShortForm(IRI.create(entity.getURI())), weight);
		}
		
		return textWithWeight;
	}
}
