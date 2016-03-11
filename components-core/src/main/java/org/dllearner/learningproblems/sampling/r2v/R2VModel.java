package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.learningproblems.sampling.strategy.FEXStrategy;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource2Vec model.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VModel {
	
	private final static Logger logger = LoggerFactory.getLogger(R2VModel.class);
	
	private OWLOntology o;
	
	// TODO as many indexes (tf-idf) as properties
	private HashMap<String, R2VProperty> properties = new HashMap<>();
	private HashMap<String, R2VInstance> instances = new HashMap<>();
	
	private FEXStrategy strategy;

	public R2VModel(OWLOntology ontology, FEXStrategy strategy) {
		super();
		this.o = ontology;
		this.strategy = strategy;
	}

	public FEXStrategy getStrategy() {
		return strategy;
	}
	
	private void index(String propertyURI, String objValue) {
		
	}
	
	public void add(OWLNamedIndividual ind) {
		
		logger.info("Processing individual "+ind);
		
		// index individual
		R2VInstance instance = new R2VInstance(this, ind.toString());
		instances.put(instance.getUri(), instance);

		// get CBD
		Set<OWLAxiom> cbd = new HashSet<>();
		cbd.addAll(o.getAnnotationAssertionAxioms(ind.getIRI()));
		cbd.addAll(o.getDataPropertyAssertionAxioms(ind));
		cbd.addAll(o.getObjectPropertyAssertionAxioms(ind));
		
		logger.info("CBD size = "+cbd.size());
				
		// compute sparse vector
		
		// for each triple
		for(OWLAxiom axiom : cbd) {
//			logger.info(axiom.toString());
			// check object type
			Triple triple = new Triple();
			if(axiom.isOfType(AxiomType.ANNOTATION_ASSERTION)) {
				OWLAnnotationAssertionAxiom ax = (OWLAnnotationAssertionAxiom) axiom;
				triple.setSubjURI(ax.getSubject().toString());
				triple.setPropURI(ax.getProperty().getIRI().toString());
				try {
					OWLLiteral lit = ax.getValue().asLiteral().get();
					// datatype property
					OWLDatatype dt = lit.getDatatype();
					triple.setDatatype(dt);
					triple.setValue(lit.getLiteral());
				} catch (Exception e) {
					// object property
					triple.setValue(ax.getValue().toString());
				}
				System.out.println(triple);
			} else {
				// TODO for other AxiomTypes (not necessary for now)
				logger.warn("Axiom not processed: "+axiom);
			}
			
			// index property if not done already
			if(!properties.containsKey(triple.getPropURI())) {
				R2VProperty property = new R2VProperty(this, triple.getPropURI());
				properties.put(property.getUri(), property);
			}
			
			if(triple.hasObjectProperty()) {
				// uri -> add to sparse vectors (boolean value)
				System.out.println("   ######## URI #########");
			} else {
				OWLDatatype dt = triple.getDatatype();
				System.out.println(dt);
				if(dt.isBoolean() || dt.isDouble() || dt.isFloat() || dt.isInteger()) {
					// numeric/date -> add to sparse vectors
					System.out.println("   ######## NUMERIC #########");
				} else {
					// string -> add to property index (property->index)
					System.out.println("   ######## STRING #########");
				}
			}
		}
		
//		try {
//			o = m.loadOntologyFromOntologyDocument(IRI.create(file.toURI()));
//		} catch (OWLOntologyCreationException e) {
//			return null;
//		}
	}
	
	
}
