package org.dllearner.kb.sparql;

import java.net.URL;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

@ComponentAnn(name="efficient SPARQL fragment extractor", shortName="sparqls", version=0.1)
public class SparqlSimpleExtractor implements KnowledgeSource, OWLOntologyKnowledgeSource {

	@ConfigOption(name="endpointURL", description="URL of the SPARQL endpoint", required=true)
	private URL endpointURL = null;
	
	public SparqlSimpleExtractor() {
		
	}
	
	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public URL getEndpointURL() {
		return endpointURL;
	}

	public void setEndpointURL(URL endpointURL) {
		this.endpointURL = endpointURL;
	}

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
        //TODO Update this to return an ontology representation of what the reasoners should work with.  Build with the passed in manager instance.
        return null;
    }
}
