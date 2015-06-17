package org.dllearner.cli;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections15.BidiMap;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.Files;
import org.semanticweb.owlapi.model.OWLClass;

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
		
		Map<Class<?>, String> componentNames = new DualHashBidiMap();
		componentNames.putAll(cm.getComponentsNamed());
		componentNames.put(CLI.class, "Command Line Interface");
		TreeMap<String, Class<?>> componentNamesInv = new TreeMap<String, Class<?>>();
		
		// create inverse, ordered map for displaying labels
		for(Entry<Class<?>, String> entry : componentNames.entrySet()) {
			componentNamesInv.put(entry.getValue(), entry.getKey());
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(getHeader());
		
		// heading
		sb.append("<h1>DL-Learner Components</h1>\n");
		
		// filter interface
		sb.append("<p>Click on the following items to filter the listing below by implemented interfaces (requires Javascript):</p>\n");
		sb.append("<a href=\"#\" onClick=\"showAllCat()\">show all</a><ul class=\"list-unstyled\">");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('Class')\">Non-component Class</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('KnowledgeSource')\">KnowledgeSource</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('ReasonerComponent')\">ReasonerComponent</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('LearningProblem')\">LearningProblem</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('LearningAlgorithm')\">LearningAlgorithm</a>");
		sb.append("<ul><li><a href=\"#\" onClick=\"showOnlyCat('AxiomLearningAlgorithm')\">AxiomLearningAlgorithm</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('ClassExpressionLearningAlgorithm')\">ClassExpressionLearningAlgorithm</a></li></ul></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('RefinementOperator')\">RefinementOperator</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('Heuristic')\">Heuristic</a></li>");
		sb.append("<li><a href=\"#\" onClick=\"showOnlyCat('OtherComponent')\">other</a></li>");
		sb.append("</ul>");
		
		// general explanations
		sb.append("<p>Click on a component to get an overview on its configuration options.</p>");
		
		// generate component overview
		sb.append("<ul>\n");
		for(Entry<String, Class<?>> compEntry : componentNamesInv.entrySet()) {
			sb.append("<div class=\"type menu " + getCoreTypes(compEntry.getValue()) + "\"><li><a href=\"#" + compEntry.getValue().getName() + "\">"+compEntry.getKey()+"</a></li></div>\n");
		}
		sb.append("</ul>\n");
		
		// generate actual documentation per component
		for(Entry<String, Class<?>> compEntry : componentNamesInv.entrySet()) {
			Class<?> comp = compEntry.getValue();
			sb.append("<div class=\"type " + getCoreTypes(comp) + "\">");
			// heading + anchor
			sb.append("<a name=\"" + comp.getName() + "\"><h2>"+compEntry.getKey()+"</h2></a>\n");
			// some information about the component
			if (Component.class.isAssignableFrom(comp)) {
				Class<? extends Component> ccomp = (Class<? extends Component>) comp;
			
				sb.append("<dl class=\"dl-horizontal\"><dt>short name</dt><dd>" + AnnComponentManager.getShortName(ccomp) + "</dd>");
				sb.append("<dt>version</dt><dd>" + AnnComponentManager.getVersion(ccomp) + "</dd>");
				sb.append("<dt>implements</dt><dd><ul class=\"list-inline\"><li>" + getCoreTypes(comp).replace(" ", "</li><li>") + "</li></ul></dd>");
				String description = AnnComponentManager.getDescription(ccomp);
				if(description.length() > 0) {
					sb.append("<dt>description</dt><dd>" + AnnComponentManager.getDescription(ccomp) + "</dd>");
				}
				sb.append("</dl>");
			}
			optionsTable(sb, comp);
			sb.append("</div>\n");
		}
		
		sb.append(getFooter());
		
		Files.createFile(file, sb.toString());
	}

	private void optionsTable(StringBuffer sb, Class<?> comp) {
		// generate table for configuration options
		Map<ConfigOption,Class<?>> options = ConfigHelper.getConfigOptionTypes(comp);
		if(options.isEmpty()) {
			sb.append("This component does not have configuration options.");
		} else {
		sb.append("<div class=\"table-responsive\"><table class=\"hor-minimalist-a table table-hover\"><thead><tr><th>option name</th><th>description</th><th>type</th><th>default value</th><th>required?</th></tr></thead><tbody>\n");
		for(Entry<ConfigOption,Class<?>> entry : options.entrySet()) {
			ConfigOption option = entry.getKey();
			String type = entry.getValue().getSimpleName();
			if(entry.getValue().equals(OWLClass.class)) {
				type = "IRI";
			}
			sb.append("<tr><td>" + option.name() + "</td><td>" + option.description() + "</td><td> " + type + "</td><td>" + option.defaultValue() + "</td><td> " + option.required() + "</td></tr>\n");
		}
		sb.append("</tbody></table></div>\n");
		}
	}		
	
	private String getHeader() {
		StringBuffer sb = new StringBuffer();
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<html><head><meta charset=\"UTF-8\"><title>DL-Learner components and configuration options</title>\n");
		sb.append("<style type=\"text/css\">\n");
		sb.append("@import url(\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.css\");\n");
		sb.append("body { line-height: 1.6em; font-size: 15px; font-family: \"Lucida Sans Unicode\", \"Lucida Grande\", Sans-Serif;  }\n");
		sb.append("h1, h2 { font-family: \"Droid Serif\", Serif; font-weight: 800; color: #c33; }\n");
		sb.append(".hor-minimalist-a 	{ font-size: 13px;	background: #fff; margin: 30px;	width: 90%;border-collapse: collapse; 	text-align: left; } \n");
		sb.append(".hor-minimalist-a th { font-size: 15px;	font-weight: normal; color: #039; padding: 10px 8px; border-bottom: 2px solid #6678b1; white-space: nowrap;	}\n");
		sb.append(".hor-minimalist-a td	{ color: #669;padding: 9px 8px 0px 8px;	}\n");
		sb.append(".hor-minimalist-a tbody tr:hover td 	{ color: #009; }\n");
		sb.append("@media screen and (max-width: 767px) {\n"
				+ ".table-responsive > .table > thead > tr > th, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > th, "
				+ ".table-responsive > .table > thead > tr > td, .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tfoot > tr > td {  white-space: inherit;  } }\n");
		sb.append("</style>\n");
		sb.append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>");
		sb.append("<script type=\"text/javascript\" language=\"javascript\">\n");
		sb.append("//<![CDATA[\n"); 
		sb.append("function showOnlyCat(className){\n");
		sb.append("	 $('div.type').show(); $('div.type').not('.'+className).hide(); }\n");
		sb.append("function showAllCat(){\n");
		sb.append("  $('div.type').show() };\n");
		sb.append("//]]>\n");
		sb.append("</script>\n"); 
		sb.append("</head><body><div class=\"container-fluid\">\n");
		return sb.toString();
	}
	
	private String getFooter() {
		return "</div></body></html>";
	}
	
	// this is a hack, because we just assume that every PropertyEditor is named 
	// as TypeEditor (e.g. ObjectPropertyEditor); however that hack does not too much harm here
//	private static String getOptionType(ConfigOption option) {
//		String name = option.propertyEditorClass().getSimpleName();
//		return name.substring(0, name.length()-6);
//	}	
	
	private static String getCoreTypes(Class<?> comp) {
		if (Component.class.isAssignableFrom(comp)) {
		List<Class<? extends Component>> types = AnnComponentManager.getCoreComponentTypes((Class<? extends Component>) comp);
		String str = "";
		for(Class<?extends Component> type : types) {
			str += " " + type.getSimpleName();
		}
		// not every component belongs to one of the core types
		if(str.length()==0) {
			return "OtherComponent";
		} else {
			return str.substring(1);
		}
		} else {
			return "Class";
		}
	}
		
	public static void main(String[] args) {
		File file = new File("doc/configOptions.html");
		DocumentationHTMLGenerator dg = new DocumentationHTMLGenerator();
		dg.writeConfigDocumentation(file);
		System.out.println("Done");
	}
	
}