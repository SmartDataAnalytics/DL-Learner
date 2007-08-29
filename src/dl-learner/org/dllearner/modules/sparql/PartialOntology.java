package org.dllearner.modules.sparql;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.ConfigurationOption;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;
import org.dllearner.modules.PreprocessingModule;


public class PartialOntology implements PreprocessingModule {
	SimpleHTTPRequest s;
	QueryMaker q;
	Cache c;
	InetAddress ia;
	FileWriter fw;
	HashSet<String> properties;
	HashSet<String> classes;
	HashSet<String> instances;
	//HashSet<String> all;// remove after cache is here
	String[] FilterPredList=null;
	String[] FilterObjList=null;
	
	String[] defaultClasses={
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Category:",
			"http://www.w3.org/2004/02/skos/core",
			"http://dbpedia.org/class/"}; //TODO FEHLER hier fehlt yago
			
	
	public String getModuleName(){
		return "SparqlModule";
	}
	
	public void preprocess(KB kb,
			Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfigurationOption> confOptions,
			List<List<String>> functionCalls, String baseDir,
			boolean useQueryMode) {
		
		String filename=System.currentTimeMillis()+".nt";
		ArrayList<String> al=new ArrayList<String>();
		//int numberOfRecursions=3;
		String prefix="";
		al.add("import");al.add(filename);al.add( "N-TRIPLES");
		functionCalls.add(al);
		 al=new ArrayList<String>();
		 
		 for (int i = 0; i < confOptions.size(); i++) {
				if(confOptions.get(i).getOption().equals("hidePrefix")){
					prefix=confOptions.get(i).getStrValue();
				}
				System.out.println(confOptions.get(i).getOption());
				if(confOptions.get(i).getOption().equals("gp"))
					System.out.println(confOptions.get(i).getIntValue()+"AAAAAAAAAAAAAAAAAAAA");
			}
		 //System.out.println(confOptions);
		//Iterator it=positiveExamples.keySet().iterator();
		addMapToArrayList(al,positiveExamples);
		addMapToArrayList(al,negativeExamples);
		String[] subjectList=new String[al.size()];
		Object[] o=al.toArray();
		for (int i = 0; i < subjectList.length; i++) {
			subjectList[i]=prefix+(String)o[i];
		}
		
		
			
		
		try{
			this.fw=new FileWriter(new File(baseDir+File.separator+filename),true);
			
			//this.getRecursiveList(subjectList,numberOfRecursions);
			//this.finalize();
			
			System.out.println("****Finished");
			
			/*System.out.println(this.classes);
			System.out.println(this.properties);
			System.out.println(this.instances);
			System.out.println();*/
			
			/*for (String s : subjectList) {
				//System.out.println("+test(\""+s+"\").");
			}
			*/
			this.fw.close();
			}catch (Exception e) {e.printStackTrace();}	
		
		
		//System.out.println(positiveExamples);
		//System.out.println(functionCalls);
		//System.out.println(confOptions);
		
//		System.out.println(baseDir);

	}
	
	void addMapToArrayList(ArrayList<String> al, Map<AtomicConcept, SortedSet<Individual>> m){
		Iterator<AtomicConcept> it=m.keySet().iterator();
		while(it.hasNext()){
			SortedSet<Individual> s=m.get(it.next());
			Iterator<Individual> inner =s.iterator();
			while(inner.hasNext()){
				al.add(inner.next().toString());
			}
		}
		
	}

	
	
	/*public static void main(String[] args){
		try{
		int numberOfRecursions=1;
				
		/*String[] subjectList={
				"http://dbpedia.org/resource/Adolf_Hitler",
				"http://dbpedia.org/resource/Prince_Chlodwig_zu_HohenloheSchillingsf%C3%BCrst",
				"http://dbpedia.org/resource/Prince_Maximilian_of_Baden",
				"http://dbpedia.org/resource/Franz_von_Papen",
				"http://dbpedia.org/resource/Joseph_Goebbels",
				"http://dbpedia.org/resource/Gerhard_Schr%C3%B6der",
				"http://dbpedia.org/resource/Angela_Merkel",
				"http://dbpedia.org/resource/Helmut_Kohl",
				"http://dbpedia.org/resource/Helmut_Schmidt",
				"http://dbpedia.org/resource/Ludwig_Erhard",
				"http://dbpedia.org/resource/Willy_Brandt"
				};*/
		
		/*String[] subjectList=args;
	
		
		
		
		
		SparqlModule sm=new SparqlModule();
		
		sm.getRecursiveList(subjectList,numberOfRecursions);
		
		
		sm.finalize();
		
		System.out.println("****Finished preprocessing");
		//System.out.println(sm.classes);
		//System.out.println(sm.properties);
		//System.out.println(sm.instances);
		//System.out.println();
		
		for (String s : subjectList) {
			System.out.println("+test(\""+s+"\").");
		}
		
		sm.fw.close();
		}catch (Exception e) {e.printStackTrace();}
		
	}*/
	
	
	public PartialOntology() {
		this.s=new SimpleHTTPRequest();
		this.q=new QueryMaker();
		this.c=new Cache("cache");
		try{
		this.ia=InetAddress.getByName("dbpedia.openlinksw.com");
		//this.fw=new FileWriter(new File(System.currentTimeMillis()+".nt"),true);
		this.properties=new HashSet<String>();
		this.classes=new HashSet<String>();
		this.instances=new HashSet<String>();
		//this.all=new HashSet<String>();
		
		}catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	public void getRecursiveList(String[] subjects,int NumberofRecursions){
		for (int i = 0; i < subjects.length; i++) {
			getRecursive(subjects[i], NumberofRecursions);
			
		}
		
	}
	
	public void getRecursive(String StartingSubject,int NumberofRecursions){
		System.out.print("Tiefe: "+NumberofRecursions+" @ "+StartingSubject+" ");
		if(NumberofRecursions<=0)
			{	return;
			}
		else {NumberofRecursions--;}
		//System.out.println(NumberofRecursions);
		try{
	
		String sparql=q.makeQueryFilter(StartingSubject, new SparqlFilter(0,null,null));
		String FromCache=c.get(StartingSubject, sparql);
		String xml;
		if(FromCache==null){
			xml=s.sendAndReceive(ia, 8890, sparql);
			c.put(StartingSubject, xml, sparql);
			System.out.print("\n");
			}
		else{
			xml=FromCache;
			System.out.println("FROM CACHE");
		}
		
		String[] newSubjects=processResult(StartingSubject,xml);
		
		for (int i = 0; (i < newSubjects.length)&& NumberofRecursions!=0; i++) {
			getRecursive(newSubjects[i], NumberofRecursions);
		}
		
		//System.out.println(xml);
		}catch (Exception e) {e.printStackTrace();}
		
	}
	
	public  String[] processResult(String subject,String xml){
		//TODO if result is empty, catch exceptions
		String one="<binding name=\"predicate\"><uri>";
		String two="<binding name=\"object\"><uri>";
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
			objtmp=xml.substring(0,xml.indexOf(end));
			
			//save for further processing
			al.add(objtmp);
			this.properties.add(predtmp);
			if(isClass(objtmp))classes.add(objtmp);
			else instances.add(objtmp);
			
			//maketriples
			try{
			fw.write(makeTriples(subject, predtmp, objtmp));
			}catch (Exception e) {e.printStackTrace();}
			//System.out.println(predtmp);
			//System.out.println(objtmp);
			//xml=xml.substring(xml.indexOf(one)+one.length());
		}
		
		Object[] o=al.toArray();
		String[] ret=new String[o.length];
		for (int i = 0; i < o.length; i++) {
			ret[i]=(String)o[i];
		}
		return ret;
		//return (String[])al.toArray();
		//System.out.println(xml);
	}
	
	public String makeTriples(String s,String p, String o){
		//this.properties.add(p);
		String subclass="http://www.w3.org/2000/01/rdf-schema#subClassOf";
				
		String ret="";
		if(isClass(s))ret="<"+s+"> <"+subclass+"> <"+o+">.\n";
		else ret="<"+s+"> <"+p+"> <"+o+">.\n";
		
		
		return ret;
	}
	
	public String makeTriplesNoSub(String s,String p, String o){
		//this.properties.add(p);
		
				
		String ret="";
		ret="<"+s+"> <"+p+"> <"+o+">.\n";
		
		
		return ret;
	}
	
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
		String p="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String o="http://www.w3.org/2002/07/owl#ObjectProperty";
		
		String current="";
		while (it.hasNext()){
			try{
			current=it.next();
			if(current.contains(rdfns)||current.contains(owlns)){/*DO NOTHING*/}
			else {this.fw.write(makeTriples(current,p,o));}
			}catch (Exception e) {}

		}
		
		
	}
	public void typeClasses(){ 
		Iterator<String> it=classes.iterator();
		String p="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String o="http://www.w3.org/2002/07/owl#Class";
		
		String current="";
		while (it.hasNext()){
			try{
			current=it.next();
			this.fw.write(makeTriplesNoSub(current,p,o));
			}catch (Exception e) {}

		}
		
		
	}
	public void typeInstances(){
		Iterator<String>it=instances.iterator();
		String p="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		//String o1="http://www.w3.org/2002/07/owl#Class";
		String o2="http://www.w3.org/2002/07/owl#Thing";
		String current="";
		while (it.hasNext()){
			try{
			current=it.next();
			//fw.write(makeTriples(current,p,o1));
			
			this.fw.write(makeTriples(current,p,o2));
			}catch (Exception e) {}
		}
		
		
	}
	
	public boolean isClass(String obj){
		
		boolean retval=false;
		for (String defclass : defaultClasses) {
			if(obj.contains(defclass))retval=true;
		}
		return retval;
	}
	

	
	public void printHashSet(HashSet<String> h){
		Iterator<String> it=h.iterator();
		String current="";
		while (it.hasNext()){
			current=it.next();
			
			if(current.contains("http://dbpedia.org/resource/"))System.out.println("test(\""+current+"\").");
			}
	}
	
	
	
	

	
}
