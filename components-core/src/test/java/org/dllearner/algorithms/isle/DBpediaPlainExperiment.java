/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.syntactic.SolrSyntacticIndex;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaPlainExperiment extends Experiment{
	
	final SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	final int maxNrOfInstancesPerClass = 10;
	static final String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
	static final String searchField = "comment";
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getIndex()
	 */
	@Override
	protected Index getIndex() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getOntology()
	 */
	@Override
	protected OWLOntology getOntology() {
		//load the DBpedia schema
		OWLOntology schema = null;
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			 CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			 schema = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
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
				Sets.newHashSet(classToDescribe),
				maxNrOfInstancesPerClass);
		cleanUpModel(sample);
		filter(sample, "http://dbpedia.org/ontology/");
		OWLEntityTypeAdder.addEntityTypes(sample);
//		StmtIterator iterator = sample.listStatements();
//		while(iterator.hasNext()){
//			System.out.println(iterator.next());
//		}
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			sample.write(baos, "TURTLE", null);
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
			man.addAxioms(ontology, schema.getAxioms());
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
			man.removeAxioms(ontology, ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE));
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Filter triples which are not relevant based on the given knowledge base
	 * namespace.
	 *
	 * @param model
	 * @param namespace
	 */
	private void filter(Model model, String namespace) {
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
			Statement st = iter.next();
			Property predicate = st.getPredicate();
			if (predicate.equals(RDF.type)) {
				if (!st.getObject().asResource().getURI().startsWith(namespace)) {
					statementsToRemove.add(st);
				} else if (st.getObject().equals(OWL.FunctionalProperty.asNode())) {
					statementsToRemove.add(st);
				} else if (st.getObject().isLiteral() && st.getObject().asLiteral().getDatatypeURI().equals(XSD.gYear.getURI())) {
					statementsToRemove.add(st);
				}
			} else if (!predicate.equals(RDFS.subClassOf) && !predicate.equals(OWL.sameAs) && !predicate.asResource().getURI().startsWith(namespace)) {
				statementsToRemove.add(st);
			}
		}
		model.remove(statementsToRemove);
	}
	
	private static void cleanUpModel(Model model) {
		// filter out triples with String literals, as therein often occur
		// some syntax errors and they are not relevant for learning
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		for (Iterator<Statement> iter = model.listStatements().toList().iterator(); iter.hasNext();) {
			Statement st = iter.next();
			RDFNode object = st.getObject();
			if (object.isLiteral()) {
				// statementsToRemove.add(st);
				Literal lit = object.asLiteral();
				if (lit.getDatatype() == null || lit.getDatatype().equals(XSD.xstring)) {
					st.changeObject("shortened", "en");
				} else if (lit.getDatatype().getURI().equals(XSD.gYear.getURI())) {
					statementsToRemove.add(st);
					//                    System.err.println("REMOVE " + st);
				} else if (lit.getDatatype().getURI().equals(XSD.gYearMonth.getURI())) {
					statementsToRemove.add(st);
//					                    System.err.println("REMOVE " + st);
				}
			}
			//remove statements like <x a owl:Class>
			if (st.getPredicate().equals(RDF.type)) {
				if (object.equals(RDFS.Class.asNode()) || object.equals(OWL.Class.asNode()) || object.equals(RDFS.Literal.asNode())
						|| object.equals(RDFS.Resource)) {
					statementsToRemove.add(st);
				}
			}

			//remove unwanted properties
			String dbo = "http://dbpedia.org/ontology/";
			Set<String> blackList = Sets.newHashSet(dbo + "wikiPageDisambiguates",dbo + "wikiPageExternalLink",
					dbo + "wikiPageID", dbo + "wikiPageInterLanguageLink", dbo + "wikiPageRedirects", dbo + "wikiPageRevisionID",
					dbo + "wikiPageWikiLink");
			for(String bl: blackList){
				if (st.getPredicate().getURI().equals(bl)) {
					statementsToRemove.add(st);
				}
			}
		}

		model.remove(statementsToRemove);
	}
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getDocuments()
	 */
	@Override
	protected Set<String> getDocuments() {
		Set<String> documents = new HashSet<String>();
        return documents;
	}
	
	public static void main(String[] args) throws Exception {
		new DBpediaPlainExperiment().run(new NamedClass("http://dbpedia.org/ontology/SpaceShuttle"));
	}
}
