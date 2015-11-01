package org.dllearner.kb.repository.oxford;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

public class OxfordRepository implements OntologyRepository{
	
	private static final Logger log = Logger.getLogger(OxfordRepository.class);
	
	private final String repositoryName = "Oxford";

    private final URI repositoryLocation = URI.create("http://www.cs.ox.ac.uk/isg/ontologies/UID/");

    private List<RepositoryEntry> entries;

    int numberOfEntries = 797;
    
    DecimalFormat df = new DecimalFormat("00000");


    public OxfordRepository() {
        entries = new ArrayList<>();
    }

    @Override
    public void initialize() {
    	refresh();
    }


    @Override
	public String getName() {
        return repositoryName;
    }


    @Override
	public String getLocation() {
        return repositoryLocation.toString();
    }


    @Override
	public void refresh() {
        fillRepository();
    }


    @Override
	public Collection<OntologyRepositoryEntry> getEntries() {
        List<OntologyRepositoryEntry> ret = new ArrayList<>();
        ret.addAll(entries);
        return ret;
    }


    @Override
	public List<Object> getMetaDataKeys() {
        return Collections.emptyList();
    }


    public void dispose() throws Exception {
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Implementation details


    private void fillRepository() {
    	entries.clear();
        for(int i = 1; i <= numberOfEntries; i++){
        	entries.add(new RepositoryEntry(URI.create(repositoryLocation + df.format(i) + ".owl")));
        }
        log.info("Loaded " + entries.size() + " ontology entries from Oxford.");
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        public RepositoryEntry(URI ontologyIRI) {
            this.ontologyURI = ontologyIRI;
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(IRI.create(ontologyIRI));
            physicalURI = ontologyIRI;
        }


        @Override
		public String getOntologyShortName() {
            return shortName;
        }


        @Override
		public URI getOntologyURI() {
            return ontologyURI;
        }


        @Override
		public URI getPhysicalURI() {
            return physicalURI;
        }


        @Override
		public String getMetaData(Object key) {
            return null;
        }

    }
    
    public static void main(String[] args) throws Exception {
		new OxfordRepository().fillRepository();
	}

}
