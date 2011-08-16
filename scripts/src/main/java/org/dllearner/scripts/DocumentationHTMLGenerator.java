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
 *
 */
package org.dllearner.scripts;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;

/**
 * Script for generating documentation for all components, in particular
 * their configuration options, in HTML format. The script is based on 
 * the new (as of 2011) annotation based component design.
 * 
 * @author Jens Lehmann
 *
 */
public class DocumentationHTMLGenerator {

	private AnnComponentManager cm;
	
	public DocumentationHTMLGenerator() {
		cm = AnnComponentManager.getInstance();
	}

	public void writeConfigDocumentation(File file) {
		Map<Class<? extends Component>, String> componentNames = cm.getComponentsNamed();
		TreeMap<String, Class<? extends Component>> componentNamesInv = new TreeMap<String, Class<? extends Component>>();
		
		// create inverse, ordered map for displaying labels
		for(Entry<Class<? extends Component>, String> entry : componentNames.entrySet()) {
			componentNamesInv.put(entry.getValue(), entry.getKey());
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(getHeader());
		
		// heading
		sb.append("<h1>DL-Learner Components</h1>\n");
		
		// generate component overview
		sb.append("<ul>\n");
		for(Entry<String, Class<? extends Component>> compEntry : componentNamesInv.entrySet()) {
			sb.append("<li><a href=\"#" + compEntry.getValue() + "\">"+compEntry.getKey()+"</a></li>\n");
		}
		sb.append("</ul>\n");
		
		// generate actual documentation per component
		for(Entry<String, Class<? extends Component>> compEntry : componentNamesInv.entrySet()) {
			sb.append("<a name=\"#" + compEntry.getValue() + "\" /><h2>"+compEntry.getKey()+"</h2>\n");
		}
		
		sb.append(getFooter());
	}		
	
	private String getHeader() {
		return "<html><head><title>DL-Learner components and configuration options</title></head><body>";
	}
	
	private String getFooter() {
		return "</body></html>";
	}
	
	public static void main(String[] args) {
		File file = new File("doc/configOptions.html");
		DocumentationHTMLGenerator dg = new DocumentationHTMLGenerator();
		dg.writeConfigDocumentation(file);
		System.out.println("Done");
	}
	
}
