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

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.dl.KB;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGConverter;

/**
 * @author Jens Lehmann
 *
 */
public class KBFile extends KnowledgeSource {

	// private File file;
	private URL url;
	private KB kb;

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
	public void init() {
		try {
			kb = KBParser.parseKBFile(url);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
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
	
	public URL getURL() {
		return url;
	}
	
}
