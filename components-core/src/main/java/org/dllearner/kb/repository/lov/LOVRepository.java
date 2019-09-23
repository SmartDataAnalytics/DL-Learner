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
import org.dllearner.kb.repository.SimpleRepositoryEntry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An ontology repository for
 * Linked Open Vocabularies https://lov.linkeddata.es
 *
 * @author Lorenz Buehmann
 */
public class LOVRepository implements OntologyRepository{

	private static final Logger log = Logger.getLogger(LOVRepository.class);

	private final String repositoryName = "LOV";

    private static final String ENDPOINT_URL = "https://lov.linkeddata.es/dataset/lov/sparql";

    private List<SimpleRepositoryEntry> entries;

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
        return ENDPOINT_URL;
    }

    @Override
    public void refresh() {
        fillRepository();
    }

    @Override
    public Collection<OntologyRepositoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    @Override
    public OWLOntology getOntology(OntologyRepositoryEntry entry) {
        return null;
    }

    public void dispose() {
    }

    @Override
    public InputStream getInputStream(OntologyRepositoryEntry entry) throws IOException {
        URL url = entry.getPhysicalURI().toURL();
        HttpURLConnection conn =  (HttpURLConnection) url.openConnection();

        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }

        System.out.println("Response Code ... " + status);

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");

            // get the cookie if need, for login
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connnection again
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            System.out.println("Redirect to URL : " + newUrl);

        }
        return conn.getInputStream();
    }



    private void fillRepository() {
        String query = "PREFIX vann:<http://purl.org/vocab/vann/>\n" +
                "PREFIX voaf:<http://purl.org/vocommons/voaf#>\n" +
                "SELECT DISTINCT ?vocabPrefix ?vocabURI ?title ?distribution {\n" +
                " \tGRAPH <https://lov.linkeddata.es/dataset/lov>{\n" +
                " \t \t?vocabURI a voaf:Vocabulary.\n" +
                " \t \t?vocabURI vann:preferredNamespacePrefix ?vocabPrefix.\n" +
                " ?vocabURI <http://purl.org/dc/terms/title> ?title." +
                " ?vocabURI <http://www.w3.org/ns/dcat#distribution> ?distribution. filter(!isBlank(?distribution))" +
                "}} ORDER BY ?vocabPrefix";

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(ENDPOINT_URL);
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                String uri = qs.getResource("vocabURI").getURI();
                String title = qs.getLiteral("title").getLexicalForm();
                String location = qs.getResource("distribution").getURI();
                System.out.println(location);
//                System.out.println(uri);
//                try {
//                    OWLOntology ont = man.loadOntology(IRI.create(uri));
//                    entries.add(new RepositoryEntry(URI.create(uri)));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                entries.add(new SimpleRepositoryEntry(URI.create(uri), URI.create(location), title));
            }
        }
        log.info("Loaded " + entries.size() + " ontology entries from LOV.");
    }

    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        @Override
        public IRI getDocumentIRI(IRI iri) {
            for(SimpleRepositoryEntry entry : entries) {
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
