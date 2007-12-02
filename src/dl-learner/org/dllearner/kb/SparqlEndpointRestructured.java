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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.dl.KB;
import org.dllearner.kb.sparql.Manager;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQueryType;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.reasoning.JenaOWLDIGConverter;
import org.dllearner.utilities.Datastructures;

/**
 * Represents a SPARQL Endpoint. 
 * TODO: Is it necessary to create a class DBpediaSparqlEndpoint?
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 */
public class SparqlEndpointRestructured extends KnowledgeSource {
	
	//ConfigOptions
	private URL url;
	private Set<String> instances;
	private URL dumpFile;
	private int numberOfRecursions;
	private int filterMode;
	private Set<String> predList;
	private Set<String> objList;
	private Set<String> classList;
	private String format;
	private boolean dumpToFile;
	private boolean useLits=false;
	
	/**
	 * Holds the results of the calculateSubjects method 
	 */
	private String[] subjects;
	
	/**
	 * Holds the results of the calculateTriples method 
	 */
	private String[] triples;
	
	/**
	 * Holds the results of the calculateConceptSubjects method 
	 */
	private String[] conceptSubjects;
	
	/**
	 * if a method is running this becomes true
	 */
	private boolean subjectThreadRunning=false;
	
	private boolean triplesThreadRunning=false;
	
	private boolean conceptThreadRunning=false;
	
	/**
	 * the Thread that is running a method
	 */
	private Thread subjectThread;
	
	private Thread triplesThread;
	
	private Thread conceptThread;
	
	//received ontology as array, used if format=Array(an element of the
	//array consists of the subject, predicate and object separated by '<'
	private String[] ontArray;
	
	//received ontology as KB, the internal format
	private KB kb;
	
	public static String getName() {
		return "SPARQL Endpoint Restructured";
	}
	
	/**
	 * sets the ConfigOptions for this KnowledgeSource
	 * @return
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("url", "URL of SPARQL Endpoint"));
		options.add(new StringSetConfigOption("instances","relevant instances e.g. positive and negative examples in a learning problem"));
		options.add(new IntegerConfigOption("numberOfRecursions","number of Recursions, the Sparql-Endpoint is asked"));
		options.add(new IntegerConfigOption("filterMode","the mode of the SPARQL Filter"));
		options.add(new StringSetConfigOption("predList","a predicate list"));
		options.add(new StringSetConfigOption("objList","an object list"));
		options.add(new StringSetConfigOption("classList","a class list"));
		options.add(new StringConfigOption("format", "N-TRIPLES or KB format"));
		options.add(new BooleanConfigOption("dumpToFile", "wether Ontology from DBPedia is written to a file or not"));
		options.add(new BooleanConfigOption("useLits","use Literals in SPARQL query"));
		return options;
	}

	/*
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({"unchecked"})
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String option = entry.getOptionName();
		if (option.equals("url")) {
			String s = (String) entry.getValue();
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {
				throw new InvalidConfigOptionValueException(entry.getOption(), entry.getValue(),"malformed URL " + s);
			}
		} else if(option.equals("instances")) {
			instances = (Set<String>) entry.getValue();
		} else if(option.equals("numberOfRecursions")){
			numberOfRecursions=(Integer)entry.getValue();
		} else if(option.equals("predList")) {
			predList = (Set<String>) entry.getValue();
		} else if(option.equals("objList")) {
			objList = (Set<String>) entry.getValue();
		} else if(option.equals("classList")) {
			classList = (Set<String>) entry.getValue();
		} else if(option.equals("filterMode")){
			filterMode=(Integer)entry.getValue();
		} else if(option.equals("format")){
			format=(String)entry.getValue();
		} else if(option.equals("dumpToFile")){
			dumpToFile=(Boolean)entry.getValue();
		} else if(option.equals("useLits")){
			useLits=(Boolean)entry.getValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		System.out.println("SparqlModul: Collecting Ontology");
		//SparqlOntologyCollector oc=
			
			
			//new SparqlOntologyCollector(Datastructures.setToArray(instances), numberOfRecursions, filterMode,
				//Datastructures.setToArray(predList),Datastructures.setToArray( objList),Datastructures.setToArray(classList),format,url,useLits);
		Manager m=new Manager();
		if(filterMode==0){
				try{
				m.usePredefinedConfiguration(new URI("http://www.extraction.org/config#dbpediatest"));
				}catch (Exception e) {e.printStackTrace();}
			}
		else{
		//SparqlQueryType sqt=new SparqlQueryType("forbid", objList,predList,useLits+"");
		//SparqlEndpoint se=new SparqlEndpoint();
		//m.useConfiguration(SparqlQueryType, SparqlEndpoint)
		}
		try
		{
			String ont=m.extract(instances);
				
			if (dumpToFile){
				String filename=System.currentTimeMillis()+".nt";
				String basedir="cache"+File.separator;
				try{
					if(!new File(basedir).exists())
						new File(basedir).mkdir();
					
					FileWriter fw=new FileWriter(new File(basedir+filename),true);
					fw.write(ont);
					fw.flush();
					fw.close();
												
					dumpFile=(new File(basedir+filename)).toURI().toURL();
				}catch (Exception e) {e.printStackTrace();}
			}
			if (format.equals("KB")) {
				try{
					kb=KBParser.parseKBFile(new StringReader(ont));
				} catch(Exception e) {e.printStackTrace();}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("SparqlModul: ****Finished");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.KnowledgeSource#toDIG()
	 */
	@Override
	public String toDIG(URI kbURI) {
		if (format.equals("N-TRIPLES")) return JenaOWLDIGConverter.getTellsString(dumpFile, OntologyFormat.N_TRIPLES, kbURI);
		else return DIGConverter.getDIGString(kb, kbURI).toString();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.KnowledgeSource#export(java.io.File, org.dllearner.core.OntologyFormat)
	 */
	@Override
	public void export(File file, OntologyFormat format) throws OntologyFormatUnsupportedException {
		// currently no export functions implemented, so we just throw an exception
		throw new OntologyFormatUnsupportedException("export", format);
	}
	
	public URL getURL() {
		return url;
	}
	
	public String[] getOntArray() {
		return ontArray;
	}
	
	public void calculateSubjects(String label,int limit)
	{
		System.out.println("SparqlModul: Collecting Subjects");
		SparqlOntologyCollector oc=new SparqlOntologyCollector(url);
		try{
			subjects=oc.getSubjectsFromLabel(label,limit);
		}catch (IOException e){
			subjects=new String[1];
			subjects[0]="[Error]Sparql Endpoint could not be reached.";
		}
		System.out.println("SparqlModul: ****Finished");
	}
	
	public void calculateTriples(String subject) {
		System.out.println("SparqlModul: Collecting Triples");
		SparqlOntologyCollector oc=new SparqlOntologyCollector(url);
		try{
			triples=oc.collectTriples(subject);
		}catch (IOException e){
			triples=new String[1];
			triples[0]="[Error]Sparql Endpoint could not be reached.";
		}
		System.out.println("SparqlModul: ****Finished");
	}
	
	public void calculateConceptSubjects(String concept)
	{
		System.out.println("SparqlModul: Collecting Subjects");
		SparqlOntologyCollector oc=new SparqlOntologyCollector(url);
		try{
			conceptSubjects=oc.getSubjectsFromConcept(concept);
		}catch (IOException e){
			conceptSubjects=new String[1];
			conceptSubjects[0]="[Error]Sparql Endpoint could not be reached.";
		}
		System.out.println("SparqlModul: ****Finished");
	}
	
	public boolean subjectThreadIsRunning()
	{
		return subjectThreadRunning;
	}
	
	public void setSubjectThreadRunning(boolean bool)
	{
		subjectThreadRunning=bool;
	}
	
	public boolean triplesThreadIsRunning()
	{
		return triplesThreadRunning;
	}
	
	public void setTriplesThreadRunning(boolean bool)
	{
		triplesThreadRunning=bool;
	}
	
	public boolean conceptThreadIsRunning()
	{
		return conceptThreadRunning;
	}
	
	public void setConceptThreadRunning(boolean bool)
	{
		conceptThreadRunning=bool;
	}
	
	public String[] getSubjects()
	{
		return subjects;
	}

	public Thread getSubjectThread() {
		return subjectThread;
	}

	public void setSubjectThread(Thread subjectThread) {
		this.subjectThread = subjectThread;
	}

	public Thread getTriplesThread() {
		return triplesThread;
	}

	public void setTriplesThread(Thread triplesThread) {
		this.triplesThread = triplesThread;
	}

	public Thread getConceptThread() {
		return conceptThread;
	}

	public void setConceptThread(Thread conceptThread) {
		this.conceptThread = conceptThread;
	}

	public String[] getTriples()
	{
		return triples;
	}
	
	public String[] getConceptSubjects()
	{
		return conceptSubjects;
	}
}
