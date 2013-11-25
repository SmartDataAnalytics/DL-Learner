/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.dllearner.algorithms.isle.index.RemoteDataProvider;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Lorenz Buehmann
 *
 */
public class SemanticBibleExperiment extends Experiment{

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getOntology()
	 */
	@Override
	protected OWLOntology getOntology() {
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology schema = man.loadOntology(IRI.create("http://www.semanticbible.com/2006/11/NTNames.owl"));
			OWLOntology instances = OWLManager.createOWLOntologyManager().loadOntology(IRI.create("http://www.semanticbible.com/2006/11/NTN-individuals.owl"));
			OWLOntology mergedOntology = man.createOntology(IRI.create("http://semanticbible.com/merged.owl"));
			man.addAxioms(mergedOntology, schema.getAxioms());
			man.addAxioms(mergedOntology, instances.getAxioms());
//			Model model = ModelFactory.createDefaultModel();
//			model.read("http://www.semanticbible.com/2006/11/NTNames.owl");
//			model.read("http://www.semanticbible.com/2006/11/NTN-individuals.owl");
//			QueryExecutionFactoryModel qef = new QueryExecutionFactoryModel(model);
//			System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT ?s ?p WHERE {?s a <http://semanticbible.org/ns/2006/NTNames#Woman>. ?s ?p ?o.}").execSelect()));
			return mergedOntology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.Experiment#getDocuments()
	 */
	@Override
	protected Set<TextDocument> getDocuments() {
		Set<TextDocument> documents = new HashSet<TextDocument>();
		File taggedFolder = new File("tmp/tagged");
		taggedFolder.mkdirs();
        try {
			RemoteDataProvider bibleByChapter = new RemoteDataProvider(
			        new URL("http://gold.linkeddata.org/data/bible/split_by_chapter.zip"));
			File folder = bibleByChapter.getLocalDirectory();
			for (File file  : folder.listFiles()) {
			    if(!file.isDirectory() && !file.isHidden()){
			        try {
			            String text = Files.toString(file, Charsets.UTF_8);
//			            String posTagged = getPOSTaggedText(text);
//			            Files.write(posTagged, new File(taggedFolder, file.getName() + ".tagged"), Charsets.UTF_8);
			            documents.add(TextDocumentGenerator.getInstance().generateDocument(text));
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
			    }
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        documents.clear();
        TextDocument doc = TextDocumentGenerator.getInstance().generateDocument("and in that day seven women shall take hold of one man saying we will eat our own bread and wear our own apparel only let us be called by thy name to take away our reproach in that day shall the branch of the lord be beautiful and glorious and the fruit of the earth excellent and comely for them that are escaped of israel and it shall come to pass left in zion and remaineth in jerusalem shall be called holy every one that is written among the living in jerusalem when the lord shall have washed away the filth of the daughters of zion and shall have purged the blood of jerusalem from the midst thereof by the spirit of judgment and by the spirit of burning and the lord will create upon every dwelling place of mount zion and upon her assemblies a cloud and smoke by day and the shining of a flaming fire by night for upon all the glory a defence and there shall be a tabernacle for a shadow in the daytime from the heat and for a place of refuge and for a covert from storm and from rain");
        documents.add(doc);
        return documents;
	}
	
	public static void main(String[] args) throws Exception {
		new SemanticBibleExperiment().run(new NamedClass("http://semanticbible.org/ns/2006/NTNames#Woman"));
	}
}
