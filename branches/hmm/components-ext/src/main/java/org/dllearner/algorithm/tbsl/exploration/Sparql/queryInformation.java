package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.util.ArrayList;
import java.util.HashMap;

public class queryInformation {
	// <question id="32" type="boolean" fusion="false" aggregation="false" yago="false">
	public final String query;
	 public final String type;
	 public final boolean fusion;
	 public final boolean aggregation;
	 public final boolean yago;
	 public final String id;
	 public final String XMLtype;
	 public final boolean hint;
	 public ArrayList<String> result = new ArrayList<String>();
	 public ArrayList<ArrayList<String>>  queryInformation = new ArrayList<ArrayList<String>>();
	 public long timeGesamt;
	 public long timeParser;
	 public long timeWithoutParser;
	 public HashMap<String,String> hashMap;
	 public String isaResource;
	 
	 public String getIsaResource() {
		return isaResource;
	}

	public void setIsaResource(String isaResource) {
		this.isaResource = isaResource;
	}

	public HashMap<String, String> getHashMap() {
		return hashMap;
	}

	public void setHashMap(HashMap<String, String> hashMap) {
		this.hashMap = hashMap;
	}
	
	public String getHashValue(String key) {
		key=key.toLowerCase();
		key=key.replace("  ", "");
		key=key.replace("_", " ");
		String result ="NONE";
		try{
			result=this.hashMap.get(key);
			if(result.isEmpty()||result==null) result="NONE";
		}
		catch (Exception e){
			return "NONE";
		}
		return result;
	}
	
	public void setHashValue(String key, String value) {
		key=key.replace("  ", "");
		key=key.replace("_", " ");
		value=value.replace("__", "");
		this.hashMap.put(key.toLowerCase(), value);
	}
	

	public long getTimeGesamt() {
		return timeGesamt;
	}

	public long getTimeParser() {
		return timeParser;
	}

	public long getTimeWithoutParser() {
		return timeWithoutParser;
	}

	public void setTimeGesamt(long time) {
		this.timeGesamt=time;
	}

	public void setTimeParser(long time) {
		this.timeParser=time;
	}

	public void setTimeWithoutParser(long time) {
		this.timeWithoutParser=time;
	}
	
	 
	 
	 public ArrayList<String> getResult() {
		return result;
	}
	 
	 public void setQueryInformation(ArrayList<ArrayList<String>> lstquery) {
		 this.queryInformation=lstquery;
		}
	 
	 public ArrayList<ArrayList<String>>  getQueryInformation() {
			return queryInformation;
		}
		 
	 public void setResult(ArrayList<String> new_result) {
			 this.result=new_result;
			}
		 


	public boolean isHint() {
		return hint;
	}


	public String getXMLtype() {
		return XMLtype;
	}

	 
	 public String getId() {
		return id;
	}
	public String getQuery() {
		return query;
	}
	public String getType() {
		return type;
	}
	public boolean isFusion() {
		return fusion;
	}
	public boolean isAggregation() {
		return aggregation;
	}
	public boolean isYago() {
		return yago;
	}
	
	public queryInformation(String query1, String id1, String type1, boolean fusion1, boolean aggregation1, boolean yago1, String XMLtype1, boolean hint1){
		this.query=query1;
		 this.type=type1;
		 this.fusion=fusion1;
		 this.aggregation=aggregation1;
		 this.yago=yago1;
		 this.id=id1;
		 this.XMLtype=XMLtype1;
		 this.hint=hint1;
		 this.hashMap= new HashMap<String,String>();
	}
}
