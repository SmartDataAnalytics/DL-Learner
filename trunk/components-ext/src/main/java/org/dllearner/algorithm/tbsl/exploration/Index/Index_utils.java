package org.dllearner.algorithm.tbsl.exploration.Index;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;


public class Index_utils {

	/**
	 *  
	 * @param string
	 * @param fall 1=Property, 0=Resource, 2=OntologyClass/Yago, 2=resource+yago+ontlogy
	 * @return ArrayList with possible URIs gotten from the Index
	 * @throws SQLException 
	 */
	public static ArrayList<String> searchIndex(String string, int fall, SQLiteIndex myindex) throws SQLException{
		
        
		string=string.replace("_", " ");
		string=string.replace("-", " ");
		string=string.replace(".", " ");
		String result=null;
		String tmp1=null;
		String tmp2 = null;
		ArrayList<String> result_List = new ArrayList<String>();
		
		if(fall==0 || fall==3){
			
			result=myindex.getResourceURI(string.toLowerCase());
			result_List.add(result);

		}
		if(fall==2||fall==3){
			
			tmp1=myindex.getontologyClassURI(string.toLowerCase());
			tmp2=myindex.getYagoURI(string.toLowerCase());
			if(tmp1!=null) result_List.add(tmp1);
			if(tmp2!=null) result_List.add(tmp2);
			//result_List.add("www.TEST.de");
		}


		if(fall==1){
			tmp1=myindex.getPropertyURI(string.toLowerCase());
			tmp2=myindex.getontologyURI(string.toLowerCase());
			if(tmp1!=null) result_List.add(tmp1);
			if(tmp2!=null) result_List.add(tmp2);
			
		}
		
		return result_List;
	}
	
	

public static ArrayList<String> searchIndexForResource(String string, SQLiteIndex myindex) throws SQLException{
	HashMap<String,Float> hm = new HashMap<String,Float>();
string=string.replace("_", " ");
		string=string.replace("-", " ");
		string=string.replace(".", " ");
		String result=null;
		ArrayList<String> result_List = new ArrayList<String>();
		result=myindex.getResourceURI(string.toLowerCase());
		if(result!=null){
			result_List.add(result);
		}
		else{
			
			
			ArrayList<String> tmp_List = new ArrayList<String>();
			String[] array_tmp= string.split(" ");
			
			if(array_tmp.length>1){
				tmp_List=myindex.getListOfUriSpecialIndex(string);
			}
			if(tmp_List!=null)for(String st : tmp_List)result_List.add(st);
			
		}
		
		
		return result_List;
	}

public static ArrayList<String> searchIndexForProperty(String string, SQLiteIndex myindex) throws SQLException{
	HashMap<String,Float> hm = new HashMap<String,Float>();
	if(Setting.isDebugModus())DebugMode.debugPrint("######\n In search Index for Property");

    // adding or set elements in Map by put method key and value pair
    /*
     * 
     * // autoboxing takes care of that.  
map.put(23, 2.5f);  
map.put(64, 4.83f);  
     */
	
	string=string.replace("_", " ");
	string=string.replace("-", " ");
	string=string.replace(".", " ");
	String result=null;
	String result2 = null;
	ArrayList<String> result_List = new ArrayList<String>();
	
	if(string.substring(string.length()-1).contains("s")){
		String neuer_string = string.substring(0, string.length() -1);
		result=myindex.getPropertyURI(neuer_string.toLowerCase());
		result2=myindex.getontologyURI(neuer_string.toLowerCase());
		//tmp2=myindex.getYagoURI(neuer_string.toLowerCase());
		if(result2!=null){
			result_List.add(result2);
			hm.put(result, 1.0f);
		}
		else if(result!=null){
			result_List.add(result);
			hm.put(result, 1.0f);
			if(Setting.isDebugModus())DebugMode.debugPrint("Found uri for: "+string.toLowerCase());
		}
		else{
			if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt find uri for: "+string.toLowerCase());
			
			result_List.add("http://dbpedia.org/ontology/"+string.toLowerCase().replace(" ", "_"));
			hm.put(result, 0.0f);
		}
	}
	else{
		result=myindex.getPropertyURI(string.toLowerCase());
		result2=myindex.getontologyURI(string.toLowerCase());
		if(Setting.isDebugModus())DebugMode.debugPrint("Result: "+result);
		if(result2!=null){
			result_List.add(result2);
			hm.put(result, 1.0f);
			if(Setting.isDebugModus())DebugMode.debugPrint("Found uri for: "+string.toLowerCase());
		}
		else if(result!=null){
			result_List.add(result);
			hm.put(result, 1.0f);
			if(Setting.isDebugModus())DebugMode.debugPrint("Found uri for: "+string.toLowerCase());
		}
		else{
			if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt find uri for: "+string.toLowerCase());
			
			result_List.add("http://dbpedia.org/ontology/"+string.toLowerCase().replace(" ", "_"));
			hm.put(result, 0.0f);
		}
	}
	
	
	if(Setting.isDebugModus())DebugMode.debugPrint("######\n");

	
	return result_List;
	//return hm;
}


	
public static ArrayList<String> searchIndexForClass(String string, SQLiteIndex myindex) throws SQLException{
		
	/*
	 * TODO: also return a rank, if you find a direct match, give back 1, if you find a part match, give back for example 0.3 if you have a string you can split in 3
	 */
		string=string.replace("_", " ");
		string=string.replace("-", " ");
		string=string.replace(".", " ");
		String tmp1=null;
		String tmp2 = null;
		ArrayList<String> result_List = new ArrayList<String>();

		tmp1=myindex.getontologyClassURI(string.toLowerCase());
		//tmp2=myindex.getYagoURI(string.toLowerCase());
		if(tmp1!=null){
			result_List.add(tmp1);
		}
		/*else{
			
			/*
			 * doesnt contains to much classes right now
			 */
		/*	ArrayList<String> tmp_List = new ArrayList<String>();
			String[] array_tmp= string.split(" ");
			
			if(array_tmp.length>1){
				tmp_List=myindex.getListOfUriSpecialIndex(string);
			}
			if(tmp_List!=null)for(String st : tmp_List){
				if(st.contains("ontology")|| st.contains("yago"))result_List.add(st);
			}*/
			
			
			/*ArrayList<String> tmp_List = new ArrayList<String>();
			String[] array_tmp= string.split(" ");
			for(String s : array_tmp){
				if(s.length()>4) tmp_List=myindex.getontologyClassURILike(s.toLowerCase(),string.toLowerCase());
				for(String st : tmp_List){
					result_List.add(st);
				}
			}*/
			
		//}
		

		/*if(tmp2!=null) {
			result_List.add(tmp2);
		}*/
		/*
		 * if nothing is found, also try the like operator for each part of the string
		 */
		/*else{
			ArrayList<String> tmp_List = new ArrayList<String>();
			String[] array_tmp= string.split(" ");
			for(String s : array_tmp){
				if(s.length()>4) tmp_List=myindex.getYagoURILike(s.toLowerCase(),string.toLowerCase());
				for(String st : tmp_List){
					result_List.add(st);
				}
			}
			
		}*/
		
		/*
		 * also add String without the plural s at the end.
		 */
		if(string.substring(string.length()-1).contains("s")){
			String neuer_string = string.substring(0, string.length() -1);
			tmp1=myindex.getontologyClassURI(neuer_string.toLowerCase());
			//tmp2=myindex.getYagoURI(neuer_string.toLowerCase());
			if(tmp1!=null){
				result_List.add(tmp1);
			}
			/*if(tmp2!=null){
				result_List.add(tmp1);
			}*/
		}
		
		if(string.length()>3){
			if(string.substring(string.length()-3).contains("ies")){
				String neuer_string = string.substring(0, string.length() -3);
				neuer_string+="y";
				tmp1=myindex.getontologyClassURI(neuer_string.toLowerCase());
				//tmp2=myindex.getYagoURI(neuer_string.toLowerCase());
				if(tmp1!=null){
					result_List.add(tmp1);
				}
				/*if(tmp2!=null){
					result_List.add(tmp1);
				}*/
				
			}
		}
		
		

		
		return result_List;
	}



	
}
