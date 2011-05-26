/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.simba.ner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 *
 * @author ngonga
 */
public class QueryProcessor {

    static Logger logger = Logger.getLogger("QP");
    private MaxentTagger tagger = null;
    HashMap<String, Integer> entryCount;
    String endPoint = "http://dbpedia.org/sparql";
    TreeSet<String> nouns;
    WordnetQuery wnq;
    private boolean expansion = true;
    
    public QueryProcessor(String _modelPath) {
        wnq = new WordnetQuery("src/main/resources/de/simba/ner/dictionary");
        try {
            tagger = new MaxentTagger(_modelPath);
            PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %c: %m%n");
            FileAppender fileAppender = new FileAppender(layout, "C:/Temp/nlquery.log", false);
            fileAppender.setLayout(layout);
            logger.addAppender(fileAppender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QueryProcessor(String _modelPath, String _endPoint, String wordnetDictionaryPath) {
        endPoint = _endPoint;
//        wnq = new WordnetQuery("D:\\Work\\Tools\\WordNetDict");
        wnq = new WordnetQuery(wordnetDictionaryPath);
        try {
            tagger = new MaxentTagger(_modelPath);
            PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %c: %m%n");
            FileAppender fileAppender = new FileAppender(layout, "C:/Temp/nlquery.log", false);
            fileAppender.setLayout(layout);
            logger.addAppender(fileAppender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /** Get nouns from the string s
     *
     * @param s
     * @return
     */
    private TreeSet<String> parseForNouns(String s) {
        logger.info("Initial query = "+s);
        TreeSet<String> result = new TreeSet<String>();
        if (tagger == null) {
            return result;
        }
        try {
            File temp = File.createTempFile("999", null);
            //write input
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(temp.getAbsolutePath())));
            writer.println(s);
            writer.close();

            //tag and write output
            String buffer = "";
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(s)));
            for (List<HasWord> sentence : sentences) {
                ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
                for(TaggedWord taWo : tSentence){
                	System.out.println("Word:" + taWo.word() + " Tag: " + taWo.tag());
                }
                buffer = buffer + Sentence.listToString(tSentence, false);
            }

            logger.info("POS-tagged query = " + buffer);
            result = getNNs(buffer);
            //read strings
            //stem words
//            result = getStemmedWords(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private HashMap<String, SortedSet<String>> getMapFromSparql(String basicQuery) {
        HashMap<String, SortedSet<String>> result = new HashMap<String, SortedSet<String>>();
        String query;
        boolean moreResults;
        int offset = 0;
        do {
            query = basicQuery + " LIMIT 1000 OFFSET " + offset;
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, query);
            ResultSet results = qexec.execSelect();

//            moreResults = results.hasNext();
            
            int cnt = 0;
            try {
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    String label = soln.get("o").asLiteral().getLexicalForm();
                    SortedSet<String> resources = result.get(label);
                    if(resources == null){
                    	resources = new TreeSet<String>();
                    	result.put(label, resources);
                    }
                    resources.add(soln.get("s").toString());
                    cnt++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            moreResults = (cnt == 1000);
            offset = offset + 1000;
        } while (moreResults);

        return result;
    }

    /** Takes a query and returns the list of all URIs that map
     *
     */
    private TreeSet<String> getSetFromSparql(String basicQuery) {
        TreeSet<String> result = new TreeSet<String>();
        String query;
        boolean moreResults;
        int offset = 0;
        do {
            query = basicQuery + " LIMIT 1000 OFFSET " + offset;
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, query);
            ResultSet results = qexec.execSelect();

            int cnt = 0;
            try {
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    result.add(soln.get("?o").toString());
                    cnt++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            moreResults = cnt == 1000;
            offset = offset + 1000;
        } while (moreResults);

        return result;
    }
    
    private TreeSet<String> getStemmedWords(TreeSet<String> words){
    	TreeSet<String> stemmedWords = new TreeSet<String>();
    	Morphology morpho = new Morphology();
    	for(String w : words){
    		stemmedWords.add(morpho.stem(w));
    	}
    	return stemmedWords;
    }

    /** Get all URIs that are related to the URI uri
     *
     * @param uri Input URI
     * @return TreeSet of related URIs
     */
    private ArrayList<String> getRelatedUris(String uri) {
        TreeSet<String> uris;
        ArrayList<String> result = new ArrayList<String>();
        String basicQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " 
                + "SELECT DISTINCT ?o "
                + "WHERE {{<" + uri + "> ?p ?o } UNION {?o ?p <" + uri + ">} UNION {?o <" + uri + "> ?p}" +
                "UNION {?p <" + uri + "> ?o}}";

        String relatedUri;
        uris = getSetFromSparql(basicQuery);
        logger.info("Found " + uris.size() + " URIs related to " + uri);

        Iterator<String> uriIter = uris.iterator();
        while (uriIter.hasNext()) {
            relatedUri = uriIter.next();
            if(relatedUri.startsWith("http://dbpedia.org"))
                result.add(relatedUri);
        }
//        logger.info("********\nUri related to " + uri + "\n" + result + "*******\n\n");
        return result;
    }

    private HashMap<String, Integer> computeMaxList(TreeSet<String> uris) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        entryCount = new HashMap<String, Integer>();
        int max = 0;
        int count;
        ArrayList<String> uriList = new ArrayList<String>(uris);
        logger.info("Threshold for frequency is "+nouns.size());
        //get all URIs that are related to the nouns found
        for (int i = 0; i < uriList.size(); i++) {
            ArrayList<String> resultList = getRelatedUris(uriList.get(i));
            for (int j = 0; j < resultList.size(); j++) {
                if (entryCount.containsKey(resultList.get(j))) {

                    //count is the new count for the entry that was just found
                    count = entryCount.get(resultList.get(j)).intValue() + 1;
                    entryCount.put(resultList.get(j), count);
                    if (count > max) {
                        max = count;
                    }
                } else {
                    entryCount.put(resultList.get(j), new Integer(1));
                }
            }
        }
        
        Iterator<String> entries = entryCount.keySet().iterator();
        String entry;
        logger.info("Maximal frequency = "+max);
        if(max > nouns.size()) max = nouns.size();
        while(entries.hasNext())
        {
            entry = entries.next();
            if(entryCount.get(entry).intValue() >= max) result.put(entry, entryCount.get(entry));
        }

        logger.info("Best scoring resources are "+result);
        return result;
    }

    private TreeSet<String> getNNs(String buffer) {
        TreeSet<String> result = new TreeSet<String>();
        String split[] = buffer.split(" ");
        String nameAndTag[], noun = "";
        for (int i = 0; i < split.length; i++) {
            nameAndTag = split[i].split("/");
            if (nameAndTag[1].startsWith("NN")) {
                noun = nameAndTag[0];
                i++;
                for (int j = i; j < split.length; j++) {
                    nameAndTag = split[j].split("/");
                    if (nameAndTag[1].startsWith("NN")) {
                        noun = noun + " " + nameAndTag[0];
                        i++;
                    } else {
                        break;
                    }
                }
            }
            if (!noun.equals("")) {
                result.add(noun);
                noun = "";
            }
        }
        return result;
    }

    /** Returns a list of possible URIs to a given text entry
     *
     * @param s Text entry
     * @return URI
     */
    private TreeSet<String> getURIs(String s) {

        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " 
        		+ "PREFIX dbo: <http://www.dbpedia.org/ontology/>"
                + "SELECT DISTINCT ?s ?o "
                + "WHERE {?s rdfs:label ?o . "
                + "?o <bif:contains> \"" + s.replaceAll(" ", "_") + "\"}";

        AbstractStringMetric metric = new QGramsDistance();
        float max = 0, sim;

        TreeSet<String> results = new TreeSet<String>();
        HashMap<String, SortedSet<String>> labelToUris = getMapFromSparql(query);

        logger.info("Found " + labelToUris.size() + " possible labels for " + s);
        //logger.info(labelToUri);

        TreeSet<String> cands = new TreeSet<String>(labelToUris.keySet());
        Iterator<String> labels = cands.iterator();
        String label;
        while (labels.hasNext()) {
            label = labels.next();

            //get best label
            sim = metric.getSimilarity(label.toLowerCase(), s.toLowerCase());
            if (sim > max) {
                results = new TreeSet<String>();
                results.addAll(labelToUris.get(label));
                max = sim;
            } else if (sim == max) {
                results.addAll(labelToUris.get(label));
            }
        }

        //System.out.println(uriToLabel);
        //get best label

        logger.info("URIs for " + s + " are <" + results + ">");
        return results;
    }

    /** Main method for using this library. One gives in a String (
     * and get a set of matching URIs
     * @param buffer
     * @return
     */
    public HashMap<String, Integer> runQuery(String query) {
        
        long startTime = System.currentTimeMillis();
        //first get nouns
        nouns = parseForNouns(query);
        logger.info("Nouns in query = "+nouns);
        //then exapnd them using all their wordnet synonyms
        TreeSet<String> expandedNouns;
        if(expansion) expandedNouns = getSynonyms(nouns);
        else expandedNouns = nouns;
        logger.info("Expanded nouns (added synonyms) from query = "+expandedNouns);
        //then get URIs for these nouns
        TreeSet<String> uris = new TreeSet<String>();
        Iterator<String> nounIter = expandedNouns.iterator();
        while (nounIter.hasNext()) {
            uris.addAll(getURIs(nounIter.next()));
        }

        //now get best scoring list of URIs related to these nouns
        logger.info("Computed in "+(System.currentTimeMillis() - startTime)/1000.0f+"seconds");
        return computeMaxList(uris);
    }

    public TreeSet<String> getSynonyms(TreeSet<String> nouns)
    {
        TreeSet<String> result = new TreeSet<String>(nouns);
        Iterator<String> nounIter = nouns.iterator();

        while (nounIter.hasNext()) {
            result.addAll(wnq.getSynset(nounIter.next()));
        }
        return result;
    }
    /**
     * Returns a list of resources that might be related to the query
     * @return
     */
    public HashMap<String, Integer> getRelatedResources() {
//        logger.info("Related resources are "+entryCount);
        return entryCount;
    }

    /** Configure whether the expansion is switched on or not
     * 
     * @param setting Setting for WordNet expansion
     */
    public void setSynonymExpansion(boolean setting)
    {
        expansion = setting;
    }
    public static void main(String[] args) {
//    	String endpoint = "http://dbpedia.org/sparql";
    	String endpoint = "http://live.dbpedia.org/sparql";
        QueryProcessor qp = new QueryProcessor("src/main/resources/de/simba/ner/models/left3words-wsj-0-18.tagger",
                endpoint, "src/main/resources/de/simba/ner/dictionary");
        String query = "Cities in Saxony";
        qp.setSynonymExpansion(false);
        System.out.println(qp.runQuery(query));
//        System.out.println(qp.getRelatedResources());
    }
}
