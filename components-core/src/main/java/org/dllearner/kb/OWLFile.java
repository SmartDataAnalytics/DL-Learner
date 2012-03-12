/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.kb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.reasoning.OWLAPIDIGConverter;
import org.dllearner.utilities.URLencodeUTF8;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *         <p/>
 *         SH: I added SPARQL capabilities.  Either URL is set directly or the basedir and filename is set or the URL and the SPARQL query is set
 */
@ComponentAnn(name = "OWL File", shortName = "owlfile", version = 0.9)
public class OWLFile extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource {

    private static Logger logger = Logger.getLogger(OWLFile.class);

    // TODO: turn this into a config option
    private URL url;
    private String fileName;
    private String baseDir;

    private String sparql = null;
    private List<String> defaultGraphURIs = new LinkedList<String>();
    private List<String> namedGraphURIs = new LinkedList<String>();

    public static String getName() {
        return "OWL file";
    }

    public OWLFile() {

    }

    public OWLFile(URL url) {
        this.url = url;
    }

    public OWLFile(String filename) {
        try {
            url = new File(filename).toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static Collection<ConfigOption<?>> createConfigOptions() {
        Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
        URLConfigOption urlOption = new URLConfigOption("url", "URL pointing to the OWL file", null, true, true);
        urlOption.setRefersToFile(true);
        options.add(urlOption);
        return options;
    }

    /*
      * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
      */
    @Override
    public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {

    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Component#init()
      */
    @Override
    public void init() throws ComponentInitException {
        if (sparql != null) {
            StringBuilder sb = new StringBuilder();

            //make URL
            sb.append(url.toString());
            sb.append("?query=").append(URLencodeUTF8.encode(sparql));
            sb.append("&format=application%2Frdf%2Bxml");

            for (String graph : defaultGraphURIs) {
                sb.append("&default-graph-uri=").append(URLencodeUTF8.encode(graph));
            }
            for (String graph : namedGraphURIs) {
                sb.append("&named-graph-uri=").append(URLencodeUTF8.encode(graph));
            }
            logger.info(sb.toString());

            try {
                url = new URL(sb.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

        } else if (url == null) {
            try {
//              url = new URL("file://" + baseDir + "/" + fileName);
          	 url = new URL(baseDir + "/" + fileName);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {

        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(getURL().toURI()));
            return ontology;
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.dllearner.core.KnowledgeSource#toDIG()
    */
    @Override
    public String toDIG(URI kbURI) {
        // TODO: need some handling for cases where the URL was not set
        return OWLAPIDIGConverter.getTellsString(url, OntologyFormat.RDF_XML, kbURI);
    }

    public URL getURL() {
        return url;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.KnowledgeSource#export(java.io.File, org.dllearner.core.OntologyFormat)
      */
    @Override
    public void export(File file, OntologyFormat format) throws OntologyFormatUnsupportedException {
        // currently no export functions implemented, so we just throw an exception
        throw new OntologyFormatUnsupportedException("export", format);
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.KnowledgeSource#toKB()
      */
    @Override
    public KB toKB() {
        throw new Error("OWL -> KB conversion not implemented yet.");
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    public List<String> getDefaultGraphURIs() {
        return defaultGraphURIs;
    }

    public void setDefaultGraphURIs(List<String> defaultGraphURIs) {
        this.defaultGraphURIs = defaultGraphURIs;
    }

    public List<String> getNamedGraphURIs() {
        return namedGraphURIs;
    }

    public void setNamedGraphURIs(List<String> namedGraphURIs) {
        this.namedGraphURIs = namedGraphURIs;
    }
}
