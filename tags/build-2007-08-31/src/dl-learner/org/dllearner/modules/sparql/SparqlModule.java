package org.dllearner.modules.sparql;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.ConfigurationOption;
import org.dllearner.Main;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;
import org.dllearner.modules.PreprocessingModule;


public class SparqlModule implements PreprocessingModule {
	
	FileWriter fw;
	
	//HashSet<String> all;// remove after cache is here
	String[] FilterPredList=null;
	String[] FilterObjList=null;
	
	
			
public String getModuleName(){
	return "Sparql Module v0.3";
}
	
	public void preprocess(KB kb,
			Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfigurationOption> confOptions,
			List<List<String>> functionCalls, String baseDir,
			boolean useQueryMode) {
		
		
		
		//temporary file
		String filename=System.currentTimeMillis()+".nt";
		
		// add filename
		ArrayList<String> al=new ArrayList<String>();
		al.add("import");al.add(filename);al.add( "N-TRIPLES");
		functionCalls.add(al);
		 
		// get options hidePrefix and recursion
		int numberOfRecursions=0;
		String prefix="";
		int filterMode=-1;
		Set<String> predList=null; 
		Set<String> objList=null; 
		Set<String> classList=null; 
		//boolean useLiterals=false;
		
		System.out.println("SparqlModul: Processing Options");
		Main.getConfMgr().addStringOption("preprocessingModule", new String[] {}); 
			
		for (int i = 0; i < confOptions.size(); i++) {
			if(confOptions.get(i).getOption().equals("hidePrefix")){
				prefix=confOptions.get(i).getStrValue();
			}
			//sparqlModule options
			if(confOptions.get(i).getOption().equals("sparqlModule")){
				if(confOptions.get(i).getSubOption().equals("numberOfRecursion")){
					numberOfRecursions=confOptions.get(i).getIntValue();
					Main.getConfMgr().addIntegerOption("sparqlModule.numberOfRecursion", new Integer[] { 1, 3 });
				}
				if(confOptions.get(i).getSubOption().equals("filterMode")){
					filterMode=confOptions.get(i).getIntValue();
					Main.getConfMgr().addIntegerOption("sparqlModule.filterMode", new Integer[] { 0, 2 });
				}
				if(confOptions.get(i).getSubOption().equals("sparqlPredicateFilterList")){
					predList=confOptions.get(i).getSetValues();
					Main.getConfMgr().addSetOption("sparqlModule.sparqlPredicateFilterList");
				}
				if(confOptions.get(i).getSubOption().equals("sparqlObjectFilterList")){
					objList=confOptions.get(i).getSetValues();
					Main.getConfMgr().addSetOption("sparqlModule.sparqlObjectFilterList");
				}
				if(confOptions.get(i).getSubOption().equals("classList")){
					classList=confOptions.get(i).getSetValues();
					Main.getConfMgr().addSetOption("sparqlModule.classList");
				}
				if(confOptions.get(i).getSubOption().equals("useLiterals")){
					//useLiterals=confOptions.get(i).;
					
				}
			}
		}// end for
		System.out.println("SparqlModul: Processing finished");
		// subject for which information is drafted from wikipedia
		String[] subjectList=makeSubjectList(prefix, positiveExamples, negativeExamples);
		

		try{
			this.fw=new FileWriter(new File(baseDir+File.separator+filename),true);
			System.out.println("SparqlModul: Collecting Ontology");
			OntologyCollector oc=new OntologyCollector(subjectList, numberOfRecursions,
					 filterMode,  Util.setToArray(predList),Util.setToArray( objList),Util.setToArray(classList));
			
			String ont=oc.collectOntology();
			fw.write(ont);
			fw.flush();
			//.getRecursiveList(subjectList,numberOfRecursions);
			//type classes and properties
			//this.finalize();
			
			System.out.println("SparqlModul: ****Finished");
			//System.out.println(yago);
			//System.out.println(subjectList.length);
			//System.out.println(subjectList);
			
			
			this.fw.close();
			//System.exit(0);
			}catch (Exception e) {e.printStackTrace();}	
	
	}
	
	
	String[] makeSubjectList(String prefix,
			Map<AtomicConcept, SortedSet<Individual>> positive,
			Map<AtomicConcept, SortedSet<Individual>> negative){
		
		//prefix
		prefix="";
		
		ArrayList<String> al=new ArrayList<String>();
		Iterator<AtomicConcept> it=positive.keySet().iterator();
		while(it.hasNext()){
			SortedSet<Individual> s=positive.get(it.next());
			Iterator<Individual> inner =s.iterator();
			while(inner.hasNext()){
				al.add(inner.next().toString());
			}
		}

		it=negative.keySet().iterator();
		while(it.hasNext()){
			SortedSet<Individual> s=negative.get(it.next());
			Iterator<Individual> inner =s.iterator();
			while(inner.hasNext()){
				al.add(inner.next().toString());
			}
		}
		String[] subjectList=new String[al.size()];
		Object[] o=al.toArray();
		for (int i = 0; i < subjectList.length; i++) {
			subjectList[i]=prefix+(String)o[i];
		}
		return subjectList;
	}

	public SparqlModule() {
		
		
	}
	
	
	
	
	
	
	

	
}