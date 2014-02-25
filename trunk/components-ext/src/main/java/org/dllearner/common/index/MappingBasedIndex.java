package org.dllearner.common.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MappingBasedIndex {

	private final Map<String, List<String>> classUri2TokensMap;
	private final Map<String, List<String>> resourceUri2TokensMap;
	private final Map<String, List<String>> datatypePropertyUri2TokensMap;
	private final Map<String, List<String>> objectPropertyUri2TokensMap;

	Map<String,List<String>> uriToTokens(InputStream mapping) throws IOException
	{
		Map<String, List<String>> uriToTokens = new HashMap<>();
		try(BufferedReader in = new BufferedReader(new InputStreamReader(mapping)))
		{
			String line = null;
			while((line = in.readLine()) != null)
			{
				int split = line.indexOf("|");
				//get the URI
				String uri = line.substring(0, split);
				//get the list of tokens
				List<String> tokens = new ArrayList<String>();
				String tokenString = line.substring(split + 1);
				String[] tokenArray = tokenString.split(",");
				for(String token : tokenArray){tokens.add(token.trim());}				
				uriToTokens.put(uri, tokens);				
			}
			return uriToTokens;
		}		
	}

	public MappingBasedIndex(InputStream classMappingsFile, InputStream resourceMappingsFile,
			InputStream dataPropertyMappingsFile, InputStream objectPropertyMappingsFile) throws IOException
	{
		classUri2TokensMap=uriToTokens(classMappingsFile);
		resourceUri2TokensMap=uriToTokens(resourceMappingsFile);
		datatypePropertyUri2TokensMap=uriToTokens(dataPropertyMappingsFile);
		objectPropertyUri2TokensMap=uriToTokens(objectPropertyMappingsFile);
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

	public List<String> getResourceAsStreams(String token){
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

	public IndexResultSet getResourceAsStreamsWithScores(String token){
		IndexResultSet rs = new IndexResultSet();
		for(String uri : getResourceAsStreams(token)){
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

	public static void main(String[] args) throws IOException {
		MappingBasedIndex index = new MappingBasedIndex(MappingBasedIndex.class.getClassLoader().getResourceAsStream("tbsl/oxford_class_mappings.txt"),null, null, null);
		System.out.println(index.getClasses("flat"));
	}

}
