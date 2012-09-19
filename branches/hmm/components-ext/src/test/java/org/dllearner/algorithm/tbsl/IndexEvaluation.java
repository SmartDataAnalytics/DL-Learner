package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.tbsl.search.HierarchicalSolrSearch;
import org.dllearner.algorithm.tbsl.search.SolrSearch;
import org.dllearner.algorithm.tbsl.search.ThresholdSlidingSolrSearch;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Prefixes;
import org.dllearner.algorithm.tbsl.util.StringSimilarityComparator;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IndexEvaluation {
	
	private static Logger logger = Logger.getLogger(IndexEvaluation.class);
	
	private SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
	
	private Map<String, String> prefixMap;
	
	private Templator templateGenerator;
	
	private SolrSearch resource_index;
	private SolrSearch class_index;
	private SolrSearch property_index;
	
	public IndexEvaluation(File ... evaluationFiles) {
		for(File file : evaluationFiles){
			readQueries(file);
		}
		init();
	}
	
	private void init(){
		try {
			Options options = new Options(new FileInputStream(this.getClass().getClassLoader().getResource("tbsl/tbsl.properties").getPath()));
			
			templateGenerator = new Templator();
			prefixMap = Prefixes.getPrefixes();
			
			String resourcesIndexUrl = options.fetch("solr.resources.url");
			String resourcesIndexSearchField = options.fetch("solr.resources.searchfield");
			resource_index = new ThresholdSlidingSolrSearch(resourcesIndexUrl, resourcesIndexSearchField, 1.0, 0.1);
			
			String classesIndexUrl = options.fetch("solr.classes.url");
			String classesIndexSearchField = options.fetch("solr.classes.searchfield");
			class_index = new ThresholdSlidingSolrSearch(classesIndexUrl, classesIndexSearchField, 1.0, 0.1);
			
			String propertiesIndexUrl = options.fetch("solr.properties.url");
			String propertiesIndexSearchField = options.fetch("solr.properties.searchfield");
			SolrSearch labelBasedPropertyIndex = new SolrSearch(propertiesIndexUrl, propertiesIndexSearchField);
			
			String boaPatternIndexUrl = options.fetch("solr.boa.properties.url");
			String boaPatternIndexSearchField = options.fetch("solr.boa.properties.searchfield");
			SolrSearch patternBasedPropertyIndex = new SolrSearch(boaPatternIndexUrl, boaPatternIndexSearchField);
			
			property_index = new HierarchicalSolrSearch(patternBasedPropertyIndex, labelBasedPropertyIndex);
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readQueries(File file){
		logger.info("Reading file containing queries and answers...");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			int id;
			String question;
			String query;
			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
				//read question ID
				id = Integer.valueOf(questionNode.getAttribute("id"));
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//Read SPARQL query
				query = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
				
				id2Question.put(id, question);
				id2Query.put(id, query);
				
			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Done.");
	}
	
	private Set<String> extractEntities(String query){
		List<String> exclusions = Arrays.asList(new String[]{"rdf", "rdfs"});
		Set<String> entities = new HashSet<String>();
		//pattern to detect resources
		Pattern pattern = Pattern.compile("(\\w+):(\\w+)");
		Matcher matcher = pattern.matcher(query);
		String group;
		while(matcher.find()){
			group = matcher.group();
			boolean add = true;
			for(String ex : exclusions){
				if(group.contains(ex)){
					add = false;
					break;
				}
			}
			if(add){
				entities.add(getFullURI(group));
			}
		}
		//pattern to detect string literals
		pattern = Pattern.compile("'(\\w+)'@en");
		matcher = pattern.matcher(query);
		while(matcher.find()){
			group = matcher.group();
			entities.add(getFullURI(buildEntityFromLabel(group)));
		}
		
		return entities;
	}
	
	private String getFullURI(String prefixedURI){
		String fullURI = prefixedURI;
		String prefix;
		String uri;
		for(Entry<String, String> uri2Prefix : prefixMap.entrySet()){
			uri = uri2Prefix.getKey();
			prefix = uri2Prefix.getValue();
			if(prefixedURI.startsWith(prefix)){
				fullURI = prefixedURI.replace(prefix + ":", uri);
				break;
			}
		}
		return fullURI;
	}
	
	private String getPrefixedURI(String fullURI){
		String prefixedURI = fullURI;
		String prefix;
		String uri;
		for(Entry<String, String> prefix2URI : prefixMap.entrySet()){
			prefix = prefix2URI.getKey();
			uri = prefix2URI.getValue();
			if(fullURI.startsWith(uri)){
				prefixedURI = fullURI.replace(uri, prefix + ":" );
				break;
			}
		}
		return prefixedURI;
	}
	
	private String buildEntityFromLabel(String label){
		String base = "res:";
		String entity = label.substring(1).substring(0, label.lastIndexOf("'")-1).replace(" ", "_");
		return base + entity;
	}
	
	private List<String> getCandidateURIsSortedBySimilarity(Slot slot){
		List<String> sortedURIs = new ArrayList<String>();
		//get the appropriate index based on slot type
		SolrSearch index = getIndexBySlotType(slot);
		
		SortedSet<String> tmp;
		List<String> uris;
		
		//prune the word list only when slot type is not RESOURCE
		List<String> words;
		if(slot.getSlotType() == SlotType.RESOURCE){
			words = slot.getWords();
		} else {
//			words = pruneList(slot.getWords());//getLemmatizedWords(slot.getWords());
			words = pruneList(slot.getWords());
		}
		
		for(String word : words){
			tmp = new TreeSet<String>(new StringSimilarityComparator(word));
			uris = index.getResources(word, 5);
		
			tmp.addAll(uris);
			sortedURIs.addAll(tmp);
			tmp.clear();
		}
		
		logger.info(slot.getToken() + "(" + slot.getSlotType() + ")" + "-->" + sortedURIs);
		return sortedURIs;
	}
	
	private List<String> pruneList(List<String> words){
		List<String> prunedList = new ArrayList<String>();
		for(String w1 : words){
			boolean smallest = true;
			for(String w2 : words){
				if(!w1.equals(w2)){
					if(w1.contains(w2)){
						smallest = false;
						break;
					}
				}
			}
			if(smallest){
				prunedList.add(w1);
			}
		}
		logger.trace("Pruned list: " + prunedList);
//		return getLemmatizedWords(words);
		return prunedList;
	}
	
	private SolrSearch getIndexBySlotType(Slot slot){
		SolrSearch index = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			index = class_index;
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			index = property_index;
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			index = resource_index;
		}
		return index;
	}
	
	public void run(){
		String question;
		String targetQuery;
		Set<String> targetEntities;
		Set<Template> templates;
		for(Entry<Integer, String> entry : id2Question.entrySet()){
			try {
				question = entry.getValue();
				targetQuery = id2Query.get(entry.getKey());
				targetQuery = targetQuery.replace("onto:", "dbo:").replace("res:", "dbr:").replace("prop:", "dbp:");
				
				logger.info("####################################################");
				logger.info(question);
//				logger.info(targetQuery);
				
				
				templates = templateGenerator.buildTemplates(question);
				if(!templates.isEmpty()){
					targetEntities = extractEntities(targetQuery);
					logger.info("Target entities:" + targetEntities);
					
					
					SortedSet<Slot> slots = new TreeSet<Slot>(new Comparator<Slot>() {

						@Override
						public int compare(Slot o1, Slot o2) {
							if(o1.getToken().equals(o2.getToken()) && o1.getSlotType().equals(o2.getSlotType())){
								return 0;
							} else {
								return -1;
							}
						}
					});
					for(Template t : templates){
						slots.addAll(t.getSlots());
					}
					
					Set<List<String>> uriLists = new HashSet<List<String>>();
					for(Slot slot : slots){
						uriLists.add(getCandidateURIsSortedBySimilarity(slot));
					}
					
					int pos = -1;
					for(String entity : targetEntities){
						for(List<String> uris : uriLists){
							pos = uris.indexOf(entity);
							if(pos >= 0){
								break;
							}
						}
						if(pos == -1){
							logger.info(entity + " not covered.");
						} else {
							logger.info(entity + " covered at position " + pos);
						}
					}
				} else {
					logger.info("No template generated.");
				}
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error");
			}
			
		}
	}
	
	public static void main(String[] args) throws IOException {
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "log/index_eval.txt", false));
		Logger.getLogger(IndexEvaluation.class).setLevel(Level.INFO);
		if(args.length == 0){
			System.out.println("Usage: IndexEvaluation <file>");
			System.exit(0);
		}
		
		File file = new File(IndexEvaluation.class.getClassLoader().getResource(args[0]).getPath());
		new IndexEvaluation(file).run();
	}

}
