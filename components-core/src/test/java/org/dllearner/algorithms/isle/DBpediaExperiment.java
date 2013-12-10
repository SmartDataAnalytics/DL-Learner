/**
 * 
 */
package org.dllearner.algorithms.isle;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaExperiment extends Experiment{
	
	final SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	final int maxNrOfInstancesPerClass = 100;
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getOntology()
	 */
	@Override
	protected OWLOntology getOntology() {
		//load the DBpedia schema
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			 CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			 OWLOntology schema = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
			 return schema;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		//load some sample data for the machine learning part
		Model sample = KnowledgebaseSampleGenerator.createKnowledgebaseSample(
				endpoint, 
				"http://dbpedia.org/ontology/", 
				Sets.newHashSet(new NamedClass("http://dbpedia.org/ontology/Person")),
				maxNrOfInstancesPerClass);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			sample.write(baos, "TURTLE", null);
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getDocuments()
	 */
	@Override
	protected Set<String> getDocuments() {
		Set<String> documents = new HashSet<String>();
		
		documents.addAll(DBpediaCorpusGenerator.getDBpediaCorpusSample(
				"http://dbpedia.org/ontology/abstract", 
				Sets.newHashSet(new NamedClass("http://dbpedia.org/ontology/Person")),
				maxNrOfInstancesPerClass));
        
		documents.clear();
        documents.add("Thomas Cruise Mapother IV, widely known as Tom Cruise, is an American film player and producer. He has been nominated for three Academy Awards and has won three Golden Globe Awards. He started his career at age 19 in the 1981 film Taps. His first leading role was in Risky Business, released in August 1983. Cruise became a full-fledged movie star after starring in Top Gun (1986). He is well known for his role as secret agent Ethan Hunt in the Mission: Impossible film series between 1996 and 2011. Cruise has starred in many Hollywood blockbusters, including Rain Man (1988), A Few Good Men (1992), Jerry Maguire (1996), Vanilla Sky (2001), Minority Report (2002), The Last Samurai (2003), Collateral (2004), War of the Worlds (2005), Tropic Thunder (2008) and Jack Reacher (2012). As of 2012, Cruise is Hollywood's highest-paid actor. Cruise is known for his Scientologist faith and for his support of the Church of Scientology.");
        
        return documents;
	}
	
	public static void main(String[] args) throws Exception {
		new DBpediaExperiment().run(new NamedClass("http://dbpedia.org/ontology/Person"));
	}
}
