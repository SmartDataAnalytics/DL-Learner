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
package org.dllearner.kb.repository;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * An ontology repository representing and hosting a set of ontologies.
 *
 * @author Lorenz Buehmann
 */
public interface OntologyRepository {

    Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/**
     * Gets the name of the repository
     * @return A short name for the repository
     */
    String getName();

    /**
     * Gets a description of the location of the repository
     * @return A human readable description of the repository location
     */
    String getLocation();

    /**
     * Ensures the repository is up to date
     */
    void refresh();

    /**
     * Initialize the repository
     */
    void initialize();

    /**
     * @return all entries in the repository
     */
    Collection<OntologyRepositoryEntry> getEntries();
    
    /**
     * Returns the ontology for the repository entry, i.e. the file also parsed.
     * @param entry
     * @return
     */
    default OWLOntology getOntology(OntologyRepositoryEntry entry) throws OWLOntologyCreationException{
        try(InputStream is = getInputStream(entry)) {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            man.addMissingImportListener(e -> {
                log.warn("Missing import: " + e.getImportedOntologyURI());
            });

            // handle missing imports
            OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration();
            conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

            // the List ontology isn't online anymore, thus, we ignore it'S import
            conf.addIgnoredImport(IRI.create("http://www.co-ode.org/ontologies/lists/2008/09/11/list.owl"));
            man.setOntologyLoaderConfiguration(conf);

            return man.loadOntologyFromOntologyDocument(is);
        } catch (Exception e) {
            throw new OWLOntologyCreationException("Failed to load ontology from " + entry.getPhysicalURI(), e);
        }
    }

    /**
     * Returns an input stream for the repository entry, i.e. the file isn't parsed.
     * @param entry
     * @return
     */
    default InputStream getInputStream(OntologyRepositoryEntry entry) throws IOException {
        URL url = entry.getPhysicalURI().toURL();
        return url.openStream();
    }

}
