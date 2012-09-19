package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.LingPipePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;
import com.aliasi.tag.Tagging;
import com.aliasi.util.Strings;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

// commented out because the NIF dependency was removed
public class POSTaggerEvaluation { /* extends StringParser<ObjectHandler<Tagging<String>>>{
	
	
	private List<PartOfSpeechTagger> taggers = Arrays.asList(new PartOfSpeechTagger[]{
			new ApachePartOfSpeechTagger(), new StanfordPartOfSpeechTagger(), new LingPipePartOfSpeechTagger()});
	
	private OLiAManager m = new OLiAManager();
    private OLiAOntology brown;
    private OLiAOntology penn;
    
    private static final String OLIA_NS = "http://purl.org/olia/olia.owl";
    
    private Filter<OntClass> olia_filter = new Filter<OntClass>() {
		@Override
		public boolean accept(OntClass oc) {
			return oc.getURI().startsWith(OLIA_NS);
		}
	};
	
	
	public POSTaggerEvaluation() {
//    	brown = m.getOLiAOntology("http://purl.org/olia/brown-link.rdf");
//    	penn = m.getOLiAOntology("http://purl.org/olia/penn-link.rdf");
	}
	
	public void run(File directory){
		 // train on files in data directory 
        File[] files = directory.listFiles();
        for (File file : files) {
            System.out.println("Training file=" + file);
            try {
				parse(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
        }
	}
	
    @Override
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i){
            if (!Strings.allWhitespace(sentences[i])){
            	processSentence(sentences[i]);
            }
        }
    }

    public String normalizeTag(String rawTag) {
        String tag = rawTag;
        String startTag = tag;
        // remove plus, default to first
        int splitIndex = tag.indexOf('+');
        if (splitIndex >= 0)
            tag = tag.substring(0,splitIndex);

        int lastHyphen = tag.lastIndexOf('-');
        if (lastHyphen >= 0) {
            String first = tag.substring(0,lastHyphen);
            String suffix = tag.substring(lastHyphen+1);
            if (suffix.equalsIgnoreCase("HL") 
                || suffix.equalsIgnoreCase("TL")
                || suffix.equalsIgnoreCase("NC")) {
                tag = first;
            }
        }

        int firstHyphen = tag.indexOf('-');
        if (firstHyphen > 0) {
            String prefix = tag.substring(0,firstHyphen);
            String rest = tag.substring(firstHyphen+1);
            if (prefix.equalsIgnoreCase("FW")
                || prefix.equalsIgnoreCase("NC")
                || prefix.equalsIgnoreCase("NP"))
                tag = rest;
        }

        // neg last, and only if not whole thing
        int negIndex = tag.indexOf('*');
        if (negIndex > 0) {
            if (negIndex == tag.length()-1)
                tag = tag.substring(0,negIndex);
            else
                tag = tag.substring(0,negIndex)
                    + tag.substring(negIndex+1);
        }
        // multiple runs to normalize
        return tag.equals(startTag) ? tag : normalizeTag(tag);
    }
    
    private String extractSentence(String taggedSentence){
    	int pos = taggedSentence.indexOf("/");
    	int i = 0;
    	while(pos != -1){
    		String first = taggedSentence.substring(0, pos);
    		int endPos = taggedSentence.substring(pos).indexOf(" ");
    		if(endPos == -1){
    			endPos = taggedSentence.substring(pos).length();
    		}
    		String rest = taggedSentence.substring(pos + endPos);
    		
    		taggedSentence = first + rest;
    		pos = taggedSentence.indexOf("/");
    		
//    		if(i++ == 22)break;
    	}
    	return taggedSentence;
    	
    }

    private void processSentence(String taggedSentence) {
        String[] tagTokenPairs = taggedSentence.split(" ");
        List<String> tokenList = new ArrayList<String>(tagTokenPairs.length);
        List<String> tagList = new ArrayList<String>(tagTokenPairs.length);
    
        for (String pair : tagTokenPairs) {
            int j = pair.lastIndexOf('/');
            String token = pair.substring(0,j);
            String tag = normalizeTag(pair.substring(j+1)); 
            tokenList.add(token);
            tagList.add(tag);
        }
        Tagging<String> tagging = new Tagging<String>(tokenList,tagList);

        evaluateTaggers(taggedSentence, tagging);
        
    }
    
    private void evaluateTaggers(String taggedSentence, Tagging<String> referenceTagging){
//    	System.out.println("Checking tagged sentence:\n" + taggedSentence);
    	System.out.println(referenceTagging);
    	String extractedSentence = extractSentence(taggedSentence);
//    	System.out.println("Extracted sentence:\n" + extractedSentence);
        for(PartOfSpeechTagger tagger : taggers){
        	System.out.println("Testing " + tagger.getName());
        	//get the tagging form the POS tagger
        	Tagging<String> tagging = tagger.getTagging(extractedSentence);
        	
        	String referenceTag;
            String tag;
            int errorCnt = 0;
            for(int i = 0; i < referenceTagging.tags().size(); i++){
            	referenceTag = referenceTagging.tags().get(i);
            	tag = tagging.tags().get(i);
            	boolean matches = matchesOLiaClass(referenceTag, tag);
            	System.out.println(matches);
            }
            if(errorCnt > 0){
            	System.out.println(tagging);
            }
        }
    }
    
    //
    //  Returns TRUE if in the OLia hierarchy is somewhere a common class.
    //
    private boolean matchesOLiaClass(String brownTag, String pennTag){
    	Set<String> brownClasses = brown.getClassURIsForTag(brownTag.toUpperCase());
    	Set<String> pennClasses = penn.getClassURIsForTag(pennTag);
    	
    	System.out.println(brownTag + "-BROWN:" + brownClasses);
    	System.out.println(pennTag + "-PENN:" + pennClasses);
    	
    	OntModel brownModel = ModelFactory.createOntologyModel();
    	OntModel pennModel = ModelFactory.createOntologyModel();
    	
    	for (String classUri : brownClasses) {
    		brownModel.add(brown.getHierarchy(classUri));
        }
    	
    	for (String classUri : pennClasses) {
    		pennModel.add(penn.getHierarchy(classUri));
        }
    	
    	for(OntClass oc1 : brownModel.listClasses().filterKeep(olia_filter).toList()){
    		for(OntClass oc2 : pennModel.listClasses().filterKeep(olia_filter).toList()){
    			if(oc1.equals(oc2)){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    public static void main(String[] args) {
		POSTaggerEvaluation eval = new POSTaggerEvaluation();
		eval.run(new File(args[0]));
	}
    
*/
}
