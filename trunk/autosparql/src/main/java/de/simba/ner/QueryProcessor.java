/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.simba.ner;

import uk.ac.shef.wit.simmetrics.similaritymetrics.*;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import javatools.parsers.PlingStemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.hp.hpl.jena.query.*;
import java.util.Iterator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
            List<ArrayList<? extends HasWord>> sentences = tagger.tokenizeText(new BufferedReader(new FileReader(temp)));
            for (ArrayList<? extends HasWord> sentence : sentences) {
                ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
                buffer = buffer + Sentence.listToString(tSentence, false);
            }

            logger.info("POS-tagged query = " + buffer);
            result = getNNs(buffer);
            //read strings

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private HashMap<String, String> getMapFromSparql(String basicQuery) {
        HashMap<String, String> result = new HashMap<String, String>();
        String query;
        boolean moreResults;
        int offset = 0;
        do {
            query = basicQuery + " LIMIT 1000 OFFSET " + offset;
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, query);
            ResultSet results = qexec.execSelect();

            if (results.hasNext()) {
                moreResults = true;
            } else {
                moreResults = false;
            }
            try {
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    result.put(soln.get("?o").toString(), soln.get("?s").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

            moreResults = results.hasNext();
            
            try {
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    result.add(soln.get("?o").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            offset = offset + 1000;
        } while (moreResults);

        return result;
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
        ArrayList<String> uriList = new ArrayList(uris);
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
                + "SELECT DISTINCT ?s ?o "
                + "WHERE {?s rdfs:label ?o . "
                + "?o <bif:contains> \"" + s.replaceAll(" ", "_") + "\"}";

        String uri = "", label;
        AbstractStringMetric metric = new QGramsDistance();
        float max = 0, sim;

        TreeSet<String> results = new TreeSet<String>();
        HashMap<String, String> labelToUri = getMapFromSparql(query);

        logger.info("Found " + labelToUri.size() + " possible labels for " + s);
        //logger.info(labelToUri);

        TreeSet<String> cands = new TreeSet(labelToUri.keySet());
        Iterator<String> labels = cands.iterator();
        String originalLabel;
        while (labels.hasNext()) {
            originalLabel = labels.next();
            //clear language information
            if (originalLabel.contains("@")) {
                label = originalLabel.substring(0, originalLabel.indexOf("@"));
            } else {
                label = originalLabel;
            }

            //get best label
            sim = metric.getSimilarity(label.toLowerCase(), s.toLowerCase());
            if (sim > max) {
                results = new TreeSet<String>();
                results.add(labelToUri.get(originalLabel));
                max = sim;
            } else if (sim == max) {
                results.add(labelToUri.get(originalLabel));
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
        logger.info("Related resources are "+entryCount);
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
        QueryProcessor qp = new QueryProcessor("src/main/resources/",
                "http://live.dbpedia.org/sparql", "src/main/resources/");
        String query = "Universities in sachsen";
        qp.setSynonymExpansion(true);
        System.out.println(qp.runQuery(query));
        System.out.println(qp.getRelatedResources());
    }
}
