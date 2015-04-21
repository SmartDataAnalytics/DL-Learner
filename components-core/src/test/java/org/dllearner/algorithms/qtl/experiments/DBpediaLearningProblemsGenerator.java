/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Generate learning problems based on the DBpedia knowledge base.
 * @author Lorenz Buehmann
 *
 */
public class DBpediaLearningProblemsGenerator {
	
	SparqlEndpoint endpoint;
	SparqlEndpointKS ks;
	SPARQLReasoner reasoner;
	ConciseBoundedDescriptionGenerator cbdGen;
	
	
	public DBpediaLearningProblemsGenerator() throws Exception {
		endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir("./qtl-benchmark/cache");
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
	}
	
	private Set<OWLClass> getClasses() {
		return reasoner.getMostSpecificClasses();
	}
	
	public void generateBenchmark(int size, int maxDepth) {
		Set<OWLClass> classes = getClasses();
		
		int i = 0;
		while(i < size && !classes.isEmpty()) {
			
			// pick class randomly
			OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Actor"));
			
			// get individuals
			SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(cls);
			
			// load data
			Model data = ModelFactory.createDefaultModel();
			int cnt = 0;
			for (OWLIndividual ind : individuals) {
				System.out.println(cnt++ + "/" + individuals.size());
				Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxDepth);
				data.add(cbd);
			}
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		new DBpediaLearningProblemsGenerator().generateBenchmark(1, 3);
	}
	

}
