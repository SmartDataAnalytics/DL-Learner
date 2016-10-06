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
package org.dllearner.kb.repository.lov;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LOVRepository implements OntologyRepository{

	private static final Logger log = Logger.getLogger(LOVRepository.class);

	private final String repositoryName = "LOV";

    String endpointURL = "http://lov.okfn.org/dataset/lov/sparql";

    private List<RepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public LOVRepository() {
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
        return endpointURL;
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
        String query = "PREFIX vann:<http://purl.org/vocab/vann/>\n" +
                "PREFIX voaf:<http://purl.org/vocommons/voaf#>\n" +
                " \n" +
                "### Vocabularies contained in LOV and their prefix\n" +
                "SELECT DISTINCT ?vocabPrefix ?vocabURI {\n" +
                " \tGRAPH <http://lov.okfn.org/dataset/lov>{\n" +
                " \t \t?vocabURI a voaf:Vocabulary.\n" +
                " \t \t?vocabURI vann:preferredNamespacePrefix ?vocabPrefix.\n" +
                "}} ORDER BY ?vocabPrefix";

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpointURL);
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                String uri = qs.getResource("vocabURI").getURI();
                System.out.println(uri);
                try {
                    OWLOntology ont = man.loadOntology(IRI.create(uri));
                    entries.add(new RepositoryEntry(URI.create(uri)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("Loaded " + entries.size() + " ontology entries from LOV.");
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        public RepositoryEntry(URI ontologyIRI) {
            this.ontologyURI = ontologyIRI;
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(IRI.create(ontologyIRI));
            physicalURI = URI.create(getLocation() + "/download?ontology=" + ontologyIRI);
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

    public static void main(String[] args) throws Exception {
        new LOVRepository().initialize();
    }

}
