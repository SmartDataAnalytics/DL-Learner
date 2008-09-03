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
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.utilities.Files;


/**
 * Collects information about all used configuration options and writes them
 * into a file. This way the documentation is always in sync with the source
 * code.
 * 
 * @author Jens Lehmann
 * 
 */
public class ConfigJavaGenerator {

	private static final String TARGET_DIR = "src/dl-learner/org/dllearner/core/configuration";

	private static final String HEADER_FILE = "doc/header.txt";

	private static final String HEADER = getHeader();

	private static final String TEMPLATE_DIR = "doc/template/";

	private static final String CLASSADDITION = "Configurator";
	
	private static final SortedSet<String> configuratorImports = new TreeSet<String>();
	private static final SortedSet<String> configuratorMethods = new TreeSet<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Files.deleteDir(TARGET_DIR);

		ComponentManager cm = ComponentManager.getInstance();
		configuratorImports.add(ComponentManager.class.getCanonicalName());
		

		for (Class<? extends KnowledgeSource> component : cm
				.getKnowledgeSources()) {
			String componentType = "knowledgeSource";
			
			if (component.getSimpleName().equalsIgnoreCase(
				"OWLFile")) {
				configuratorImports.add(component.getCanonicalName());
				configuratorImports.add("org.dllearner.core.configuration."+component.getSimpleName()+CLASSADDITION);
				make (component, componentType);
			}if (component.getSimpleName().equalsIgnoreCase(
				"SparqlKnowledgeSource")) {
				configuratorImports.add(component.getCanonicalName());
				configuratorImports.add("org.dllearner.core.configuration."+component.getSimpleName()+CLASSADDITION);
				make (component, componentType);
			}
			
			
		}
		
		makeConfiguratorSuperClass();

		System.out.println("Done");
	}
	
	public static void makeConfiguratorSuperClass(){
		StringBuffer current = new StringBuffer();
		current.append(HEADER+"\n");
		current.append("package org.dllearner.core.configuration;\n\n");
		for (String string : configuratorImports) {
			current.append("import "+string+";\n");
		}
		current.append("\n"+getClassComment());
		current.append("public class "+CLASSADDITION+" {\n\n");
		for (String string : configuratorMethods) {
			current.append(string+"\n");
		}
		current.append("}\n");
		Files.createFile(new File(TARGET_DIR + "/" + CLASSADDITION + ".java"), current.toString());
	}
	
	public static void  make(Class<? extends KnowledgeSource>  component, String componentType){
		StringBuffer current = new StringBuffer();
		StringBuffer vars = new StringBuffer();
		StringBuffer setters = new StringBuffer();
		StringBuffer getters = new StringBuffer();
		SortedSet<String> imports = new TreeSet<String>();
		String className = component.getSimpleName();
		List<ConfigOption<?>> mandatoryOptions = new LinkedList<ConfigOption<?>>();

		imports.add(component.getCanonicalName());
		imports.add(ComponentManager.class.getCanonicalName());
		imports.add(Configurator.class.getCanonicalName());
		imports.add(ConfigEntry.class.getCanonicalName());
		//imports.add("import "+ConfigOption.class.getCanonicalName()+";");
		
		vars.append(className+" "+ className+";\n");
		current.append(HEADER + "\n");
		current.append("package org.dllearner.core.configuration;\n\n");
		
	
		for (ConfigOption<?> option : ComponentManager
				.getConfigOptions(component)) {
			if(option.isMandatory()){
				mandatoryOptions.add(option);
				configuratorImports.addAll(option.getJavaImports());
			}
			
			imports.addAll(option.getJavaImports());
			vars.append(getVarDef(option));
			setters.append(getSetter(option));
			getters.append(getGetter(option));
			// System.out.println(option);
			// componentOptions.get(component)) {

		}
		for (String string : imports) {
			current.append("import "+string+";\n");
		}
		current.append("\n\n");
		current.append(getClassDefinition(className));
		current.append(vars);
		current.append("\n\n");
		current.append(getMandatoryFunctions(className, componentType, mandatoryOptions));
		current.append("\n\n");
		current.append(makeApplyConfigEntry(ComponentManager.getConfigOptions(component)));
		current.append("\n\n");
		current.append(setters+"\n\n"+getters+"\n\n");
		current.append("}\n");
		
		configuratorMethods.add(getSuperClassFunction(className, componentType, mandatoryOptions));
		
		Files.createFile(new File(TARGET_DIR + "/" + className
				+ CLASSADDITION + ".java"), current.toString());
	}
	
	private static String getConstructor(String className){
		return "";
	}
	
	public static String makeApplyConfigEntry(List<ConfigOption<?>> options){
		String ret ="@SuppressWarnings({ \"unchecked\" })\n" +
				"public <T> void applyConfigEntry(ConfigEntry<T> entry){\n";
		ret+="String optionName = entry.getOptionName();\n";
		//ret+="ConfigOption<T> option = entry.getOption();\n";
		if(!options.isEmpty()){
			ConfigOption<?> first = options.remove(0);
			ret+="if (optionName.equals("+first.getName()+")){\n" +
				""+first.getName()+" = ("+rightType(first.getValueTypeAsJavaString())+") " +
				" entry.getValue();\n";
		}
		for (ConfigOption<?> option : options) {
			ret+="}else if (optionName.equals("+option.getName()+")){\n";
			ret+=""+option.getName()+" = ("+rightType(option.getValueTypeAsJavaString())+") " +
				" entry.getValue();\n";
				
		}
		ret+="}\n}\n";
		return ret;
	}
	
	public static String rightType(String type){
		if(type.equals("int"))return "Integer";
		else if(type.equals("boolean"))return "Boolean";
		else return type;
		
	}
	
	private static String getMandatoryFunctionComment(List<ConfigOption<?>> options){
		String ret = "/**\n";
		for (ConfigOption<?> option : options) {
			ret+=option.getDescription()+"\n";
		}
		ret+="**/\n";
		return ret;
	}
	
	private static String getMandatoryFunctions(String className, String componentType, List<ConfigOption<?>> options){
		String mandParametersWithType = getMandatoryParameters(options, true);
		//String mandParametersNoType = getMandatoryParameters(options, false);
		String mandFunctionBody = mandatoryFunctionBody(options);
		String ret= getMandatoryFunctionComment(options); 
			ret += "public void setMandatoryOptions ("+mandParametersWithType+" ) {\n" +
				    ""+mandFunctionBody+"}\n";
		
		ret+=  getMandatoryFunctionComment(options);
		ret+= "public static "+className+" get"+className+" (ComponentManager cm, "+mandParametersWithType+" ) {\n" +
		className+" component = cm."+componentType+"("+className+".class);\n";
		for (ConfigOption<?> option : options) {
			ret+="cm.applyConfigEntry(component, \""+option.getName()+"\", "+option.getName()+");\n";
		}
		
		ret+="return component;\n}\n";
		return ret;
	}
	
	private static String getSuperClassFunction(String className, String componentType, List<ConfigOption<?>> options){
		String mandParametersWithType = getMandatoryParameters(options, true);
		String mandParametersNoType = getMandatoryParameters(options, false);
		//String mandFunctionBody = mandatoryFunctionBody(options);
		 String ret = getMandatoryFunctionComment(options);
		 ret += "public static "+className+" get"+className+" (ComponentManager cm, "+mandParametersWithType+" ) {\n" +
		"return "+ className+CLASSADDITION+".get"+className+"(cm, "+mandParametersNoType+");\n}\n";
		return ret;
	}
	
	public static String getMandatoryParameters (List<ConfigOption<?>> options, boolean includeType){
		if(options.isEmpty())return "";
		String ret = "";
		String type = "";
		for (ConfigOption<?> option : options) {
			type = (includeType)?option.getValueTypeAsJavaString():"";
			ret += type + " " + option.getName()+", "; 
		}
		ret = ret.substring(0,ret.length()-2);
		return ret;
	}
	
	private static String mandatoryFunctionBody(List<ConfigOption<?>> options){
		String ret = "";
		for (ConfigOption<?> option : options) {
			ret += "this."+option.getName()+" = "+option.getName()+";\n";
		}
		return ret;
	}
	

	private static String getVarDef(ConfigOption<?> option) {
		return "private " + option.getValueTypeAsJavaString() + " "
				+ option.getName() + " = " + option.getDefaultValueInJava()
				+ ";\n";

	}

	private static String getSetter(ConfigOption<?> option) {
		String s = option.getName().substring(0, 1);
		s = s.toUpperCase() + option.getName().substring(1);
		String comment = "/**\n" +
				"* "+option.getDescription()+"\n" +
				"**/\n";
		
	return comment + "public void set" + s + " (" + option.getValueTypeAsJavaString()
				+ " "+option.getName()+") {\n" +
						"this." + option.getName()+" = "+ option.getName()+
						";\n" +
						"}\n\n";

	}
	
	private static String getGetter(ConfigOption<?> option) {
		String s = option.getName().substring(0, 1);
		s = s.toUpperCase() + option.getName().substring(1);
		String comment = "/**\n" +
				"* "+option.getDescription()+"\n" +
				"* \n" +
				"**/\n";
		
	return comment + "public "+option.getValueTypeAsJavaString()+" get" + s + " ( ) {\n" +
						"return this." + option.getName()+";\n" +
						"}\n\n";

	}

	private static String getClassDefinition(String className) {
		String ret = getClassComment()
				+ "public class " + className + CLASSADDITION + " extends Configurator {\n" + "\n";
		return ret;
	}
	
	private static String getClassComment(){
		return "" + "/**\n"
		+ "* automatically generated, do not edit manually\n" + "**/\n";

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
