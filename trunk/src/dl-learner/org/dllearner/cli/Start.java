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
package org.dllearner.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.kb.SparqlEndpoint;
import org.dllearner.parser.ConfParser;

/**
 * Startup file for Command Line Interface.
 * 
 * @author Jens Lehmann
 *
 */
public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(args[args.length-1]);
		String baseDir = file.getParentFile().getPath();
		
		// create component manager instance
		ComponentManager cm = ComponentManager.getInstance();
		
		// parse conf file
		ConfParser parser = ConfParser.parseFile(file);
		
//		try {
		
		// step 1: detect knowledge sources
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = getImportedFiles(parser, baseDir);
		for(Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles.entrySet()) {
			KnowledgeSource ks = cm.knowledgeSource(entry.getValue());
			// ConfigOption<?> urlOption = cm.getConfigOption(entry.getValue(), "url");
			// cm.applyConfigEntry(ks, new ConfigEntry<String>(urlOption, entry.getValue()));
			cm.applyConfigEntry(ks, "url", entry.getKey().toString());

			// TODO: SPARQL-specific settings
			
			// ks.applyConfigEntry(new ConfigEntry)
				
		}
		
		// use parsed values to configure components
		
//		} catch (InvalidConfigOptionValueException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private static Map<URL, Class<? extends KnowledgeSource>> getImportedFiles(ConfParser parser, String baseDir) {
		List<List<String>> imports = parser.getFunctionCalls().get("import");
		Map<URL, Class<? extends KnowledgeSource>> importedFiles = new HashMap<URL, Class<? extends KnowledgeSource>>();
		
		for(List<String> arguments : imports) {
			// step 1: detect URL
			URL url = null;
			try {				
				String fileString = arguments.get(0);
				if(fileString.startsWith("http:")) {
					url = new URL(fileString);
				} else {
					File f = new File(baseDir, arguments.get(0));
					url = f.toURI().toURL();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			// step 2: detect format
			Class<? extends KnowledgeSource> ksClass;
			if (arguments.size() == 1) {
				String filename = url.getPath();
				String ending = filename.substring(filename.lastIndexOf("."));
				
				if(ending.equals("rdf") || ending.equals("owl"))
					ksClass = OWLFile.class;
				else if(ending.equals("nt"))
					ksClass = OWLFile.class;
				else if(ending.equals("kb"))
					ksClass = KBFile.class;
				else {
					System.err.println("Warning: no format fiven for " + arguments.get(0) + " and could not detect it. Chosing RDF/XML.");
					ksClass = OWLFile.class;
				}
				
				importedFiles.put(url, ksClass);
			} else {
				String formatString = arguments.get(1);
				
				if (formatString.equals("RDF/XML"))
					ksClass = OWLFile.class;
				else if(formatString.equals("KB"))
					ksClass = KBFile.class;
				else if(formatString.equals("SPARQL"))
					ksClass = SparqlEndpoint.class;
				else if(formatString.equals("NT"))
					ksClass = OWLFile.class;
				else {
					throw new RuntimeException("Unsupported knowledge source format " + formatString + ". Exiting.");
				}
					
				importedFiles.put(url, ksClass);
			}
		}
		
		return importedFiles;
	}
	
}
