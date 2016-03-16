/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.repository.tones;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

public class TONESRepository implements OntologyRepository{
	
	private static final Logger log = Logger.getLogger(TONESRepository.class);
	
	private final String repositoryName = "TONES";

    private final URI repositoryLocation = URI.create("http://owl.cs.manchester.ac.uk/repository");

    private List<RepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public TONESRepository() {
        entries = new ArrayList<>();
        iriMapper = new RepositoryIRIMapper();
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

    public void dispose() {
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Implementation details

    private void fillRepository() {
        try {
            entries.clear();
            URI listURI = URI.create(repositoryLocation + "/list");
            BufferedReader br = new BufferedReader(new InputStreamReader(listURI.toURL().openStream()));
            String line;
            while((line = br.readLine()) != null) {
                try {
                    entries.add(new RepositoryEntry(new URI(line)));
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Loaded " + entries.size() + " ontology entries from TONES.");
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        public RepositoryEntry(URI ontologyIRI) {
            this.ontologyURI = ontologyIRI;
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(IRI.create(ontologyIRI));
            physicalURI = URI.create(repositoryLocation + "/download?ontology=" + ontologyIRI);
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

    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        @Override
        public IRI getDocumentIRI(IRI iri) {
            for(RepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }

}
