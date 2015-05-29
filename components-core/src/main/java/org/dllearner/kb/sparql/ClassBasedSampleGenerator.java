/**
 * 
 */
package org.dllearner.kb.sparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Computes a sample fragment of the knowledge base given an OWL class.
 * @author Lorenz Buehmann
 *
 */
public class ClassBasedSampleGenerator extends InstanceBasedSampleGenerator{
	
	private int maxNrOfPosExamples = 20;
	private int maxNrOfNegExamples = 20;
	
	private boolean useNegExamples = true;
	
	private AutomaticNegativeExampleFinderSPARQL2 negExamplesFinder;
	
	private Random rnd = new Random(12345);

	public ClassBasedSampleGenerator(QueryExecutionFactory qef) {
		super(qef);
		
		negExamplesFinder = new AutomaticNegativeExampleFinderSPARQL2(qef);
		
	}
	
	/**
	 * Computes a sample fragment of the knowledge base by using instances of the
	 * given OWL class.
	 * @param cls
	 * @return
	 */
	public OWLOntology getSample(OWLClass cls) {
		// get pos examples
		Set<OWLIndividual> posExamples = computePosExamples(cls);
		
		// get neg examples if enabled
		Set<OWLIndividual> negExamples = computeNegExamples(cls, posExamples);
		
		return getSample(Sets.union(posExamples, negExamples));
	}
	
	private Set<OWLIndividual> computePosExamples(OWLClass cls) {
		List<OWLIndividual> posExamples = new ArrayList<>();
		
		String query = String.format("SELECT ?s WHERE {?s a <%s>}", cls.toStringID());
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			posExamples.add(new OWLNamedIndividualImpl(IRI.create(qs.getResource("s").getURI())));
		}
		qe.close();
		
		Collections.shuffle(posExamples, rnd);
		
		return new TreeSet<>(posExamples.subList(0, Math.min(posExamples.size(), maxNrOfPosExamples)));
	}
	
	private Set<OWLIndividual> computeNegExamples(OWLClass cls, Set<OWLIndividual> posExamples) {
		Set<OWLIndividual> negExamples = new TreeSet<>();
		
		if(useNegExamples && maxNrOfPosExamples > 0) {
			negExamples = negExamplesFinder.getNegativeExamples(cls, posExamples, maxNrOfNegExamples);
		}
		
		return negExamples;
	}

}
