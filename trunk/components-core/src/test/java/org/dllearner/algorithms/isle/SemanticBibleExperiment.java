/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.index.RemoteDataProvider;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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
	protected Set<String> getDocuments() {
		Set<String> documents = new HashSet<String>();
        try {
			RemoteDataProvider bibleByChapter = new RemoteDataProvider(
			        new URL("http://gold.linkeddata.org/data/bible/split_by_chapter.zip"));
			File folder = bibleByChapter.getLocalDirectory();
			for (File file  : folder.listFiles()) {
			    if(!file.isDirectory() && !file.isHidden()){
			        try {
			            String text = Files.toString(file, Charsets.UTF_8);
			            text = text.trim();
			            if(!text.isEmpty()){
			            	documents.add(text);
			            }
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
//        documents.clear();
//        documents.add("and in that day seven women shall take hold of one man saying we will eat our own bread and wear our own apparel only let us be called by thy name to take away our reproach in that day shall the branch of the lord be beautiful and glorious and the fruit of the earth excellent and comely for them that are escaped of israel and it shall come to pass left in zion and remaineth in jerusalem shall be called holy every one that is written among the living in jerusalem when the lord shall have washed away the filth of the daughters of zion and shall have purged the blood of jerusalem from the midst thereof by the spirit of judgment and by the spirit of burning and the lord will create upon every dwelling place of mount zion and upon her assemblies a cloud and smoke by day and the shining of a flaming fire by night for upon all the glory a defence and there shall be a tabernacle for a shadow in the daytime from the heat and for a place of refuge and for a covert from storm and from rain");
        return documents;
	}
	
	public static void main(String[] args) throws Exception {
		new SemanticBibleExperiment().run(new NamedClass("http://semanticbible.org/ns/2006/NTNames#Woman"));
	}
}
