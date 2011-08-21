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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.configurators.KBFileConfigurator;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGConverter;

/**
 * KB files are an internal convenience format used in DL-Learner. Their
 * syntax is close to Description Logics and easy to use. KB files can be
 * exported to OWL for usage outside of DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public class KBFile extends AbstractKnowledgeSource {

	private static Logger logger = Logger.getLogger(KBFile.class);
	
	private KB kb;
	
	private KBFileConfigurator configurator;

	/**
	 * Default constructor (needed for reflection in ComponentManager).
	 */
	public KBFile() {
		configurator = new KBFileConfigurator(this);
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
		configurator = new KBFileConfigurator(this);
		this.kb = kb;
	}
	
	@Override
	public KBFileConfigurator getConfigurator(){
		return configurator;
	}	
	
	public static String getName() {
		return "KB file";
	}
	

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
//		options.add(new StringConfigOption("filename", "pointer to the KB file on local file system",null, true, true));
		URLConfigOption urlOption = new URLConfigOption("url", "URL pointer to the KB file",null, false, true);
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
		try {
			
			// we either need a specified URL (if object is created  
			// via component manager) or the kb object has been
			// passed directly (via constructor)
			if(kb == null) {
				if(configurator.getUrl() != null) {
					kb = KBParser.parseKBFile(configurator.getUrl());
					logger.trace("KB File " + configurator.getUrl() + " parsed successfully.");
				} else {
					throw new ComponentInitException("No URL option or kb object given. Cannot initialise KBFile component.");
				}
			}
						
		} catch (IOException e) {
			throw new ComponentInitException("KB file " + configurator.getUrl() + " could not be read.", e);
		} catch (ParseException e) {
			throw new ComponentInitException("KB file " + configurator.getUrl() + " could not be parsed correctly.", e);
		}
		
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
		if(kb==null)
			return "KB file (not initialised)";
		else
			return kb.toString();
	}
	
	@Override
	public void export(File file, org.dllearner.core.OntologyFormat format){
		kb.export(file, format);
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//        URI ontologyURI = URI.create("http://example.com");
//        URI physicalURI = file.toURI();
//        SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
//        manager.addURIMapper(mapper);
//        OWLOntology ontology;
//		try {
//			ontology = manager.createOntology(ontologyURI);
//			// OWLAPIReasoner.fillOWLAPIOntology(manager,ontology,kb);
//			OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
//			manager.saveOntology(ontology);			
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		} catch (UnknownOWLOntologyException e) {
//			e.printStackTrace();
//		} catch (OWLOntologyStorageException e) {
//			e.printStackTrace();
//		}
	}
	
	public URL getURL() {
		return configurator.getUrl();
	}

	@Override
	public KB toKB() {
		return kb;
	}
	
}
