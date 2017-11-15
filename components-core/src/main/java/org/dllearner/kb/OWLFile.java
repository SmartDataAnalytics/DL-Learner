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
package org.dllearner.kb;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.log4j.Logger;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.URLencodeUTF8;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *         <p/>
 *         SH added SPARQL capabilities.  Either URL is set directly or the basedir and filename is set or the URL and the SPARQL query is set
 */
@ComponentAnn(name = "OWL File", shortName = "owlfile", version = 0.9)
public class OWLFile extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource {

    private static Logger logger = Logger.getLogger(OWLFile.class);

    // TODO: turn this into a config option
    @ConfigOption(description = "URL pointer to the KB file or Endpoint")
    private URL url;
    @ConfigOption(description = "relative or absolute path to KB file")
    private String fileName;
    @ConfigOption(description = "separately specify directory of KB file")
    private String baseDir;

    @ConfigOption(description = "SPARQL CONSTRUCT expression to download from Endpoint")
    private String sparql = null;
    @ConfigOption(description = "a list of default graph URIs to query from the Endpoint")
    private List<String> defaultGraphURIs = new LinkedList<>();
    @ConfigOption(description = "a list of named graph URIs to query from the Endpoint")
    private List<String> namedGraphURIs = new LinkedList<>();

	@NoConfigOption // set via reasoningString
    private OntModelSpec reasoning = OntModelSpec.OWL_MEM;
    @ConfigOption(defaultValue = "false", description = "Enable JENA reasoning on the Ontology Model."
    		+ " Available reasoners are: \"micro_rule\", \"mini_rule\", \"rdfs\", \"rule\"")
    private String reasoningString = "";

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

    /* (non-Javadoc)
      * @see org.dllearner.core.Component#init()
      */
    @Override
    public void init() throws ComponentInitException {
    	setReasoning(getReasoningString());
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
            logger.debug(sb.toString());

            try {
                url = new URL(sb.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

        } else if (url == null) {
        	try {
		        Path path = Paths.get(fileName);

		        if(!path.isAbsolute() && baseDir != null) {// else relative to base directory
			        path = Paths.get(baseDir, fileName);
		        }

        		url = path.normalize().toUri().toURL();
        	} catch (MalformedURLException e) {
        		throw new RuntimeException(e);
        	}
        }
        
        initialized = true;
    }

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
        try {
	        return manager.loadOntologyFromOntologyDocument(IRI.create(getURL().toURI()));
        } catch (OWLOntologyCreationException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getURL() {
        return url;
    }

    public void setURL(URL url) {
        this.url = url;
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
    	setSparql(sparql, true);
    }
    
    public void setSparql(String sparql, boolean autoQuote) {
    	if (autoQuote) {
    		// quote IRIs
    		sparql = sparql.replaceAll("(?<=^|\\s|\\()((?:([^<(:/?#\\s]*):)(?://([^/?#]*?))?([^?#]*?)(?:\\?([^#]*?))?(?:#(.*?))?)(?=(,|\\.|;|)(\\)|\\s|$))", "<$1>");
    	}
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

    public void setReasoning(String reasoning) {
    	switch (reasoning) {
    	case "micro_rule":
    		this.reasoning = OntModelSpec.OWL_MEM_MICRO_RULE_INF;
    		break;
    	case "mini_rule":
    		this.reasoning = OntModelSpec.OWL_MEM_MINI_RULE_INF;
    		break;
    	case "true":
    	case "rdfs":
    		this.reasoning = OntModelSpec.OWL_MEM_RDFS_INF;
    		break;
    	case "rule":
    		this.reasoning = OntModelSpec.OWL_MEM_RULE_INF;
    		break;
    	case "false":
    	case "":
    		this.reasoning = OntModelSpec.OWL_MEM;
    		break;
    	default:
    		logger.warn("Unknown reasoning type: " + reasoning + ", must be one of [micro_rule, mini_rule, rdfs, rule]");
    		this.reasoning = OntModelSpec.OWL_MEM;
    	}
    }

    public void setReasoning(boolean reasoning) {
		this.setReasoning(reasoning ? "rdfs" : "");
	}

    public void setReasoning(OntModelSpec reasoning) {
    	this.reasoning = reasoning;
    }

	public OntModelSpec getReasoning() {
		return this.reasoning;
	}

	public String getReasoningString() {
		return reasoningString;
	}

	public void setReasoningString(String reasoningString) {
		this.reasoningString = reasoningString;
	}
}
