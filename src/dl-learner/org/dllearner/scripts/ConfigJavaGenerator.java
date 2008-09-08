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
package org.dllearner.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.Files;


/**
 * Collects information about all used configuration options and writes them
 * into a file. This way the documentation is always in sync with the source
 * code.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 * 
 */
public class ConfigJavaGenerator {
	
	private static final boolean INCLUDE_UNUSED = false;
	private static final String UNUSED = "@SuppressWarnings(\"unused\")\n";

	private static final String TARGET_DIR = "src/dl-learner/org/dllearner/core/configurators";
	private static final String TARGET_PACKAGE = "org.dllearner.core.configurators";

	private static final String HEADER_FILE = "doc/header.txt";

	private static final String HEADER = getHeader();
	
	private static final String REINITVAR = "reinitNecessary";
	private static final String REINITGETTER = 
		"public boolean is"+capitalize(REINITVAR)+"(){\nreturn "+REINITVAR+";\n}\n";

	//private static final String TEMPLATE_DIR = "doc/template/";

	private static final String CONFIGURATOR = "Configurator";
	private static final String COMPONENT_FACTORY = "ComponentFactory";
	
	private static final SortedSet<String> configuratorImports = new TreeSet<String>();
	private static final List<String> configuratorMethods = new ArrayList<String>();
	
	Class<? extends Component> component;
 	String className;
 	String componentType;
	List<String>  body = new ArrayList<String>();
	List<String>  vars = new ArrayList<String>();
	List<String>  setters = new ArrayList<String>();
	List<String>  getters = new ArrayList<String>();
	SortedSet<String>  imports = new TreeSet<String>();
	SortedSet<String>  getinstanceExceptions = new TreeSet<String>();
	Map<String,String>  additionalMandatoryVars = new LinkedHashMap<String, String>();
	Map<String,String>  mandatoryVars = new LinkedHashMap<String, String>();
	Map<String,String>  varMap = new LinkedHashMap<String, String>();
	
	List<ConfigOption<?>> mandatoryOptions = new LinkedList<ConfigOption<?>>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Files.backupDirectory(TARGET_DIR);
		//System.exit(0);
		Files.deleteDir(TARGET_DIR);
		
		ComponentManager cm = ComponentManager.getInstance();
		configuratorImports.add(ComponentManager.class.getCanonicalName());
		configuratorImports.add(KnowledgeSource.class.getCanonicalName());
		//configuratorImports.add(ReasonerComponent.class.getCanonicalName());
		configuratorImports.add(ReasoningService.class.getCanonicalName());
		configuratorImports.add(LearningProblem.class.getCanonicalName());
		configuratorImports.add(LearningAlgorithm.class.getCanonicalName());
		configuratorImports.add(LearningProblemUnsupportedException.class.getCanonicalName());

		for (Class<? extends KnowledgeSource> component : cm
				.getKnowledgeSources()) {
			String componentType = "knowledgeSource";
			
			configuratorImports.add(component.getCanonicalName());
			configuratorImports.add(TARGET_PACKAGE+"."+component.getSimpleName()+CONFIGURATOR);
			ConfigJavaGenerator c = new ConfigJavaGenerator(component, componentType);
			c.makeSubclasses();
			
		}
		for (Class<? extends ReasonerComponent> component : cm.getReasonerComponents()) {
			
			configuratorImports.add(component.getCanonicalName());
			configuratorImports.add(TARGET_PACKAGE+"."+component.getSimpleName()+CONFIGURATOR);
			
			ConfigJavaGenerator c = new ConfigJavaGenerator(component, "reasoner");
			c.imports.add("org.dllearner.core.KnowledgeSource");
			c.additionalMandatoryVars.put("KnowledgeSource knowledgeSource","knowledgeSource");
			c.makeSubclasses();
					
		}
		for (Class<? extends LearningProblem> component : cm.getLearningProblems()) {
			
			configuratorImports.add(component.getCanonicalName());
			configuratorImports.add(TARGET_PACKAGE + "." + component.getSimpleName()+CONFIGURATOR);
			
			ConfigJavaGenerator c = new ConfigJavaGenerator(component, "learningProblem");
			c.imports.add("org.dllearner.core.ReasoningService");
			c.additionalMandatoryVars.put("ReasoningService reasoningService","reasoningService");
			c.makeSubclasses();
					
		}
		
		for (Class<? extends LearningAlgorithm> component : cm.getLearningAlgorithms()) {
			
			configuratorImports.add(component.getCanonicalName());
			configuratorImports.add(TARGET_PACKAGE+"."+ component.getSimpleName()+CONFIGURATOR);
			
			ConfigJavaGenerator c = new ConfigJavaGenerator(component, "learningAlgorithm");
			c.imports.add("org.dllearner.core.LearningProblem");
			c.imports.add("org.dllearner.core.ReasoningService");
			c.imports.add(LearningProblemUnsupportedException.class.getCanonicalName());
			
			c.additionalMandatoryVars.put("LearningProblem learningProblem","learningProblem");
			c.additionalMandatoryVars.put("ReasoningService reasoningService","reasoningService");
			c.getinstanceExceptions.add("LearningProblemUnsupportedException");
			c.makeSubclasses();
					
		}
		
		
		
		makeComponentFactory();

		System.out.println("Done");
	}
	
	public ConfigJavaGenerator (Class<? extends Component>  component, String componentType){
		className = component.getSimpleName();
		this.component = component;
		this.componentType = componentType;
		imports.add(component.getCanonicalName());
		imports.add(ComponentManager.class.getCanonicalName());
		//imports.add(Configurator.class.getCanonicalName());
		//imports.add(ConfigEntry.class.getCanonicalName());
		
		vars.add("private " + className+" "+className+";\n");
	
	}
	
	public  void  makeSubclasses(){
	
		for (ConfigOption<?> option : ComponentManager
				.getConfigOptions(component)) {
			
			String type = option.getValueTypeAsJavaString();
			String optionName = option.getName();
			//String defaultValue = option.getDefaultValueInJava();
			String comment = option.getJavaDocString();
			
			if(option.isMandatory()){
				mandatoryVars.put(type + " " + optionName , optionName);
				mandatoryOptions.add(option);
				configuratorImports.addAll(option.getJavaImports());
			}	
			
			imports.addAll(option.getJavaImports());
			//vars.add(fillVariableDefTemplate(optionName, type, defaultValue));
			setters.add(fillSetterTemplate(className, comment, optionName, type, option.requiresInit()));
			getters.add(fillGetterTemplate(className, comment, optionName, type));
			// System.out.println(option);
			// componentOptions.get(component)) {

		}
		
		
		
		body.add("private boolean "+REINITVAR+" = false;");
		body.add(expandCollection(vars, "", "", 0));
		body.add(fillConstructorTemplate(className));
		
		body.add(makeGetInstanceForSubclass(className, componentType, 
				getAllCommentsForOptionList(mandatoryOptions)));
		
		//body.add(makeApplyConfigEntryForOptionList(ComponentManager.getConfigOptions(component)));
		
		body.add(expandCollection(getters, "", "", 0));
		body.add(expandCollection(setters, "", "", 0));
		body.add(REINITGETTER);
		String ret = fillClassTemplate(
				TARGET_PACKAGE, 
				expandCollection(imports, "import ", ";\n", 0), 
				className+CONFIGURATOR, 
				"", 
				expandCollection(body, "", "\n", 0) );
		
		
		
		
		
		//configuratorMethods.add((className, componentType, mandatoryOptions));
		
		Files.createFile(new File(TARGET_DIR + "/" + className
				+ CONFIGURATOR + ".java"), ret);
		
		configuratorMethods.add(
				makeSuperConfiguratorMethods(
						className, 
						componentType, 
						getAllCommentsForOptionList(mandatoryOptions)));
	}
	
	
	
	public static String makeApplyConfigEntryForOptionList(List<ConfigOption<?>> options){
		String ret ="@SuppressWarnings({ \"unchecked\" })\n" +
				"public <T> void applyConfigEntry(ConfigEntry<T> entry){\n";
		ret+="String optionName = entry.getOptionName();\n";
		//ret+="ConfigOption<T> option = entry.getOption();\n";
		ret+="if(false){//empty block \n}";
		
		for (ConfigOption<?> option : options) {
			ret+="else if (optionName.equals(\""+option.getName()+"\")){\n";
			ret+=""+option.getName()+" = ("+rightType(option.getValueTypeAsJavaString())+") " +
				" entry.getValue();\n}";
				
		}
		ret+="\n}\n";
		return ret;
	}
	
	
	private static String getAllCommentsForOptionList(List<ConfigOption<?>> options){
		String ret = "";
		for (ConfigOption<?> option : options) {
			ret+="* @param "+option.getName()+" "+option.getDescription()+"\n";
		}
		
		return fillJavaDocComment(ret);
	}
	
	private  String makeGetInstanceForSubclass(String className, String componentType, 
			 String comments){
		Map<String, String> parametersWithType = new LinkedHashMap<String,String>();
		Map<String, String> parametersNoType = new LinkedHashMap<String,String>();
		String applyConf = "";
		//parametersWithType.put("ComponentManager cm", "cm");
		parametersNoType.put(className+".class", className+".class");
		for (String s : additionalMandatoryVars.keySet()) {
			parametersWithType.put(s, additionalMandatoryVars.get(s));
			parametersNoType.put(s,  additionalMandatoryVars.get(s));
		}
		for (String s : mandatoryVars.keySet()) {
			parametersWithType.put(s, mandatoryVars.get(s));
			applyConf += fillApplyConfigEntry("component", mandatoryVars.get(s))+"";
		}
		
		String parWithType = expandCollection(parametersWithType.keySet(), "", ", ", 2);
		
		String par = expandCollection(parametersNoType.values(), "", ", ", 2); 
		
		String exceptions = "";
		if (!getinstanceExceptions.isEmpty()){
			exceptions += "throws ";
			exceptions += expandCollection(getinstanceExceptions, "", ", ", 2);
		}
		
		String ret = comments;
		ret += "public static "+className+" get"+className+" ("+parWithType+" ) "+exceptions+"{\n";
		ret +=	className+" component = ComponentManager.getInstance()."+componentType+"(" +par+" );\n";
		ret += applyConf;
	
		ret+="return component;\n}\n";
		return ret;
	}
	
	private String makeSuperConfiguratorMethods(
			String className, String componentType, String comments){
		
		Map<String, String> parametersWithType = new LinkedHashMap<String,String>();
		Map<String, String> parametersNoType = new LinkedHashMap<String,String>();
		String applyConf = "";
		//parametersWithType.put("ComponentManager cm", "cm");
		parametersNoType.put(className+".class", className+".class");
		for (String s : additionalMandatoryVars.keySet()) {
			parametersWithType.put(s, additionalMandatoryVars.get(s));
			parametersNoType.put(s,  additionalMandatoryVars.get(s));
		}
		for (String s : mandatoryVars.keySet()) {
			parametersWithType.put(s, mandatoryVars.get(s));
			applyConf += fillApplyConfigEntry("component", mandatoryVars.get(s))+"";
		}
		
		String parWithType = expandCollection(parametersWithType.keySet(), "", ", ", 2);
		
		String par = expandCollection(parametersWithType.values(), "", ", ", 2); 
		
		String exceptions = "";
		if (!getinstanceExceptions.isEmpty()){
			exceptions += "throws ";
			exceptions += expandCollection(getinstanceExceptions, "", ", ", 2);
		}
		
		String ret = comments;
		ret += "public static "+className+" get"+className+" ("+parWithType+" ) "+exceptions+" {\n" +
		"return "+ className+CONFIGURATOR+".get"+className+"("+par+");\n}\n";
		return ret;
	}
	
	public static void makeComponentFactory(){
		String ret=
			fillClassTemplate(
				TARGET_PACKAGE, 
				expandCollection(configuratorImports, "import ", ";\n",0), 
				COMPONENT_FACTORY, 
				"",
				expandCollection(configuratorMethods, "", "\n", 0)
				);
		
		
		Files.createFile(new File(TARGET_DIR + "/" + COMPONENT_FACTORY + ".java"), ret);
	}
	

	
	private static String fillApplyConfigEntry(String className, String optionName){
		return "ComponentManager.getInstance().applyConfigEntry("+className+", \""+optionName+"\", "+optionName+");\n";
	}
	
	private static String fillConstructorTemplate(String className){
		return "public "+className+CONFIGURATOR+" ("+className+" "+className+"){\n" +
				"this."+className+" = "+className+";\n" +
				"}\n";   
	}
	

	private static String fillVariableDefTemplate(String optionName, String type, String defaultValue) {
		return "private " + type + " "
				+ optionName + " = " + defaultValue
				+ ";\n";

	}
	
	private static String fillJavaDocComment(String lines){
		return "/**\n"+lines+"**/\n";
	}

	
	
	private static String fillGetterTemplate( String className,String comment, String optionName, String type) {
		String ret = fillJavaDocComment (comment);
			ret+= (type.contains("<String>"))?"@SuppressWarnings(\"unchecked\")\n":"";
			ret+= "public "+type+" get" + capitalize(optionName) + " ( ) {\n";
			ret+= "return ("+rightType(type)+") ComponentManager.getInstance().";
			ret+= "getConfigOptionValue("+className+ ",  \""+optionName+"\") ;\n";
			ret+=		"}\n";
			return ret;
	}
	private static String fillSetterTemplate(String className, String comment, String optionName, String type, boolean reinit ){
		String ret = fillJavaDocComment (comment);
		ret += "public void set" + capitalize(optionName);
		ret += " ( "+  type +" "+ optionName+") {\n";
		ret += fillApplyConfigEntry(className, optionName);
		ret += (reinit)?REINITVAR + " = true;\n":"";
		ret += "}\n" ;
		return ret;
	}
	
	
	
	private static String fillClassTemplate(String Package, String imports, String className, String Extends, String body){
		String ret = HEADER +"\n";
		ret += "package "+Package+";\n\n";
		ret += imports+"\n";
		ret += fillJavaDocComment("* automatically generated, do not edit manually\n");
		ret += (INCLUDE_UNUSED)?UNUSED:"";
		ret += "public class "+className+" "+((Extends.length()>0)?"extends "+Extends:"")+" {\n\n";
		ret += body+"\n";
		ret +="}\n";
		return ret;
		
	}
	
	
	
	private static String expandCollection (Collection<String> col, String before, String after, int removeChars){
			if(col.isEmpty())return "";
			String ret ="";
			for (String string : col) {
				ret+= before+string+after;
			}
			
			if(removeChars==0){
				return ret;
			}else {
				return ret.substring(0, ret.length()-removeChars);
			}
		}
		
	
	
	private static String capitalize(String s){
		String tmp = s.substring(0, 1);
		return tmp.toUpperCase() + s.substring(1);
	}
	
	public static String rightType(String type){
		if(type.equals("int"))return "Integer";
		else if(type.equals("boolean"))return "Boolean";
		else if(type.equals("double"))return "Double";
		else return type;
		
	}
	

	private static String getHeader() {
		try {
			return Files.readFile(new File(HEADER_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
