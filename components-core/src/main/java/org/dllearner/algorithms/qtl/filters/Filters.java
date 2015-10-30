package org.dllearner.algorithms.qtl.filters;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.vocab.SKOSVocabulary;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Filters {
	
	public static List<String> getSkosFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.add(SKOSVocabulary.COMMENT.getIRI().toString());
		filters.add(SKOSVocabulary.DEFINITION.getIRI().toString());
		filters.add(SKOSVocabulary.COMMENT.getIRI().toString());
		filters.add(SKOSVocabulary.PREFLABEL.getIRI().toString());
		filters.add(SKOSVocabulary.ALTLABEL.getIRI().toString());
		
		return filters;
	}
	
	public static List<String> getRDFSFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.add(RDFS.comment.toString());
		filters.add(RDFS.label.toString());
		filters.add(RDFS.isDefinedBy.toString());
		filters.add(RDFS.seeAlso.toString());
		
		return filters;
	}
	
	public static List<String> getDBPediaFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.add("http://dbpedia.org/property/pageId");
		filters.add("http://dbpedia.org/property/revisionId");
		filters.add("http://dbpedia.org/ontology/abstract");
		filters.add("http://dbpedia.org/ontology/thumbnail");
		filters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		
		return filters;
	}
	
	public static List<String> getFOAFFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.add(FOAF.page.toString());
		filters.add(FOAF.homepage.toString());
		filters.add(FOAF.depiction.toString());
		filters.add(FOAF.Image.toString());
		filters.add(FOAF.surname.toString());
		filters.add(FOAF.birthday.toString());
		filters.add(FOAF.name.toString());
		filters.add(FOAF.firstName.toString());
//		filters.add(FOAF.givenname.toString());
		filters.add(FOAF.primaryTopic.toString());
		
		return filters;
	}
	
	public static List<String> getPurlFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.add("http://purl.org/dc/elements/1.1/language");
		filters.add("http://purl.org/dc/elements/1.1/rights");
			
		return filters;
	}
	
	public static List<String> getAllFilterProperties(){
		List<String> filters = new ArrayList<>();
		
		filters.addAll(Filters.getDBPediaFilterProperties());
		filters.addAll(Filters.getSkosFilterProperties());
		filters.addAll(Filters.getRDFSFilterProperties());
		filters.addAll(Filters.getPurlFilterProperties());
		filters.addAll(Filters.getFOAFFilterProperties());
		
		return filters;
	}

}
