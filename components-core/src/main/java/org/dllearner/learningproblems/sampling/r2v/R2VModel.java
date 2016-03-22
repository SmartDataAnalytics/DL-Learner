package org.dllearner.learningproblems.sampling.r2v;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.learningproblems.sampling.strategy.FEXStrategy;
import org.dllearner.learningproblems.sampling.strategy.TfidfFEXStrategy;
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
	
	private OWLOntology ontology;
	
	// as many indexes (tf-idf) as properties
	private HashMap<String, R2VProperty> properties = new HashMap<>();
	private HashMap<String, R2VInstance> instances = new HashMap<>();
	
	private FEXStrategy strategy;

	public R2VModel(OWLOntology ontology, FEXStrategy strategy) {
		super();
		this.ontology = ontology;
		this.strategy = strategy;
	}

	public FEXStrategy getStrategy() {
		return strategy;
	}
	
	public void normalize() {
		// TODO
	}
	
	/**
	 * 	Compute features for string values.
	 */
	public void stringFeatures() {
		
		if(strategy instanceof TfidfFEXStrategy) {
			// for each property
			for(R2VProperty property : properties.values())
				property.getTextIndex().compute();
			
			// compute indexes (word2vec or tf-idf)
			for(R2VInstance instance : instances.values()) {
				for(R2VProperty property : instance.getFeatures().keySet()) {
					TfidfIndex index = property.getTextIndex();
					R2VFeature feature = instance.getFeatures().get(property);
					if(feature.getType().equals(R2VFeatureType.STRING)) {
						Map<String, Double> map = index.tfidf(feature.getStringValue());
						for(String term : map.keySet()) {
							R2VSubfeature sub = new R2VSubfeature(feature, term, map.get(term));
							feature.getSubfeatures().put(term, sub);
						}
					}
				}
				logger.info(instance.getUri() + "\t" + instance.getFlatSparseVector());
			}
		} else {
			logger.info("WARNING: Unknown FEX strategy type! Skipping string processing...");
		}
		
	}
	
	/**
	 * @param ind
	 */
	public void add(OWLNamedIndividual ind) {
		
		logger.info("Processing individual "+ind);
		
		// index individual
		R2VInstance instance = new R2VInstance(this, ind);
		instances.put(instance.getUri(), instance);

		// get CBD
		Set<OWLAxiom> cbd = new HashSet<>();
		cbd.addAll(ontology.getAnnotationAssertionAxioms(ind.getIRI()));
		cbd.addAll(ontology.getDataPropertyAssertionAxioms(ind));
		cbd.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
		
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
				logger.info(triple.toString());
			} else {
				// TODO for other AxiomTypes (not necessary for now)
				logger.warn("Axiom not processed: "+axiom);
			}
			
			// index property if not done already
			R2VProperty property;
			if(!properties.containsKey(triple.getPropURI())) {
				property = new R2VProperty(this, triple.getPropURI());
				properties.put(property.getUri(), property);
			} else {
				property = properties.get(triple.getPropURI());
			}
			
			// index feature
			R2VFeature feature;
			if(!instance.getFeatures().containsKey(property)) {
				feature = new R2VFeature(this, instance, property);
				instance.getFeatures().put(property, feature);
			} else {
				feature = instance.getFeatures().get(property);
			}
			
			if(triple.hasObjectProperty()) {
				// uri -> add to sparse vectors (boolean value)
				System.out.println("   ######## URI #########");
				feature.getSubfeatures().put(triple.getValue(), new R2VSubfeature(feature, triple.getValue(), 1.0));
				feature.setType(R2VFeatureType.URI);
			} else {
				OWLDatatype dt = triple.getDatatype();
				System.out.println(dt);
				if(dt.isBoolean() || dt.isDouble() || dt.isFloat() || dt.isInteger()) {
					// numeric/date -> add to sparse vectors
					System.out.println("   ######## NUMERIC #########");
					feature.add(Double.parseDouble(triple.getValue()));
					feature.setType(R2VFeatureType.NUMERICAL);
				} else {
					// string -> add to property index (property->index)
					System.out.println("   ######## STRING #########");
					feature.setType(R2VFeatureType.STRING);
					feature.setStringValue(triple.getValue());
					property.getTextIndex().addNumeric(triple.getValue());
				}
			}
			
		} // end for each axiom
				
	}

	/**
	 * @param points 
	 * @return
	 */
	public OWLNamedIndividual getMeanPoint(Collection<OWLNamedIndividual> points) {
		// TODO this is a dummy test
		for(R2VInstance instance : instances.values())
			if(points.contains(instance.getIndividual()))
				return instance.getIndividual();
		return null;
	}
	
	/**
	 * Euclidean distance of two individuals within the model.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public Double distance(OWLNamedIndividual a, OWLNamedIndividual b) {
		HashMap<String, Double> aV = instances.get(a.toString()).getFlatSparseVector();
		HashMap<String, Double> bV = instances.get(b.toString()).getFlatSparseVector();
		TreeSet<String> commonSpace = new TreeSet<>();
		commonSpace.addAll(aV.keySet());
		commonSpace.addAll(bV.keySet());
		Double sum = 0d;
		for(String dim : commonSpace) {
			Double aVal = aV.containsKey(dim) ? aV.get(dim) : 0d;
			Double bVal = bV.containsKey(dim) ? bV.get(dim) : 0d;
			sum += Math.pow(aVal - bVal, 2);
		}
		return Math.sqrt(sum);
	}
	
	@Override
	public String toString() {
		return "R2VModel [instances=" + instances + ", strategy=" + strategy
				+ "]";
	}

	public String info() {
		TreeSet<String> subf = new TreeSet<>();
		for(R2VInstance instance : instances.values())
			for(R2VFeature feature : instance.getFeatures().values())
				subf.addAll(feature.getSubfeatures().keySet());
		return "R2VModel #instances="+instances.size()+" #properties="+properties.size()+" #features="+subf.size();
	}

	
}
