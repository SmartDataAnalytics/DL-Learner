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
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * KB files are an internal convenience format used in DL-Learner. Their
 * syntax is close to Description Logics and easy to use. KB files can be
 * exported to OWL for usage outside of DL-Learner.
 *
 * @author Jens Lehmann
 */
@ComponentAnn(name = "KB File", shortName = "kbfile", version = 0.8)
public class KBFile extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource {

    private static Logger logger = Logger.getLogger(KBFile.class);

    private KB kb;

    @ConfigOption(name = "url", description = "URL pointer to the KB file")
    private String url;
    private String baseDir;
    @ConfigOption(name = "fileName", description = "relative or absolute path to KB file")
    private String fileName;


    /**
     * Default constructor (needed for reflection in ComponentManager).
     */
    public KBFile() {
    }

    /**
     * Constructor allowing you to treat an already existing KB object
     * as a KBFile knowledge source. Use it sparingly, because the
     * standard way to create components is via
     * {@link org.dllearner.core.ComponentManager}.
     *
     * @param kb A KB object.
     */
    public KBFile(KB kb) {
        this.kb = kb;
    }

    public static Collection<org.dllearner.core.options.ConfigOption<?>> createConfigOptions() {
        Collection<org.dllearner.core.options.ConfigOption<?>> options = new LinkedList<org.dllearner.core.options.ConfigOption<?>>();
//		options.add(new StringConfigOption("filename", "pointer to the KB file on local file system",null, true, true));
        URLConfigOption urlOption = new URLConfigOption("url", "URL pointer to the KB file", null, false, true);
        urlOption.setRefersToFile(true);
        options.add(urlOption);
        return options;
    }

    public static String getName() {
        return "KB file";
    }

    @Override
    public void init() throws ComponentInitException {
        try {

            if (getUrl() != null) {
                String fileString = getUrl();
                if (fileString.startsWith("http:") || fileString.startsWith("file:")) {
                    /** Leave it as is */
                    kb = KBParser.parseKBFile(getUrl());
                } else {
                	
                	//this check is for eliminating the redundancy
                	//if the baseDir has separator at the end, do not add one more between baseDir and KB filename (or url)
                	String fullFilepath;
                	if (baseDir.endsWith("\\") || baseDir.endsWith("/"))
                		fullFilepath = baseDir + getUrl();
                	else 
                		fullFilepath = baseDir + File.separator + getUrl();
                	
                    File f = new File(new URI(fullFilepath));
                    setUrl(f.toURI().toString());
                    kb = KBParser.parseKBFile(f);
                }

                logger.trace("KB File " + getUrl() + " parsed successfully.");
            } else {
                throw new ComponentInitException("No URL option or kb object given. Cannot initialise KBFile component.");
            }

        } catch (ParseException e) {
            throw new ComponentInitException("KB file " + getUrl() + " could not be parsed correctly.", e);
        } catch (FileNotFoundException e) {
            throw new ComponentInitException("KB file " + getUrl() + " could not be found.", e);
        } catch (URISyntaxException e) {
            throw new ComponentInitException("KB file " + getUrl() + " could not be found.", e);
        }
    }

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {

        IRI ontologyURI = IRI.create("http://example.com");
        OWLOntology ontology;
        try {
            ontology = manager.createOntology(ontologyURI);
            OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);

        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        return ontology;
    }

    /*
    * (non-Javadoc)
    *
    * @see org.dllearner.core.KnowledgeSource#toDIG()
    */
    @Override
    public String toDIG(URI kbURI) {
        return DIGConverter.getDIGString(kb, kbURI).toString();
    }

    @Override
    public String toString() {
        if (kb == null)
            return "KB file (not initialised)";
        else
            return kb.toString();
    }

    @Override
    public void export(File file, org.dllearner.core.OntologyFormat format) {
        kb.export(file, format);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public KB toKB() {
        return kb;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
