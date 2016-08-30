package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.TripleComparator;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Lorenz Buehmann
 */
public class QueryRewriter {

    private static final Logger logger = LoggerFactory.getLogger(QueryRewriter.class);

    private static final QueryUtils queryUtils = new QueryUtils();

    static int maxTreeDepth = 3;

    /**
     * Returns a set of queries such that the intersection of the result for each query returns exactly the same result
     * as for the whole query.
     *
     * @param query
     */
    public static List<Query> split(Query query) {
        List<Query> queries = new ArrayList<>();
        queries.addAll(splitOutgoing(query));
        queries.addAll(splitIncoming(query));
        return queries;
    }

    private static List<Query> splitOutgoing(Query query) {
        // the target variable
        Var targetVar = query.getProjectVars().get(0);

        // mapping from var to outgoing triple patterns
        Set<Triple> outgoingTPs = queryUtils.extractOutgoingTriplePatternsTrans(query, targetVar.asNode());
        final Multimap<Var, Triple> var2OutgoingTPs = HashMultimap.create();
        for (Triple tp : outgoingTPs) {
            var2OutgoingTPs.put(Var.alloc(tp.getSubject()), tp);
        }

        // 1. get the outgoing triple patterns of the target var that do not have
        // outgoing triple patterns
        Set<Triple> fixedTriplePatterns = new HashSet<>();
        Set<Set<Triple>> clusters = new HashSet<>();
        var2OutgoingTPs.get(targetVar).forEach(tp -> {
            Node object = tp.getObject();
            if(object.isConcrete() || !var2OutgoingTPs.containsKey(Var.alloc(object))){
                fixedTriplePatterns.add(tp);
            } else {
                Set<Triple> cluster = new TreeSet<>(new TripleComparator());
                cluster.add(tp);
                clusters.add(cluster);
            }
        });
        boolean useSplitting = !clusters.isEmpty();

        if(!useSplitting){
            clusters.add(Sets.newHashSet(fixedTriplePatterns));
        } else {
            logger.debug("Query too complex. Splitting...");
            // 2. build clusters for other
            for (Set<Triple> cluster : clusters) {
                Triple representative = cluster.iterator().next();
                cluster.addAll(var2OutgoingTPs.get(Var.alloc(representative.getObject())));
                cluster.addAll(fixedTriplePatterns);
            }
        }

        // again split clusters to have only a maximum number of triple patterns
        int maxNrOfTriplePatternsPerQuery = 20;// number of outgoing triple patterns form the target var in each executed query
        Set<Set<Triple>> newClusters = new HashSet<>();
        for (Set<Triple> cluster : clusters) {
            int cnt = 0;
            for (Triple triple : cluster) {
                if(triple.getSubject().matches(targetVar)) {
                    cnt++;
                }
            }

            if(cnt > maxNrOfTriplePatternsPerQuery) {
                Set<Triple> newCluster = new HashSet<>();
                for (Triple triple : cluster) {
                    if(triple.getSubject().matches(targetVar)) {
                        newCluster.add(triple);
                    }
                    if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
                        newClusters.add(newCluster);
                        newCluster = new HashSet<>();
                    }
                }
                if(!newCluster.isEmpty()) {
                    newClusters.add(newCluster);
                }
            }
        }

        for (Set<Triple> cluster : newClusters) {
            for(int i = 1; i < maxTreeDepth; i++) {
                Set<Triple> additionalTriples = new HashSet<>();
                cluster.stream().filter(triple -> triple.getObject().isVariable()).forEach(triple -> {
                    Collection<Triple> triples = var2OutgoingTPs.get(Var.alloc(triple.getObject()));
                    additionalTriples.addAll(triples);
                });
                cluster.addAll(additionalTriples);
            }
        }
//		clusters = newClusters;



        List<Query> queries = new ArrayList<>();

        // 3. run query for each cluster
        for (Set<Triple> cluster : clusters) {
            // remove redundant edges
            SortedSet<Triple> tmp = new TreeSet<>(new Comparator<Triple>() {

                TripleComparator comp = new TripleComparator();

                @Override
                public int compare(Triple o1, Triple o2) {
                    boolean same = o1.subjectMatches(o2.getSubject())
                            && o2.predicateMatches(o2.getPredicate())
                            && o1.getObject().isVariable() && o2.getObject().isVariable();
//							&& !var2OutgoingTPs.containsKey(o1.getObject());
                    if (same) return 0;
                    return comp.compare(o1, o2);
                }
            });
            tmp.addAll(cluster);
            cluster = tmp;

            // build query
            Query q = new Query();
            q.addProjectVars(Collections.singleton(targetVar));
            ElementTriplesBlock el = new ElementTriplesBlock();
            for (Triple triple : cluster) {
                el.addTriple(triple);
            }
            q.setQuerySelectType();
            q.setDistinct(true);
            q.setQueryPattern(el);

            q = VirtuosoUtils.rewriteForVirtuosoDateLiteralBug(q);
//			q = rewriteForVirtuosoFloatingPointIssue(q);
//			sparqlQuery = getPrefixedQuery(sparqlQuery);

            queries.add(q);
        }
        return queries;
    }

    private static List<Query> splitIncoming(Query query) {
        // the target variable
        Var targetVar = query.getProjectVars().get(0);

        // mapping from var to outgoing triple patterns
        Set<Triple> incomingTPs = queryUtils.extractIncomingTriplePatternsTrans(query, targetVar.asNode());
        final Multimap<Var, Triple> var2IncomingTPs = HashMultimap.create();
        for (Triple tp : incomingTPs) {
            var2IncomingTPs.put(Var.alloc(tp.getObject()), tp);
        }

        // 1. get the outgoing triple patterns of the target var that do not have
        // outgoing triple patterns
        Set<Triple> fixedTriplePatterns = new HashSet<>();
        Set<Set<Triple>> clusters = new HashSet<>();
        var2IncomingTPs.get(targetVar).forEach(tp -> {
            Node s = tp.getSubject();
            if(s.isConcrete() || !var2IncomingTPs.containsKey(Var.alloc(s))){
                fixedTriplePatterns.add(tp);
            } else {
                Set<Triple> cluster = new TreeSet<>(new TripleComparator());
                cluster.add(tp);
                clusters.add(cluster);
            }
        });
        boolean useSplitting = !clusters.isEmpty();

        if(!useSplitting){
            clusters.add(Sets.newHashSet(fixedTriplePatterns));
        } else {
            logger.debug("Query too complex. Splitting...");
            // 2. build clusters for other
            for (Set<Triple> cluster : clusters) {
                Triple representative = cluster.iterator().next();
                cluster.addAll(var2IncomingTPs.get(Var.alloc(representative.getSubject())));
                cluster.addAll(fixedTriplePatterns);
            }
        }

        // again split clusters to have only a maximum number of triple patterns
        int maxNrOfTriplePatternsPerQuery = 20;// number of outgoing triple patterns form the target var in each executed query
        Set<Set<Triple>> newClusters = new HashSet<>();
        for (Set<Triple> cluster : clusters) {
            int cnt = 0;
            for (Triple triple : cluster) {
                if(triple.getObject().matches(targetVar)) {
                    cnt++;
                }
            }

            if(cnt > maxNrOfTriplePatternsPerQuery) {
                Set<Triple> newCluster = new HashSet<>();
                for (Triple triple : cluster) {
                    if(triple.getObject().matches(targetVar)) {
                        newCluster.add(triple);
                    }
                    if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
                        newClusters.add(newCluster);
                        newCluster = new HashSet<>();
                    }
                }
                if(!newCluster.isEmpty()) {
                    newClusters.add(newCluster);
                }
            }
        }

        for (Set<Triple> cluster : newClusters) {
            for(int i = 1; i < maxTreeDepth; i++) {
                Set<Triple> additionalTriples = new HashSet<>();
                cluster.stream().filter(triple -> triple.getSubject().isVariable()).forEach(triple -> {
                    Collection<Triple> triples = var2IncomingTPs.get(Var.alloc(triple.getSubject()));
                    additionalTriples.addAll(triples);
                });
                cluster.addAll(additionalTriples);
            }
        }
//		clusters = newClusters;



        List<Query> queries = new ArrayList<>();

        // 3. run query for each cluster
        for (Set<Triple> cluster : clusters) {
            // remove redundant edges
            SortedSet<Triple> tmp = new TreeSet<>(new Comparator<Triple>() {

                TripleComparator comp = new TripleComparator();

                @Override
                public int compare(Triple o1, Triple o2) {
                    boolean same = o1.objectMatches(o2.getObject())
                            && o2.predicateMatches(o2.getPredicate())
                            && o1.getSubject().isVariable() && o2.getSubject().isVariable();
//							&& !var2IncomingTPs.containsKey(o1.getObject());
                    if (same) return 0;
                    return comp.compare(o1, o2);
                }
            });
            tmp.addAll(cluster);
            cluster = tmp;

            // build query
            Query q = new Query();
            q.addProjectVars(Collections.singleton(targetVar));
            ElementTriplesBlock el = new ElementTriplesBlock();
            for (Triple triple : cluster) {
                el.addTriple(triple);
            }
            q.setQuerySelectType();
            q.setDistinct(true);
            q.setQueryPattern(el);

            q = VirtuosoUtils.rewriteForVirtuosoDateLiteralBug(q);
//			q = rewriteForVirtuosoFloatingPointIssue(q);
//			sparqlQuery = getPrefixedQuery(sparqlQuery);

            queries.add(q);
        }
        return queries;
    }

    public static void main(String[] args) {
        String query = "BASE    <http://dbpedia.org/resource/>\n" +
                "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                "PREFIX  schema: <http://schema.org/>\n" +
                "PREFIX  odp-dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#>\n" +
                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX  wiki: <http://wikidata.dbpedia.org/resource/>\n" +
                "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" +
                "\n" +
                "SELECT DISTINCT  ?s\n" +
                "WHERE\n" +
                "  { ?s        <http://purl.org/dc/terms/subject>  <./Category:Art_movements> .\n" +
                "    <Alfred_Pellan>\n" +
                "              dbo:influencedBy      ?s .\n" +
                "    <Sanzi>   dbo:influencedBy      ?s .\n" +
                "    <Alberto_Giacometti>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Ali_Divandari>\n" +
                "              dbo:movement          ?s .\n" +
                "    ?x0       dbo:movement          ?s .\n" +
                "    <Lucienne_Bloch>\n" +
                "              dbo:influencedBy      ?x0 .\n" +
                "    <Ang_Kiukok>  dbo:movement      ?s .\n" +
                "    ?x1       dbo:movement          ?s .\n" +
                "    <Ben_F._Laposky>\n" +
                "              dbo:influencedBy      ?x1 .\n" +
                "    <Francis_Picabia>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Francis_de_Erdely>\n" +
                "              dbo:movement          ?s .\n" +
                "    ?x2       dbo:movement          ?s .\n" +
                "    <Kenneth_Noland>\n" +
                "              dbo:influencedBy      ?x2 .\n" +
                "    <Jean_Lambert-Rucki>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Lito_Mayo>  dbo:movement       ?s .\n" +
                "    <Livio_Masciarelli>\n" +
                "              dbo:movement          ?s .\n" +
                "    ?x3       dbo:movement          ?s .\n" +
                "    <Georg_Muche>\n" +
                "              dbo:influencedBy      ?x3 .\n" +
                "    ?x4       dbo:movement          ?s .\n" +
                "    <Joseph_Cornell>\n" +
                "              dbo:influenced        ?x4 .\n" +
                "    ?x5       dbo:movement          ?s .\n" +
                "    <Sarah_Seager>\n" +
                "              dbo:influencedBy      ?x5 .\n" +
                "    ?x6       dbo:movement          ?s .\n" +
                "    <Stephen_Albair>\n" +
                "              dbo:influencedBy      ?x6 .\n" +
                "    <Marcel_Janco>\n" +
                "              dbo:movement          ?s .\n" +
                "    ?x7       dbo:movement          ?s .\n" +
                "    <Night_Tide>  dbo:starring      ?x7 .\n" +
                "    <Olga_Sacharoff>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Pablo_Picasso>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Peter_Blume>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Salvador_Dalí>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Syed_Thajudeen>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Theodore_Haupt>\n" +
                "              dbo:movement          ?s .\n" +
                "    <Vjekoslav_Vojo_Radoičić>\n" +
                "              dbo:movement          ?s .\n" +
                "    ?x8       dbo:movement          ?s .\n" +
                "    <Thomas_Furlong_(artist)>\n" +
                "              dbo:influenced        ?x8\n" +
                "  }";
        List<Query> queries = split(QueryFactory.create(query));
        queries.forEach(System.out::println);
    }
}
