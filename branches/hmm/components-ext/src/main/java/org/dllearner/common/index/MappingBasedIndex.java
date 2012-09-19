package org.dllearner.common.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MappingBasedIndex {
	
	private Map<String, List<String>> classUri2TokensMap = new HashMap<String, List<String>>();
	private Map<String, List<String>> resourceUri2TokensMap = new HashMap<String, List<String>>();
	private Map<String, List<String>> datatypePropertyUri2TokensMap = new HashMap<String, List<String>>();
	private Map<String, List<String>> objectPropertyUri2TokensMap = new HashMap<String, List<String>>();
	
	public MappingBasedIndex(String classMappingsFile, String resourceMappingsFile,
			String dataPropertyMappingsFile, String objectPropertyMappingsFile) {
		BufferedReader br = null;
		String line = null;
		try {
			//load class mappings
			if(classMappingsFile != null){
				br = new BufferedReader(new FileReader(new File(classMappingsFile)));
				while((line = br.readLine()) != null){
					int split = line.indexOf("|");
					//get the URI
					String uri = line.substring(0, split);
					//get the list of tokens
					List<String> tokens = new ArrayList<String>();
					String tokenString = line.substring(split + 1);
					String[] tokenArray = tokenString.split(",");
					for(String token : tokenArray){
						tokens.add(token.trim());
					}
					
					classUri2TokensMap.put(uri, tokens);
				}
			}
			
			//load resource mappings
			if(resourceMappingsFile != null){
				br = new BufferedReader(new FileReader(new File(resourceMappingsFile)));
				while((line = br.readLine()) != null){
					int split = line.indexOf("|");
					//get the URI
					String uri = line.substring(0, split);
					//get the list of tokens
					List<String> tokens = new ArrayList<String>();
					String tokenString = line.substring(split + 1);
					String[] tokenArray = tokenString.split(",");
					for(String token : tokenArray){
						tokens.add(token.trim());
					}
					
					resourceUri2TokensMap.put(uri, tokens);
				}
			}
			
			//load object property mappings
			if(objectPropertyMappingsFile != null){
				br = new BufferedReader(new FileReader(new File(objectPropertyMappingsFile)));
				while((line = br.readLine()) != null){
					int split = line.indexOf("|");
					//get the URI
					String uri = line.substring(0, split);
					//get the list of tokens
					List<String> tokens = new ArrayList<String>();
					String tokenString = line.substring(split + 1);
					String[] tokenArray = tokenString.split(",");
					for(String token : tokenArray){
						tokens.add(token.trim());
					}
					
					objectPropertyUri2TokensMap.put(uri, tokens);
				}
			}
			
			//load datatype property mappings
			if(dataPropertyMappingsFile != null){
				br = new BufferedReader(new FileReader(new File(dataPropertyMappingsFile)));
				while((line = br.readLine()) != null){
					int split = line.indexOf("|");
					//get the URI
					String uri = line.substring(0, split);
					//get the list of tokens
					List<String> tokens = new ArrayList<String>();
					String tokenString = line.substring(split + 1);
					String[] tokenArray = tokenString.split(",");
					for(String token : tokenArray){
						tokens.add(token.trim());
					}
					
					datatypePropertyUri2TokensMap.put(uri, tokens);
				}
			}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getClasses(String token){
		List<String> uris = new ArrayList<String>();
		for(Entry<String, List<String>> entry : classUri2TokensMap.entrySet()){
			if(entry.getValue().contains(token)){
				uris.add(entry.getKey());
			}
		}
		return uris;
	}
	
	public List<String> getResources(String token){
		List<String> uris = new ArrayList<String>();
		for(Entry<String, List<String>> entry : resourceUri2TokensMap.entrySet()){
			if(entry.getValue().contains(token)){
				uris.add(entry.getKey());
			}
		}
		return uris;
	}
	
	public List<String> getObjectProperties(String token){
		List<String> uris = new ArrayList<String>();
		for(Entry<String, List<String>> entry : objectPropertyUri2TokensMap.entrySet()){
			if(entry.getValue().contains(token)){
				uris.add(entry.getKey());
			}
		}
		return uris;
	}
	
	public List<String> getDatatypeProperties(String token){
		List<String> uris = new ArrayList<String>();
		for(Entry<String, List<String>> entry : datatypePropertyUri2TokensMap.entrySet()){
			if(entry.getValue().contains(token)){
				uris.add(entry.getKey());
			}
		}
		return uris;
	}
	
	public List<String> getProperties(String token){
		List<String> uris = new ArrayList<String>();
		uris.addAll(getObjectProperties(token));
		uris.addAll(getDatatypeProperties(token));
		return uris;
	}
	
	public IndexResultSet getClassesWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getClasses(token)){
			rs.addItem(new IndexResultItem(uri, token, 1f));
		}
		return rs;
	}
	
	public IndexResultSet getResourcesWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getResources(token)){
			rs.addItem(new IndexResultItem(uri, token, 1f));
		}
		return rs;
	}
	
	public IndexResultSet getObjectPropertiesWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getObjectProperties(token)){
			rs.addItem(new IndexResultItem(uri, token, 1f));
		}
		return rs;
	}
	
	public IndexResultSet getDatatypePropertiesWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getDatatypeProperties(token)){
			rs.addItem(new IndexResultItem(uri, token, 1f));
		}
		return rs;
	}
	
	public IndexResultSet getPropertiesWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getProperties(token)){
			rs.addItem(new IndexResultItem(uri, token, 1f));
		}
		return rs;
	}
	
	public Boolean isDataProperty(String uri){
		if(datatypePropertyUri2TokensMap.containsKey(uri)) {
			return true;
		} else if(objectPropertyUri2TokensMap.containsKey(uri)){
			return false;
		}
		return null;
	}
	
	public static void main(String[] args) {
		MappingBasedIndex index = new MappingBasedIndex(MappingBasedIndex.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), null, null, null);
		System.out.println(index.getClasses("flat"));
	}

}
