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
package org.dllearner.kb.sparql.simple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SchemaIndexer {
    private static Logger log = LoggerFactory.getLogger(SchemaIndexer.class);
    private OntModel model;

    //static instantiation
    private static ClassIndexer classIndexer = new ClassIndexer();
    //remember ontologies
    private static Set<String> alreadyIndexed = new HashSet<>();
    //set or list of urls for the ontologies
    private List<String> ontologySchemaUrls;

    public SchemaIndexer() {
    }

    public synchronized void init() {

        for (String url : ontologySchemaUrls) {
            log.info("Testing, if indexed: " + url);
            if (alreadyIndexed.add(url)) {
                log.info("Ontology not found, start indexing");
                try {
                    Monitor m0 = MonitorFactory.start("Indexer parsing ontology");
                    model = ModelFactory.createOntologyModel();
                    model.read(url, null);
                    classIndexer.index(model);
                    m0.stop();
                    log.info("indexed ontology in ms: " + m0.getTotal());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                //not so important output
                log.debug("Already indexed: " + url + " " + alreadyIndexed);
            }
        }

    }

    public OntModel getHierarchyForURI(String classUri) {
        if (classIndexer == null) {
            this.init();
        }
        return classIndexer.getHierarchyForClassURI(classUri);
    }

    public static void main(String... args) {
        SchemaIndexer i = new SchemaIndexer();
        System.out.println(i.getHierarchyForURI("http://dbpedia.org/ontology/Software"));
    }

    public void setOntologySchemaUrls(List<String> ontologySchemaUrls) {
        this.ontologySchemaUrls = ontologySchemaUrls;
    }
}