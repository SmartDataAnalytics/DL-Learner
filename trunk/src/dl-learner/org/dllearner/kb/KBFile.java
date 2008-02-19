/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.kb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.reasoning.OWLAPIAxiomConvertVisitor;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;

/**
 * KB files are an internal convenience format used in DL-Learner. Their
 * syntax is close to Description Logics and easy to use. KB files can be
 * exported to OWL for usage outside of DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public class KBFile extends KnowledgeSource {

	// private File file;
	private URL url;
	private KB kb;

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
	
	public static String getName() {
		return "KB file";
	}

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("filename", "pointer to the KB file on local file system"));
		options.add(new StringConfigOption("url", "URL pointer to the KB file"));
		return options;
	}

	/*
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String option = entry.getOptionName();
		if (option.equals("filename")) {
			// file = new File((String)entry.getValue());
			try {
				url = new File((String)entry.getValue()).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new InvalidConfigOptionValueException(entry.getOption(),entry.getValue());
			}
		} else if(option.equals("url")) {
			try {
				url = new URL((String)entry.getValue());
			} catch (MalformedURLException e) {
				throw new InvalidConfigOptionValueException(entry.getOption(),entry.getValue());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		try {
			if(url != null)
				kb = KBParser.parseKBFile(url);
		} catch (IOException e) {
			throw new ComponentInitException("KB file " + url + " could not be read.", e);
		} catch (ParseException e) {
			throw new ComponentInitException("KB file " + url + " could not be parsed correctly.", e);
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
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        URI ontologyURI = URI.create("http://example.com");
        URI physicalURI = file.toURI();
        SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
        manager.addURIMapper(mapper);
        OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyURI);
			// OWLAPIReasoner.fillOWLAPIOntology(manager,ontology,kb);
			OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
			manager.saveOntology(ontology);			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
//		Reasoner kaon2Reasoner = KAON2Reasoner.getKAON2Reasoner(kb);
//		
//		String kaon2Format = null;
//		if(format.equals(org.dllearner.core.OntologyFormat.RDF_XML))
//			kaon2Format = OntologyFileFormat.OWL_RDF;
//		else {
//			System.err.println("Warning: Cannot export format " + format + ". Exiting.");
//			System.exit(0);
//		}
//		
//		try {
//			kaon2Reasoner.getOntology().saveOntology(kaon2Format,file,"ISO-8859-1");
//		} catch (KAON2Exception e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}		
	}
	
	public URL getURL() {
		return url;
	}

	@Override
	public KB toKB() {
		return kb;
	}
	
}
