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
package org.dllearner.gui;

import java.util.List;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.kb.OWLFile;

/**
 * @author Jens Lehmann
 *
 */
public class ComponentRetrievalTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// example 1: getting all components of a given type
		ComponentManager cm = ComponentManager.getInstance();
		List<Class<? extends KnowledgeSource>> sources = cm.getKnowledgeSources();
		cm.knowledgeSource(sources.get(0));
		System.out.println(sources.get(1).toString());
		
		// example 2: setting a config value using a ConfigEntry object
		KnowledgeSource owlFile = cm.knowledgeSource(OWLFile.class);
		StringConfigOption urlOption = (StringConfigOption) cm.getConfigOption(OWLFile.class, "url");
		ConfigEntry<String> urlEntry;
		try {
			urlEntry = new ConfigEntry<String>(urlOption, "http://example.com");
			cm.applyConfigEntry(owlFile, urlEntry);
		} catch (InvalidConfigOptionValueException e) {
			e.printStackTrace();
		}
		
	}

}
