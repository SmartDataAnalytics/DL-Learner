/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.configurators.FuzzyCELOEConfigurator;
import org.dllearner.core.configurators.ISLEConfigurator;
import org.dllearner.core.configurators.OCELConfigurator;
import org.dllearner.core.configurators.ROLearnerConfigurator;
import org.dllearner.core.configurators.RefinementOperatorConfigurator;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.FastRetrievalReasoner;
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
public final class ConfigJavaGenerator {
	
	private static final SortedSet<String> DONOTDELETE = 
		new TreeSet<String>(Arrays.asList(new String[]{
				".svn",
				"RefinementOperatorConfigurator.java",
				}));
	
	// currently it targets the configurators for 
	private static final SortedSet<String> EXTENDSREFINEMENTOPERATOR = 
		new TreeSet<String>(Arrays.asList(new String[]{
				ROLearnerConfigurator.class.getSimpleName(),
				OCELConfigurator.class.getSimpleName(),
				CELOEConfigurator.class.getSimpleName(),
				FuzzyCELOEConfigurator.class.getSimpleName(),
				ISLEConfigurator.class.getSimpleName()
				}));
	
	private static final Class<RefinementOperatorConfigurator> EXTENDSREFINEMENTOPERATORCLASS = RefinementOperatorConfigurator.class;

//	private static final boolean INCLUDE_UNUSED = false;

	@SuppressWarnings("unused")
	private static final String UNUSED = "@SuppressWarnings(\"unused\")\n";
	private static final String OVERRIDE = "@SuppressWarnings(\"all\")\n";

	private static final String TARGET_DIR = "../components-core/src/main/java/org/dllearner/core/configurators";

	private static final String TARGET_PACKAGE = "org.dllearner.core.configurators";

	private static final String HEADER_FILE = "../interfaces/doc/header.txt";

	private static final String HEADER = getHeader();

	private static final String REINITVAR = "reinitNecessary";

	private static final String REINITGETTER = "/**\n* true, if this component needs reinitializsation.\n"
			+ "* @return boolean\n**/\n"
			+ "public boolean is"
			+ capitalize(REINITVAR)
			+ "(){\nreturn "
			+ REINITVAR + ";\n}\n";

	// private static final String TEMPLATE_DIR = "doc/template/";

	private static final String CONFIGURATOR = "Configurator";

	private static final String COMPONENT_FACTORY = "ComponentFactory";
	private static final String CLASS_COMMENT = "* automatically generated, do not edit manually.\n"
		+ "* run " + ConfigJavaGenerator.class.getCanonicalName()+ " to update\n";

	private static final SortedSet<String> COMPONENT_FACTORY_IMPORTS = new TreeSet<String>();

	private static final List<String> COMPONENT_FACTORY_METHODS = new ArrayList<String>();

	private Class<? extends Component> component;

	private String className;

	private String componentType;
	
	private String extendS = "";
	
	//private String implementS = "";

	private List<String> body = new ArrayList<String>();

	private List<String> vars = new ArrayList<String>();

	private List<String> setters = new ArrayList<String>();

	private List<String> getters = new ArrayList<String>();

	private SortedSet<String> imports = new TreeSet<String>();

	private SortedSet<String> getinstanceExceptions = new TreeSet<String>();

	private Map<String, String> additionalMandatoryVars = new LinkedHashMap<String, String>();

	private Map<String, String> mandatoryVars = new LinkedHashMap<String, String>();

	// private Map<String,String> varMap = new LinkedHashMap<String, String>();

	private List<ConfigOption<?>> mandatoryOptions = new LinkedList<ConfigOption<?>>();

	/**
	 * @param args
	 *            none
	 */
	public static void main(String[] args) {

		Files.backupDirectory(TARGET_DIR);
		System.out.println("previous classes were backupped to tmp/+System.currentTimeMillis()");
		String[] files = Files.listDir(TARGET_DIR); 
		
		for (String file : files){
			//System.out.println(DONOTDELETE);
			
			if(DONOTDELETE.contains(file)){
				continue;
			}
			//System.out.println(file);
			String todelete = TARGET_DIR + File.separator + file;
			Files.deleteFile(todelete);
			
		}
		//System.exit(0);
		//Files.deleteDir(TARGET_DIR);

		ComponentManager cm = ComponentManager.getInstance();
		COMPONENT_FACTORY_IMPORTS.add(KnowledgeSource.class.getCanonicalName());
		COMPONENT_FACTORY_IMPORTS
				.add(ReasonerComponent.class.getCanonicalName());
		COMPONENT_FACTORY_IMPORTS.add(LearningProblem.class.getCanonicalName());
		COMPONENT_FACTORY_IMPORTS.add(LearningProblemUnsupportedException.class
				.getCanonicalName());
		COMPONENT_FACTORY_METHODS.add("private "+COMPONENT_FACTORY+"(){}\n");

		for (Class<? extends KnowledgeSource> component : cm
				.getKnowledgeSources()) {
			String componentType = "knowledgeSource";

			COMPONENT_FACTORY_IMPORTS.add(component.getCanonicalName());
			// configuratorImports.add(TARGET_PACKAGE+"."+component.getSimpleName()+CONFIGURATOR);
			ConfigJavaGenerator c = new ConfigJavaGenerator(component,
					componentType);
			c.makeConfigurator();

		}
		for (Class<? extends ReasonerComponent> component : cm
				.getReasonerComponents()) {

			COMPONENT_FACTORY_IMPORTS.add(component.getCanonicalName());
			// configuratorImports.add(TARGET_PACKAGE+"."+component.getSimpleName()+CONFIGURATOR);

			ConfigJavaGenerator c = new ConfigJavaGenerator(component,
					"reasoner");
			c.imports.add("org.dllearner.core.KnowledgeSource");
			c.imports.add(Set.class.getCanonicalName());
			c.additionalMandatoryVars.put("Set<KnowledgeSource> knowledgeSource",
					"knowledgeSource");
			c.makeConfigurator();

		}
		for (Class<? extends LearningProblem> component : cm
				.getLearningProblems()) {

			COMPONENT_FACTORY_IMPORTS.add(component.getCanonicalName());
			// configuratorImports.add(TARGET_PACKAGE + "." +
			// component.getSimpleName()+CONFIGURATOR);

			ConfigJavaGenerator c = new ConfigJavaGenerator(component,
					"learningProblem");
			c.imports.add("org.dllearner.core.ReasonerComponent");
			c.additionalMandatoryVars.put("ReasonerComponent reasoningService",
					"reasoningService");
			c.makeConfigurator();

		}

		for (Class<? extends AbstractCELA> component : cm
				.getLearningAlgorithms()) {

			COMPONENT_FACTORY_IMPORTS.add(component.getCanonicalName());
			// configuratorImports.add(TARGET_PACKAGE+"."+
			// component.getSimpleName()+CONFIGURATOR);

			ConfigJavaGenerator c = new ConfigJavaGenerator(component,
					"learningAlgorithm");
			c.imports.add("org.dllearner.core.LearningProblem");
			c.imports.add("org.dllearner.core.ReasonerComponent");
			c.imports.add(LearningProblemUnsupportedException.class
					.getCanonicalName());

			c.additionalMandatoryVars.put("LearningProblem learningProblem",
					"learningProblem");
			c.additionalMandatoryVars.put("ReasonerComponent reasoningService",
					"reasoningService");
			c.getinstanceExceptions.add("LearningProblemUnsupportedException");
			c.makeConfigurator();

		}

		makeComponentFactory();
		makeInterface();
		writePackageHTML();

		System.out.println("Done");
	}
	
	

	private ConfigJavaGenerator(Class<? extends Component> component,
			String componentType) {
		this.className = component.getSimpleName();
		this.component = component;
		this.componentType = componentType;
		imports.add(component.getCanonicalName());
		imports.add(ComponentManager.class.getCanonicalName());
		// imports.add(Configurator.class.getCanonicalName());
		// imports.add(ConfigEntry.class.getCanonicalName());

		vars.add("private " + className + " " + deCapitalize(className)
						+ ";\n");
		
		if(EXTENDSREFINEMENTOPERATOR.contains(this.className+CONFIGURATOR)){
				this.extendS = EXTENDSREFINEMENTOPERATORCLASS.getSimpleName();
				this.imports.add(EXTENDSREFINEMENTOPERATORCLASS.getCanonicalName());
			}
			

	}

	private void makeConfigurator() {

		for (ConfigOption<?> option : ComponentManager
				.getConfigOptions(component)) {

			String type = option.getValueTypeAsJavaString();
			String optionName = option.getName();
			// String defaultValue = option.getDefaultValueInJava();
			String comment = option.getJavaDocString();

			if (option.isMandatory()) {
				mandatoryVars.put(type + " " + optionName, optionName);
				mandatoryOptions.add(option);
				COMPONENT_FACTORY_IMPORTS.addAll(option.getJavaImports());
			}

			imports.addAll(option.getJavaImports());
			// vars.add(fillVariableDefTemplate(optionName, type,
			// defaultValue));
			setters.add(fillSetterTemplate(className, comment, optionName,
					type, option.requiresInit()));
			getters
					.add(fillGetterTemplate(className, comment, optionName,
							type));
			// System.out.println(option);
			// componentOptions.get(component)) {

		}

		body.add("private boolean " + REINITVAR + " = false;");
		// suppress warnings if necessary
		if(component.equals(FastRetrievalReasoner.class) || component.equals(OWLAPIOntology.class)) {
			body.add(UNUSED);
		}
		body.add(expandCollection(vars, "", "", 0));
		body.add(fillConstructorTemplate(className));

		body
				.add(makeGetInstanceForConfigurators(getAllCommentsForOptionList(mandatoryOptions)));

		// body.add(makeApplyConfigEntryForOptionList(ComponentManager.getConfigOptions(component)));
 
		body.add(expandCollection(getters, "", "", 0));
		body.add(expandCollection(setters, "", "", 0));
		body.add(REINITGETTER);
		String bodytmp = expandCollection(body, "", "\n",0);
		String importtmp =  expandCollection(imports, "import ", ";\n", 0);
		String ret = fillClassTemplate(
				TARGET_PACKAGE,
				importtmp, 
				className + CONFIGURATOR, 
				extendS,
				bodytmp ,
				"", 
				CONFIGURATOR); 

		// configuratorMethods.add((className, componentType,
		// mandatoryOptions));

		Files.createFile(new File(TARGET_DIR + "/" + className + CONFIGURATOR
				+ ".java"), ret);

		COMPONENT_FACTORY_METHODS
				.add(makeComponentFactoryMethods(getAllCommentsForOptionList(mandatoryOptions)));
	}

	private static void makeInterface(){
		String ret ="";
		ret+= HEADER+"\n";
		ret+= "package "+TARGET_PACKAGE+";\n\n";
		ret+= fillJavaDocComment(CLASS_COMMENT);
		ret+="public interface Configurator{\n}\n";
		Files.createFile(new File(TARGET_DIR+File.separator+CONFIGURATOR+".java"), ret);
	}
	
	@SuppressWarnings("unused")
	private static String makeApplyConfigEntryForOptionList(
			List<ConfigOption<?>> options) {
		String ret = "@SuppressWarnings({ \"unchecked\" })\n"
				+ "public <T> void applyConfigEntry(ConfigEntry<T> entry){\n";
		ret += "String optionName = entry.getOptionName();\n";
		// ret+="ConfigOption<T> option = entry.getOption();\n";
		ret += "if(false){//empty block \n}";

		for (ConfigOption<?> option : options) {
			ret += "else if (optionName.equals(\"" + option.getName()
					+ "\")){\n";
			ret += "" + option.getName() + " = ("
					+ rightType(option.getValueTypeAsJavaString()) + ") "
					+ " entry.getValue();\n}";

		}
		ret += "\n}\n";
		return ret;
	}

	private static String getAllCommentsForOptionList(
			List<ConfigOption<?>> options) {
		String ret = "";
		for (ConfigOption<?> option : options) {
			ret += "* @param " + option.getName() + " "
					+ option.getDescription() + "\n";
		}

		return ret;
	}

	private String makeGetInstanceForConfigurators(String comments) {

		Map<String, String> parametersWithType = new LinkedHashMap<String, String>();
		Map<String, String> parametersNoType = new LinkedHashMap<String, String>();
		String applyConf = "";
		// parametersWithType.put("ComponentManager cm", "cm");
		parametersNoType.put(className + ".class", className + ".class");
		for (String s : additionalMandatoryVars.keySet()) {
			parametersWithType.put(s, additionalMandatoryVars.get(s));
			parametersNoType.put(s, additionalMandatoryVars.get(s));
			comments ="* @param "+additionalMandatoryVars.get(s)+" see "+additionalMandatoryVars.get(s)+"\n"+comments;
		}
		for (String s : mandatoryVars.keySet()) {
			parametersWithType.put(s, mandatoryVars.get(s));
			applyConf += fillApplyConfigEntry("component", mandatoryVars.get(s));
		}

		String parWithType = expandCollection(parametersWithType.keySet(), "",
				", ", 2);

		String par = expandCollection(parametersNoType.values(), "", ", ", 2);

		String exceptions = "";
		if (!getinstanceExceptions.isEmpty()) {
			exceptions += "throws ";
			exceptions += expandCollection(getinstanceExceptions, "", ", ", 2);
			comments+=expandCollection(getinstanceExceptions, "* @throws ", " see \n", 0);
		}
		comments = fillJavaDocComment(comments + "* @return " + className
				+ "\n");
		String ret = comments;
		ret += "public static " + className + " get" + className + "("
				+ parWithType + ") " + exceptions + "{\n";
		ret += className + " component = ComponentManager.getInstance()."
				+ componentType + "(" + par + ");\n";
		ret += applyConf;

		ret += "return component;\n}\n";
				
		return ret;
	}
	
	


	private String makeComponentFactoryMethods(String comments) {

		Map<String, String> parametersWithType = new LinkedHashMap<String, String>();
		Map<String, String> parametersNoType = new LinkedHashMap<String, String>();
		String applyConf = "";
		// parametersWithType.put("ComponentManager cm", "cm");
		parametersNoType.put(className + ".class", className + ".class");
		for (String s : additionalMandatoryVars.keySet()) {
			parametersWithType.put(s, additionalMandatoryVars.get(s));
			parametersNoType.put(s, additionalMandatoryVars.get(s));
			comments += "* @param " + additionalMandatoryVars.get(s) + " see "
					+ capitalize(additionalMandatoryVars.get(s)) + "\n";
		}
		for (String s : mandatoryVars.keySet()) {
			parametersWithType.put(s, mandatoryVars.get(s));
			applyConf += fillApplyConfigEntry("component", mandatoryVars.get(s))
					+ "";
		}

		String parWithType = expandCollection(parametersWithType.keySet(), "",
				", ", 2);

		String par = expandCollection(parametersWithType.values(), "", ", ", 2);

		String exceptions = "";
		if (!getinstanceExceptions.isEmpty()) {
			exceptions += "throws ";
			exceptions += expandCollection(getinstanceExceptions, "", ", ", 2);
			comments += expandCollection(getinstanceExceptions, "* @throws ",
					" see\n", 0);
		}
		comments += "* @return a component ready for initialization "
				+ className + "\n";
		String ret = fillJavaDocComment(comments);
		ret += "public static " + className + " get" + className + "("
				+ parWithType + ") " + exceptions + " {\n" + "return "
				+ className + CONFIGURATOR + ".get" + className + "(" + par
				+ ");\n}\n";
		return ret;
	}

	private static void makeComponentFactory() {
		String ret = fillClassTemplate(TARGET_PACKAGE, expandCollection(
				COMPONENT_FACTORY_IMPORTS, "import ", ";\n", 0),
				COMPONENT_FACTORY, "", expandCollection(
						COMPONENT_FACTORY_METHODS, "", "\n", 0), "final","");

		Files.createFile(new File(TARGET_DIR + "/" + COMPONENT_FACTORY
				+ ".java"), ret);
	}

	private static String fillApplyConfigEntry(String className,
			String optionName) {
		return "ComponentManager.getInstance().applyConfigEntry("
				+ deCapitalize(className) + ", \"" + optionName + "\", "
				+ optionName + ");\n";
	}

	private static String fillConstructorTemplate(String className) {
		return fillJavaDocComment("* @param " + deCapitalize(className)
				+ " see " + className + "\n")
				+ "public "
				+ className
				+ CONFIGURATOR
				+ "("
				+ className
				+ " "
				+ deCapitalize(className)
				+ "){\n"
				+ "this."
				+ deCapitalize(className)
				+ " = "
				+ deCapitalize(className)
				+ ";\n" + "}\n";
	}

	@SuppressWarnings("unused")
	private static String fillVariableDefTemplate(String optionName,
			String type, String defaultValue) {
		return "private " + type + " " + optionName + " = " + defaultValue
				+ ";\n";

	}

	private static String fillJavaDocComment(String lines) {
		return "/**\n" + lines + "**/\n";
	}

	private static String fillGetterTemplate(String className, String comment,
			String optionName, String type) {
		comment = comment.replaceAll("@param ", "");
		comment += "* @return " + checkstyleAdjust(type) + " \n";
		String ret = fillJavaDocComment(comment);
		ret += (type.contains("<String>") || type.contains("<StringTuple>")) ? "@SuppressWarnings(\"unchecked\")\n"
				: "";
		ret += "public " + type + " get" + capitalize(optionName) + "() {\n";
		ret += "return (" + rightType(type)
				+ ") ComponentManager.getInstance().";
		ret += "getConfigOptionValue(" + deCapitalize(className) + ",  \""
				+ optionName + "\") ;\n";
		ret += "}\n";
		return ret;
	}

	private static String fillSetterTemplate(String className, String comment,
			String optionName, String type, boolean reinit) {
		String ret = fillJavaDocComment(comment);
		ret += "public void set" + capitalize(optionName);
		ret += "(" + type + " " + optionName + ") {\n";
		ret += fillApplyConfigEntry(className, optionName);
		ret += (reinit) ? REINITVAR + " = true;\n" : "";
		ret += "}\n";
		return ret;
	}

	
	
	
	private static String fillClassTemplate(String packagE, String imports,
			String className, String extendS,  String body, String classModifier, String implementS) {
		

		String ret = HEADER + "\n";
		ret += "package " + packagE + ";\n\n";
		ret += imports + "\n";
		ret += fillJavaDocComment(CLASS_COMMENT);
//		ret += (INCLUDE_UNUSED) ? UNUSED : "";
		ret += (!extendS.isEmpty()) ? OVERRIDE : "";
		ret += "public "+classModifier+" class " + className + " "
				+ ((extendS.length() > 0) ? " extends " + extendS : "")
				+ ((implementS.length() > 0) ? " implements " + implementS : "")
				+ " {\n\n";
		ret += body + "\n";
		ret += "}\n";
		return ret;

	}
	
	private static void writePackageHTML(){
		String c = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"+
		"<html>"+
		"<head></head>"+
		"<body bgcolor=\"white\">"+
		"<p>Automatically generated classes, which enable programmatically setting"+
		"and getting configuration options of components.</p>"+
		"</body>"+
		"</html>";
		try{
		Files.createFile(new File(TARGET_DIR+File.separator+"package.html"), c);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static String expandCollection(Collection<String> col,
			String before, String after, int removeChars) {
		if (col.isEmpty()){
			return "";
		}
		String ret = "";
		for (String string : col) {
			ret += before + string + after;
		}

		if (removeChars == 0) {
			return ret;
		} else {
			return ret.substring(0, ret.length() - removeChars);
		}
	}

	private static String capitalize(String s) {
		String tmp = s.substring(0, 1);
		return tmp.toUpperCase() + s.substring(1);
	}

	private static String deCapitalize(String s) {
		String tmp = s.substring(0, 1);
		return tmp.toLowerCase() + s.substring(1);
	}

	private static String rightType(String type) {
		if (type.equals("int")){
			return "Integer";
		}else if (type.equals("boolean")){
			return "Boolean";
		}else if (type.equals("double")){
			return "Double";
		}else{
			return type;
		}

	}


	private static String checkstyleAdjust(String type) {
		type = type.replaceAll("<", "(");
		type = type.replaceAll(">", ")");
		return type;
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
