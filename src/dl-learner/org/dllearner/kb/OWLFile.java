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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.reasoning.OWLAPIDIGConverter;

/**
 * @author Jens Lehmann
 * 
 */
public class OWLFile extends KnowledgeSource {

	private URL url;

	public static String getName() {
		return "OWL file";
	}
	
	

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("url", "URL pointing to the OWL file"));
		return options;
	}

	/*
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		if (entry.getOptionName().equals("url")) {
			String s = (String) entry.getValue();
			try {
				url = new URL(s);
				// File f = new File(url.toURI());
				//if(!f.canRead())
				//	throw new InvalidConfigOptionValueException(entry.getOption(), entry.getValue());
			} catch (MalformedURLException e) {
				throw new InvalidConfigOptionValueException(entry.getOption(), entry.getValue(),"malformed URL " + s);
			} //catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
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

}
