/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.dllearner.kb.sparql.SparqlQueryMaker;


/**
 * This class collects the ontology from dbpedia,
 * everything is saved in hashsets, so the doublettes are taken care of
 * 
 * 
 * @author Sebastian Hellmann
 * @author Sebastian Knappe
 *
 */
public class SparqlOntologyCollector {

	boolean print_flag=false;
	SparqlQueryMaker queryMaker;
	SparqlCache cache;
	URL url;
	SparqlFilter sf;
	String[] subjectList;
	int numberOfRecursions;
	HashSet<String> properties;
	HashSet<String> classes;
	HashSet<String> instances;
	HashSet<String> triples;
	String format;
		
	// some namespaces
	String subclass="http://www.w3.org/2000/01/rdf-schema#subClassOf";
	String type="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	String objectProperty="http://www.w3.org/2002/07/owl#ObjectProperty";
	String classns="http://www.w3.org/2002/07/owl#Class";
	String thing="http://www.w3.org/2002/07/owl#Thing";
	
	
	String[] defaultClasses={
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Category:",
			"http://dbpedia.org/resource/Template:",
			"http://www.w3.org/2004/02/skos/core",
			"http://dbpedia.org/class/"};
	
	
	/**
	 * 
	 * 
	 * @param subjectList
	 * @param numberOfRecursions
	 * @param filterMode
	 * @param FilterPredList
	 * @param FilterObjList
	 * @param defClasses
	 */
	public SparqlOntologyCollector(String[] subjectList,int numberOfRecursions,
			int filterMode, String[] FilterPredList,String[] FilterObjList,String[] defClasses, String format, URL url, boolean useLits){
		this.subjectList=subjectList;
		this.numberOfRecursions=numberOfRecursions;
		this.format=format;
		//this.queryMaker=new SparqlQueryMaker();
		this.cache=new SparqlCache("cache");
		if(defClasses!=null && defClasses.length>0 ){
			this.defaultClasses=defClasses;
		}		
		try{
			this.sf=new SparqlFilter(filterMode,FilterPredList,FilterObjList,useLits);
			this.url=url;
			this.properties=new HashSet<String>();
			this.classes=new HashSet<String>();
			this.instances=new HashSet<String>();
			this.triples=new HashSet<String>();
		}catch (Exception e) {e.printStackTrace();}
		
	}
	
	public SparqlOntologyCollector(URL url)
	{
		// this.queryMaker=new SparqlQueryMaker();
		this.cache=new SparqlCache("cache");
		this.url=url;
	}
	
	/**
	 * first collects the ontology 
	 * then types everything so it becomes owl-dl
	 * 
	 * @return all triples in n-triple format
	 */
	public String collectOntology() throws IOException{
		getRecursiveList(subjectList, numberOfRecursions);
		finalize();
		String ret="";
		for (Iterator<String> iter = triples.iterator(); iter.hasNext();) {
			ret += iter.next();
		}	
		return ret;
	}
	
	public String[] collectTriples(String subject) throws IOException{
		System.out.println("Searching for Article: "+subject);
		String sparql=SparqlQueryMaker.makeArticleQuery(subject);
		String fromCache=cache.get(subject, sparql);
		String xml;
		// if not in cache get it from dbpedia
		if(fromCache==null){
			xml=sendAndReceive(sparql);
			cache.put(subject, xml, sparql);
			System.out.print("\n");
		}
		else{
			xml=fromCache;
			System.out.println("FROM CACHE");
		}
		
		return processArticle(xml);
	}
	
	public String[] processArticle(String xml)
	{
		Vector<String> vec=new Vector<String>();
		String one="<binding name=\"predicate\"><uri>";
		String two="<binding name=\"object\">";
		String end="</uri></binding>";
		String predtmp="";
		String objtmp="";
		// ArrayList<String> al=new ArrayList<String>();
		while(xml.indexOf(one)!=-1){
			//get pred
			xml=xml.substring(xml.indexOf(one)+one.length());
			predtmp=xml.substring(0,xml.indexOf(end));
			//getobj
			xml=xml.substring(xml.indexOf(two)+two.length());
			if (xml.startsWith("<literal xml:lang=\"en\">")){
				xml=xml.substring(xml.indexOf(">")+1);
				objtmp=xml.substring(0,xml.indexOf("</literal>"));
			}
			else if (xml.startsWith("<uri>")) objtmp=xml.substring(5,xml.indexOf(end));
			else continue;
			
			System.out.println("Pred: "+predtmp+" Obj: "+objtmp);			
			
			vec.add(predtmp+"<"+objtmp);
		}
		
		String[] ret=new String[vec.size()];
		return vec.toArray(ret);
	}
	
	public String[] getSubjectsFromLabel(String label, int limit) throws IOException{
		System.out.println("Searching for Label: "+label);
		String sparql=SparqlQueryMaker.makeLabelQuery(label,limit);
		String FromCache=cache.get(label, sparql);
		String xml;
		// if not in cache get it from dbpedia
		if(FromCache==null){
			xml=sendAndReceive(sparql);
			cache.put(label, xml, sparql);
			System.out.print("\n");
		}
		else{
			xml=FromCache;
			System.out.println("FROM CACHE");
		}
		
		return processSubjects(xml);
	}
	
	public String[] getSubjectsFromConcept(String concept) throws IOException
	{
		System.out.println("Searching for Subjects of type: "+concept);
		String sparql=SparqlQueryMaker.makeConceptQuery(concept);
		String FromCache=cache.get(concept, sparql);
		String xml;
		// if not in cache get it from dbpedia
		if(FromCache==null){
			xml=sendAndReceive(sparql);
			cache.put(concept, xml, sparql);
			System.out.print("\n");
		}
		else{
			xml=FromCache;
			System.out.println("FROM CACHE");
		}
		
		return processSubjects(xml);
	}
	
	/**
	 * calls getRecursive for each subject in list
	 * @param subjects
	 * @param NumberofRecursions
	 */
	public void getRecursiveList(String[] subjects,int NumberofRecursions) throws IOException{
		for (int i = 0; i < subjects.length; i++) {
			getRecursive(subjects[i], NumberofRecursions);	
		}	
	}
	
	/**
	 * gets all triples until numberofrecursion-- gets 0
	 * 
	 * @param StartingSubject
	 * @param NumberofRecursions
	 */
	public void getRecursive(String StartingSubject,int NumberofRecursions) throws IOException{
		System.out.print("SparqlModul: Depth: "+NumberofRecursions+" @ "+StartingSubject+" ");
		if(NumberofRecursions<=0)
			return;
		else {NumberofRecursions--;}
		
		String sparql=SparqlQueryMaker.makeQueryFilter(StartingSubject,this.sf);
		// checks cache
		String FromCache=cache.get(StartingSubject, sparql);
		String xml;
		// if not in cache get it from dbpedia
		if(FromCache==null){
			xml=sendAndReceive(sparql);
			cache.put(StartingSubject, xml, sparql);
			System.out.print("\n");
		}
		else{
			xml=FromCache;
			System.out.println("FROM CACHE");
		}
		
		// get new Subjects
		String[] newSubjects=processResult(StartingSubject,xml);
			
		for (int i = 0; (i < newSubjects.length)&& NumberofRecursions!=0; i++) {
			getRecursive(newSubjects[i], NumberofRecursions);
		}
	}
	
	/**
	 * process the sparql result xml in a simple manner
	 * 
	 * 
	 * @param subject
	 * @param xml
	 * @return list of new individuals
	 */
	public  String[] processResult(String subject,String xml){
		//TODO if result is empty, catch exceptions
		String one="<binding name=\"predicate\"><uri>";
		String two="<binding name=\"object\">";
		String end="</uri></binding>";
		String predtmp="";
		String objtmp="";
		ArrayList<String> al=new ArrayList<String>();
		while(xml.indexOf(one)!=-1){
			//get pred
			xml=xml.substring(xml.indexOf(one)+one.length());
			predtmp=xml.substring(0,xml.indexOf(end));
			//getobj
			xml=xml.substring(xml.indexOf(two)+two.length());
			if (xml.startsWith("<literal xml:lang=\"en\">")){
				xml=xml.substring(xml.indexOf(">")+1);
				objtmp=xml.substring(0,xml.indexOf("</literal>"));
			}
			else if (xml.startsWith("<uri>")) objtmp=xml.substring(5,xml.indexOf(end));
			else continue;
			
			System.out.println("Pred: "+predtmp+" Obj: "+objtmp);			
			// writes the triples and resources in the hashsets
			// also fills the arraylist al
			processTriples(subject, predtmp, objtmp,al);
			//System.out.println(al.size());
			
		}
		
		// convert al to list
		Object[] o=al.toArray();
		String[] ret=new String[o.length];
		for (int i = 0; i < o.length; i++) {
			ret[i]=(String)o[i];
		}
		return ret;
	}
		
	
	
	/**
	 * 
	* writes the triples and resources in the hashsets
	* also fills the arraylist al with new individals for further processing
	 * @param s
	 * @param p
	 * @param o
	 * @param al
	 */
	public void processTriples(String s,String p, String o,ArrayList<String> al){
		// the next two lines bump out some inconsistencies within dbpedia
		String t="/Category";
		if(s.equals(t) || o.equals(t))return ;
		
		if(sf.mode==2)
		{
			if(  o.startsWith("http://dbpedia.org/resource/Category:")
					&& 
				!p.startsWith("http://www.w3.org/2004/02/skos/core"))
				return;
			if(p.equals("http://www.w3.org/2004/02/skos/core#broader")){
				p=subclass;
			}
			else if(p.equals("http://www.w3.org/2004/02/skos/core#subject")){
				p=type;
			}
			else {}
		}
			
		//save for further processing
		al.add(o);
			
		// type classes
		if(isClass(o)){
			classes.add(o);
			if(isClass(s))p=subclass;
			else p=type;
		}
		else {
			instances.add(o);
			this.properties.add(p);
		}
				
		//maketriples
		try{
			this.triples.add(makeTriples(s, p, o));
			//fw.write(makeTriples(subject, predtmp, objtmp));
		}catch (Exception e) {e.printStackTrace();}
			
			
		return;
	}
	
	private String[] processSubjects(String xml){
		Vector<String> vec=new Vector<String>();
		String one="<binding name=\"subject\"><uri>";
		String end="</uri></binding>";
		String subject="";
		while(xml.indexOf(one)!=-1){
			//get subject
			xml=xml.substring(xml.indexOf(one)+one.length());
			subject=xml.substring(0,xml.indexOf(end));
							
			System.out.println("Subject: "+subject);
			vec.addElement(subject);
		}	
		String[] a=new String[vec.size()];
		return vec.toArray(a);
	}
	
	/**
	 * also makes subclass property between classes
	 * 
	 * @param s
	 * @param p
	 * @param o
	 * @return triple in the n triple notation
	 */
	public String makeTriples(String s,String p, String o){
		String ret="";
		if (format.equals("N-TRIPLES")) ret="<"+s+"> <"+p+"> <"+o+">.\n";
		else if (format.equals("KB")){
			if (p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) ret="\""+o+"\"(\""+s+"\").\n"; 
			else ret="\""+p+"\"(\""+s+"\",\""+o+"\").\n";
		}
		else if (format.equals("Array"))
		{
			ret=s+"<"+p+"<"+o+"\n";
		}
		return ret;
	}
		
	/**
	 * decides if an object is treated as a class
	 * 
	 * @param obj
	 * @return true if obj is in the defaultClassesList
	 */
	public boolean isClass(String obj){
		
		boolean retval=false;
		for (String defclass : defaultClasses) {
			if(obj.contains(defclass))retval=true;
		}
		return retval;
	}
		
	
	/** 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize(){
		typeProperties();
		typeClasses();
		typeInstances();
	}
		
	public void typeProperties(){
		String rdfns="http://www.w3.org/1999/02/22-rdf-syntax-ns";
		String owlns="http://www.w3.org/2002/07/owl";
		Iterator<String> it=properties.iterator();
		String current="";
		while (it.hasNext()){
			try{
			current=it.next();
			if(current.equals(subclass))continue;
			if(current.contains(rdfns)||current.contains(owlns)){/*DO NOTHING*/}
			else {this.triples.add(makeTriples(current,type,objectProperty));}
			}catch (Exception e) {}

		}
	}
	
	public void typeClasses(){ 
		Iterator<String> it=classes.iterator();
		String current="";
		while (it.hasNext()){
			try{
			current=it.next();
			this.triples.add(makeTriples(current,type,classns));
			}catch (Exception e) {}
		}
	}
	
	public void typeInstances(){
		Iterator<String> it=instances.iterator();
		String current="";
		while (it.hasNext()){
			try{
				current=it.next();
				this.triples.add(makeTriples(current,type,thing));
			}catch (Exception e) {}
		}
	}
	//TODO alles dbpedia-spezifische rausbekommen	
	private String sendAndReceive(String sparql) throws IOException{
		StringBuilder answer = new StringBuilder();	
		
		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
			
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
							
		connection.addRequestProperty("Host", "dbpedia.openlinksw.com");
		connection.addRequestProperty("Connection","close");
		connection.addRequestProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language","de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset","utf-8;q=1.0");
		connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");
				
		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		osw.write("default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
			URLEncoder.encode(sparql, "UTF-8")+
			"&format=application%2Fsparql-results%2Bxml");
			osw.close();
				
		// receive answer
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
			
		String line;
		do {
			line = br.readLine();
			if(line!=null)
				answer.append(line);
		} while (line != null);
			
		br.close();
				
		return answer.toString();
	}
}