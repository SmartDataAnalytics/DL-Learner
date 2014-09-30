/**
 * 
 */
package org.dllearner.index;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.spell.TermFreqIterator;
import org.apache.lucene.search.spell.TermFreqIterator.TermFreqIteratorWrapper;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.InputIterator.InputIteratorWrapper;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class WordnetAnalyzerTest {

	@Test
	public void test() {
		WordnetAnalyzer analyzer = new WordnetAnalyzer("/home/me/tools/wordnet/prolog/wn_s.pl");
		
		Dataset ds1 = DatasetFactory.createMem() ;

		EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label) ;
		entDef.setAnalyzer("text", analyzer);
		// Lucene, in memory.
		Directory dir =  new RAMDirectory();
		// Join together into a dataset
		Dataset dataset = TextDatasetFactory.createLucene(ds1, dir, entDef);
		
		String query = "CONSTRUCT \n" + 
				"  { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .\n" + 
				"    ?s ?p_label ?l .}\n" + 
				"WHERE\n" + 
				"  { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type\n" + 
				"    FILTER ( ?type IN (<http://www.w3.org/2002/07/owl#Class>, <http://www.w3.org/2000/01/rdf-schema#Class>) )\n" + 
				"    OPTIONAL\n" + 
				"      { ?s ?p_label ?l\n" + 
				"        FILTER ( ?p_label IN (<http://www.w3.org/2000/01/rdf-schema#label>) )\n" + 
				"        FILTER langMatches(lang(?l), \"en\")\n" + 
				"      }\n" + 
				"  }";
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
		Model model = qef.createQueryExecution(query).execConstruct();
		
		dataset.begin(ReadWrite.WRITE);
		try {
			dataset.getDefaultModel().add(model);
			dataset.commit();
		} finally {
			dataset.end();
		}
		
		ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
				"PREFIX text: <http://jena.apache.org/text#>"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
						+ "SELECT ?s ?label { ?s text:query (rdfs:label ?l 10) ;     rdfs:label ?label}");
		queryTemplate.setLiteral("l", "aerodrome");
		
		ResultSet rs = com.hp.hpl.jena.query.QueryExecutionFactory.create(queryTemplate.asQuery(), dataset).execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
		
		
		try {
			IndexReader iReader = DirectoryReader.open(dir);
			TermsEnum iterator = iReader.leaves().get(0).reader().terms("text").iterator(null);
			InputIteratorWrapper termFreqIteratorWrapper = new InputIterator.InputIteratorWrapper(iterator);
			AnalyzingSuggester suggester = new AnalyzingSuggester(analyzer);
			suggester.build(termFreqIteratorWrapper);
			List<LookupResult> lookup = suggester.lookup("air", false, 10);
			for (LookupResult lookupResult : lookup) {
				System.out.println(lookupResult);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
