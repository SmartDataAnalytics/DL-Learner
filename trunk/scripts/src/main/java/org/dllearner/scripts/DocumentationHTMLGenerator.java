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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.Files;

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
		
		// filter interface
		sb.append("<p>Filter components by implemented interfaces:</p>\n<ul>");
		sb.append("<a href=\"#\" onClick=\"showAllCat()\">show all</a>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('KnowledgeSource')\">KnowledgeSource</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('ReasonerComponent')\">ReasonerComponent</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('LearningProblem')\">LearningProblem</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('LearningAlgorithm')\">LearningAlgorithm</a>");
		sb.append("<ul><li><a href=\"#\" onClick=\"showOnlyCat('AxiomLearningAlgorithm')\">AxiomLearningAlgorithm</a></li></ul></li>");
		sb.append("</ul>");
		
		// general explanations
		sb.append("<p>Click on a component to get an overview on its configuration options.</p>");
		
		// generate component overview
		sb.append("<ul>\n");
		for(Entry<String, Class<? extends Component>> compEntry : componentNamesInv.entrySet()) {
			sb.append("<div class=\"" + getCoreTypes(compEntry.getValue()) + "\"><li><a href=\"#" + compEntry.getValue().getName() + "\">"+compEntry.getKey()+"</a></li></div>\n");
		}
		sb.append("</ul>\n");
		
		// generate actual documentation per component
		for(Entry<String, Class<? extends Component>> compEntry : componentNamesInv.entrySet()) {
			Class<? extends Component> comp = compEntry.getValue();
			sb.append("<div class=\"" + getCoreTypes(comp) + "\">");
			// heading + anchor
			sb.append("<a name=\"" + comp.getName() + "\"><h2>"+compEntry.getKey()+"</h2></a>\n");
			// some information about the component
			sb.append("<p>short name: " + AnnComponentManager.getShortName(comp) + "<br />");
			sb.append("version: " + AnnComponentManager.getVersion(comp) + "<br />");
			sb.append("implements: " + getCoreTypes(comp).replace(" ", ", ") + "<br />");
			String description = AnnComponentManager.getDescription(comp);
			if(description.length() > 0) {
				sb.append("description: " + AnnComponentManager.getDescription(comp) + "<br />");
			}
			sb.append("</p>");
			
			// generate table for configuration options
			List<ConfigOption> options = ConfigHelper.getConfigOptions(comp);
			if(options.isEmpty()) {
				sb.append("This component does not have configuration options.");
			} else {
			sb.append("<table id=\"hor-minimalist-a\"><thead><tr><th>option name</th><th>description</th><th>type</th><th>default value</th><th>required?</th></tr></thead><tbody>\n");
			for(ConfigOption option : options) {
				sb.append("<tr><td>" + option.name() + "</td><td>" + option.description() + "</td><td> " + getOptionType(option) + "</td><td>" + option.defaultValue() + "</td><td> " + option.required() + "</td></tr>\n");
			}
			sb.append("</tbody></table>\n");
			}
			sb.append("</div>\n");
		}
		
		sb.append(getFooter());
		
		Files.createFile(file, sb.toString());
	}		
	
	private String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><title>DL-Learner components and configuration options</title>\n");
		sb.append("<style type=\"text/css\">\n");
		sb.append("body { line-height: 1.6em; font-size: 15px; font-family: \"Lucida Sans Unicode\", \"Lucida Grande\", Sans-Serif;  }\n");
		sb.append("#hor-minimalist-a 	{ font-size: 13px;	background: #fff; margin: 30px;	width: 90%;border-collapse: collapse; 	text-align: left; } \n");
		sb.append("#hor-minimalist-a th { font-size: 15px;	font-weight: normal; color: #039; padding: 10px 8px; border-bottom: 2px solid #6678b1;	}\n");
		sb.append("#hor-minimalist-a td	{ color: #669;padding: 9px 8px 0px 8px;	}\n");
		sb.append("#hor-minimalist-a tbody tr:hover td 	{ color: #009; }\n");
		sb.append("</style>\n");
		sb.append("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js\"></script>");
		sb.append("<script type=\"text/javascript\" language=\"javascript\">\n");
		sb.append("//<![CDATA[\n"); 
		sb.append("function showOnlyCat(className){\n");
		sb.append("	 $('div').show(); $('div').not('.'+className).hide(); }\n");
		sb.append("function showAllCat(){\n");
		sb.append("  $('div').show() };\n");
		sb.append("//]]>\n");
		sb.append("</script>\n"); 
		sb.append("</head><body>\n");
		return sb.toString();
	}
	
	private String getFooter() {
		return "</body></html>";
	}
	
	// this is a hack, because we just assume that every PropertyEditor is named 
	// as TypeEditor (e.g. ObjectPropertyEditor); however that hack does not too much harm here
	private static String getOptionType(ConfigOption option) {
		String name = option.propertyEditorClass().getSimpleName();
		return name.substring(0, name.length()-6);
	}	
	
	private static String getCoreTypes(Class<? extends Component> comp) {
		List<Class<? extends Component>> types = AnnComponentManager.getCoreComponentTypes(comp);
		String str = "";
		for(Class<?extends Component> type : types) {
			str += " " + type.getSimpleName();
		}
		return str.substring(1);
	}
		
	public static void main(String[] args) {
		File file = new File("../interfaces/doc/configOptions.html");
		DocumentationHTMLGenerator dg = new DocumentationHTMLGenerator();
		dg.writeConfigDocumentation(file);
		System.out.println("Done");
	}
	
}
