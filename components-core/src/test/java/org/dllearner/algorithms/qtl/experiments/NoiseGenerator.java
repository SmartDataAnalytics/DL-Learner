package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Lorenz Buehmann
 */
public class NoiseGenerator {

    private static final Logger logger = LoggerFactory.getLogger(NoiseGenerator.class);

    private final QueryExecutionFactory qef;
    private final RandomDataGenerator rnd;

    enum NoiseMethod {
        RANDOM, SIMILAR, SIMILARITY_PARAMETERIZED
    }

    public NoiseGenerator(QueryExecutionFactory qef, RandomDataGenerator rnd) {
        this.qef = qef;
        this.rnd = rnd;
    }

    /**
     * Generates a list of candidates that are not contained in the given set of examples.
     * @param sparqlQuery
     * @param noiseMethod
     * @param examples
     * @param limit
     * @return list of candidate resource
     */
    public List<String> generateNoiseCandidates(String sparqlQuery, NoiseMethod noiseMethod, List<String> examples, int limit) {
        logger.info("generating noise...");
        List<String> noiseCandidates = new ArrayList<>();

        switch(noiseMethod) {
            case RANDOM: noiseCandidates = generateNoiseCandidatesRandom(examples, limit);
                break;
            case SIMILAR:noiseCandidates = generateNoiseCandidatesSimilar(examples, sparqlQuery, limit);
                break;
            case SIMILARITY_PARAMETERIZED://TODO implement configurable noise method
                break;
            default:noiseCandidates = generateNoiseCandidatesRandom(examples, limit);
                break;
        }
        Collections.sort(noiseCandidates);
        logger.info("#noise candidates={}", noiseCandidates.size());
        return noiseCandidates;
    }

    /**
     * Randomly pick {@code n} instances from KB that do not belong to given set of instances {@code examples}.
     * @param examples the instances that must not be contained in the returned list
     * @param n the number of random instances
     * @return
     */
    private List<String> generateNoiseCandidatesRandom(List<String> examples, int n) {
        List<String> noiseExampleCandidates = new ArrayList<>();

        rnd.reSeed(123);
        // get max number of instances in KB
        boolean strictOWL = false;
        String query;
        if(strictOWL) {
            query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type . ?type a <http://www.w3.org/2002/07/owl#Class> .}";
        } else {
            query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type }";
        }


        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        int max = rs.next().get("cnt").asLiteral().getInt();
        qe.close();

        // generate random instances
        while(noiseExampleCandidates.size() < n) {
            int offset = rnd.nextInt(0, max);
            if(strictOWL) {
                query = "SELECT DISTINCT ?s WHERE {?s a ?type . ?type a <http://www.w3.org/2002/07/owl#Class> .} LIMIT 10 OFFSET " + offset;
            } else {
                query = "SELECT DISTINCT ?s WHERE {?s a ?type } LIMIT 10 OFFSET " + offset;
            }
            System.out.println(query);
            qe = qef.createQueryExecution(query);
            rs = qe.execSelect();

            while(rs.hasNext()) {
                String resource = rs.next().getResource("s").getURI();

                if(!examples.contains(resource) && !resource.contains("__")) {
                    noiseExampleCandidates.add(resource);
                }
            }

            qe.close();
        }

        return noiseExampleCandidates;
    }

    private List<String> generateNoiseCandidatesSimilar(List<String> examples, String queryString, int limit){
        List<String> negExamples = new ArrayList<>();

        Query query = QueryFactory.create(queryString);

        QueryUtils queryUtils = new QueryUtils();

        Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);

        Set<String> negExamplesSet = new TreeSet<>();

        if(triplePatterns.size() == 1){
            Triple tp = triplePatterns.iterator().next();
            Node var = NodeFactory.createVariable("var");
            Triple newTp = Triple.create(tp.getSubject(), tp.getPredicate(), var);

            ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
            triplesBlock.addTriple(newTp);

            ElementFilter filter = new ElementFilter(new E_NotEquals(new ExprVar(var), NodeValue.makeNode(tp.getObject())));

            ElementGroup eg = new ElementGroup();
            eg.addElement(triplesBlock);
            eg.addElementFilter(filter);

            Query q = new Query();
            q.setQuerySelectType();
            q.setDistinct(true);
            q.addProjectVars(query.getProjectVars());

            q.setQueryPattern(eg);
//			System.out.println(q);

            List<String> result = null;
            try {
                result = SPARQLUtils.getResult(qef, q);
            } catch (Exception e) {
                e.printStackTrace();
            }
            negExamplesSet.addAll(result);
        } else {
            // we modify each triple pattern <s p o> by <s p ?var> . ?var != o
            Set<Set<Triple>> powerSet = new TreeSet<>((o1, o2) -> ComparisonChain.start()
                    .compare(o1.size(), o2.size())
                    .compare(o1.hashCode(), o2.hashCode())
                    .result());
            powerSet.addAll(Sets.powerSet(triplePatterns));

            for (Set<Triple> set : powerSet) {
                if(!set.isEmpty() && set.size() != triplePatterns.size()){
                    List<Triple> existingTriplePatterns = new ArrayList<>(triplePatterns);
                    List<Triple> newTriplePatterns = new ArrayList<>();
                    List<ElementFilter> filters = new ArrayList<>();
                    int cnt = 0;
                    for (Triple tp : set) {
                        if(tp.getObject().isURI() || tp.getObject().isLiteral()){
                            Node var = NodeFactory.createVariable("var" + cnt++);
                            Triple newTp = Triple.create(tp.getSubject(), tp.getPredicate(), var);

                            existingTriplePatterns.remove(tp);
                            newTriplePatterns.add(newTp);

                            ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
                            triplesBlock.addTriple(tp);

                            ElementGroup eg = new ElementGroup();
                            eg.addElement(triplesBlock);

                            ElementFilter filter = new ElementFilter(new E_NotExists(eg));
                            filters.add(filter);
                        }
                    }
                    Query q = new Query();
                    q.setQuerySelectType();
                    q.setDistinct(true);
                    q.addProjectVars(query.getProjectVars());
                    List<Triple> allTriplePatterns = new ArrayList<>(existingTriplePatterns);
                    allTriplePatterns.addAll(newTriplePatterns);
                    ElementTriplesBlock tripleBlock = new ElementTriplesBlock(BasicPattern.wrap(allTriplePatterns));
                    ElementGroup eg = new ElementGroup();
                    eg.addElement(tripleBlock);

                    for (ElementFilter filter : filters) {
                        eg.addElementFilter(filter);
                    }

                    q.setQueryPattern(eg);
//					System.out.println(q);

                    List<String> result = null;
                    try {
                        result = SPARQLUtils.getResult(qef, q);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.removeAll(examples);

                    if(result.isEmpty()){
                        q = new Query();
                        q.setQuerySelectType();
                        q.setDistinct(true);
                        q.addProjectVars(query.getProjectVars());
                        tripleBlock = new ElementTriplesBlock(BasicPattern.wrap(existingTriplePatterns));
                        eg = new ElementGroup();
                        eg.addElement(tripleBlock);

                        for (ElementFilter filter : filters) {
                            eg.addElementFilter(filter);
                        }

                        q.setQueryPattern(eg);
//						System.out.println(q);

                        try {
                            result = SPARQLUtils.getResult(qef, q);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        result.removeAll(examples);
                    }
                    negExamplesSet.addAll(result);
                }
            }
        }

        negExamplesSet.removeAll(examples);
        if(negExamples.isEmpty()){
            logger.error("Found no negative example.");
            System.exit(0);
        }
        negExamples.addAll(negExamplesSet);
        return new ArrayList<>(negExamples).subList(0, Math.min(negExamples.size(), limit));
    }
}
