package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
import java.util.stream.Collectors;

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
//        System.out.println("SPLIT:\n" + query);
        List<Query> queries = new ArrayList<>();
        queries.addAll(splitOutgoing2(query));
        queries.addAll(splitIncoming(query));
        return queries;
    }

    private static List<Query> splitOutgoing(Query query) {
        // the target variable
        Var targetVar = query.getProjectVars().get(0);

        // mapping from var to outgoing triple patterns
        Set<Triple> outgoingTPs = queryUtils.extractOutgoingTriplePatternsTrans(query, targetVar.asNode());

        if(outgoingTPs.isEmpty()) {
            return Collections.emptyList();
        }

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

    private static List<Query> splitOutgoing2(Query query) {
        // the target variable
        Var targetVar = query.getProjectVars().get(0);

        // mapping from var to incoming triple patterns
        Set<Triple> incomingTPs = queryUtils.extractOutgoingTriplePatternsTrans(query, targetVar.asNode());

        // return if there are no incoming triple patterns
        if(incomingTPs.isEmpty()) {
            return Collections.emptyList();
        }

        // mapping from each variable to triple patterns in which they occur as object
        final Multimap<Var, Triple> var2IncomingTPs = HashMultimap.create();
        for (Triple tp : incomingTPs) {
            var2IncomingTPs.put(Var.alloc(tp.getSubject()), tp);
        }

        // 1. get the incoming triple patterns of the target var that do not have
        // incoming triple patterns
        Set<Triple> fixedTriplePatterns = var2IncomingTPs.get(targetVar).stream()
                .filter(tp -> tp.getObject().isConcrete() || !var2IncomingTPs.containsKey(Var.alloc(tp.getObject())))
                .collect(Collectors.toSet());

        // split
        List<List<Triple>> fixedTpPartitions = Lists.partition(new ArrayList<>(fixedTriplePatterns), 5);

        // all other incoming triple patterns from target var become a new cluster
        Set<Set<Triple>> clusters = var2IncomingTPs.get(targetVar).stream()
                .filter(tp -> !fixedTriplePatterns.contains(tp))
                .map(Sets::newHashSet)
                .collect(Collectors.toSet());

        // add some fixed TPs to reduce resultset
        for (Set<Triple> cluster : clusters) {
            Triple representative = cluster.iterator().next();
            cluster.addAll(var2IncomingTPs.get(Var.alloc(representative.getObject())));
            cluster.addAll(fixedTpPartitions.get(0));
        }

        fixedTpPartitions.forEach(p -> clusters.add(Sets.newHashSet(p)));

        // again split clusters to have only a maximum number of triple patterns
        int maxNrOfTriplePatternsPerQuery = 10;// number of outgoing triple patterns form the target var in each executed query
        Set<Set<Triple>> newClusters = new HashSet<>();

        for (Set<Triple> cluster : clusters) {
            Lists.partition(new ArrayList<>(cluster), 10).forEach(p -> newClusters.add(Sets.newHashSet(p)));

//            int cnt = 0;
//            for (Triple triple : cluster) {
//                if(triple.getObject().matches(targetVar)) {
//                    cnt++;
//                }
//            }
//
//            if(cnt > maxNrOfTriplePatternsPerQuery) {
//                Set<Triple> newCluster = new HashSet<>();
//                for (Triple triple : cluster) {
//                    if(triple.getObject().matches(targetVar)) {
//                        newCluster.add(triple);
//                    }
//                    if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
//                        newClusters.add(newCluster);
//                        newCluster = new HashSet<>();
//                    }
//                }
//                if(!newCluster.isEmpty()) {
//                    newClusters.add(newCluster);
//                }
//            }
        }

        // expand the paths, i.e. add triple patterns to var nodes if exist
        for (Set<Triple> cluster : newClusters) {
            for(int i = 1; i < maxTreeDepth; i++) {
                Set<Triple> additionalTriples = new HashSet<>();
                cluster.stream().filter(triple -> triple.getObject().isVariable()).forEach(triple -> {
                    Collection<Triple> triples = var2IncomingTPs.get(Var.alloc(triple.getObject()));
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

    private static List<Query> splitIncoming(Query query) {
        // the target variable
        Var targetVar = query.getProjectVars().get(0);

        // mapping from var to incoming triple patterns
        Set<Triple> incomingTPs = queryUtils.extractIncomingTriplePatternsTrans(query, targetVar.asNode());

        // return if there are no incoming triple patterns
        if(incomingTPs.isEmpty()) {
            return Collections.emptyList();
        }

        // mapping from each variable to triple patterns in which they occur as object
        final Multimap<Var, Triple> var2IncomingTPs = HashMultimap.create();
        for (Triple tp : incomingTPs) {
            var2IncomingTPs.put(Var.alloc(tp.getObject()), tp);
        }

        // 1. get the incoming triple patterns of the target var that do not have
        // incoming triple patterns
        Set<Triple> fixedTriplePatterns = var2IncomingTPs.get(targetVar).stream()
                .filter(tp -> tp.getSubject().isConcrete() || !var2IncomingTPs.containsKey(Var.alloc(tp.getSubject())))
                .collect(Collectors.toSet());

        // split
        List<List<Triple>> fixedTpPartitions = Lists.partition(new ArrayList<>(fixedTriplePatterns), 7);

        // all other incoming triple patterns from target var become a new cluster
        Set<Set<Triple>> clusters = var2IncomingTPs.get(targetVar).stream()
                .filter(tp -> !fixedTriplePatterns.contains(tp))
                .map(Sets::newHashSet)
                .collect(Collectors.toSet());

        // add some fixed TPs to reduce resultset
        for (Set<Triple> cluster : clusters) {
            Triple representative = cluster.iterator().next();
            cluster.addAll(var2IncomingTPs.get(Var.alloc(representative.getSubject())));
            cluster.addAll(fixedTpPartitions.get(0));
        }

        fixedTpPartitions.forEach(p -> clusters.add(Sets.newHashSet(p)));

        // again split clusters to have only a maximum number of triple patterns
        int maxNrOfTriplePatternsPerQuery = 10;// number of outgoing triple patterns form the target var in each executed query
        Set<Set<Triple>> newClusters = new HashSet<>();
        
        for (Set<Triple> cluster : clusters) {
            Lists.partition(new ArrayList<>(cluster), 10).forEach(p -> newClusters.add(Sets.newHashSet(p)));

//            int cnt = 0;
//            for (Triple triple : cluster) {
//                if(triple.getObject().matches(targetVar)) {
//                    cnt++;
//                }
//            }
//
//            if(cnt > maxNrOfTriplePatternsPerQuery) {
//                Set<Triple> newCluster = new HashSet<>();
//                for (Triple triple : cluster) {
//                    if(triple.getObject().matches(targetVar)) {
//                        newCluster.add(triple);
//                    }
//                    if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
//                        newClusters.add(newCluster);
//                        newCluster = new HashSet<>();
//                    }
//                }
//                if(!newCluster.isEmpty()) {
//                    newClusters.add(newCluster);
//                }
//            }
        }

        // expand the paths, i.e. add triple patterns to var nodes if exist
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
        query = "BASE    <http://dbpedia.org/resource/>\n" +
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
                "  { ?s   dbo:director          ?x0 .\n" +
                "    ?x0  <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         rdf:type              dbo:Person .\n" +
                "    ?s   dbo:distributor       ?x1 .\n" +
                "    ?x1  dbo:type              <Division_(business)> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_film_studios> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Cinema_of_Southern_California> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Entertainment_companies_based_in_California> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Film_distributors_of_the_United_States> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Film_production_companies_of_the_United_States> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Hollywood_history_and_culture> ;\n" +
                "         rdf:type              dbo:Company .\n" +
                "    ?s   dbo:musicComposer     ?x2 .\n" +
                "    ?x2  <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Male_film_score_composers> .\n" +
                "    ?s   dbo:starring          ?x3 .\n" +
                "    ?x3  <http://purl.org/dc/terms/subject>  <./Category:American_people_of_English_descent> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         rdf:type              dbo:Person .\n" +
                "    ?s   dbo:starring          <Jesse_Eisenberg> ;\n" +
                "         dbo:starring          ?x4 .\n" +
                "    ?x4  dbo:occupation        <Actor> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:21st-century_American_male_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_male_film_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_male_television_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         rdf:type              dbo:Person .\n" +
                "    ?s   dbo:starring          ?x5 .\n" +
                "    ?x5  <http://purl.org/dc/terms/subject>  <./Category:20th-century_American_male_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:21st-century_American_male_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_male_film_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_male_television_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:American_male_voice_actors> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         <http://purl.org/dc/terms/subject>  <./Category:Primetime_Emmy_Award_winners> ;\n" +
                "         rdf:type              dbo:Person .\n" +
                "    ?s   dbo:writer            ?x6 .\n" +
                "    ?x6  <http://purl.org/dc/terms/subject>  <./Category:Living_people> ;\n" +
                "         rdf:type              dbo:Person .\n" +
                "    ?s   <http://purl.org/dc/terms/subject>  <./Category:American_films> ;\n" +
                "         rdf:type              dbo:Film\n" +
                "  }";

        query = "SELECT DISTINCT  ?s\n" +
                "WHERE\n" +
                "  { ?s   a                     <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/drugs> ;\n" +
                "         <http://www.w3.org/2002/07/owl#sameAs>  ?x0 ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0000737> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0002170> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0002622> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0003467> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0003862> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0003864> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0004093> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0004096> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0004604> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0006840> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0007859> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0008031> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0009763> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0009806> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0010200> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0011124> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0011616> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0011849> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0011991> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0012569> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0012833> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0013144> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0013390> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0013395> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0013428> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0013604> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0015230> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0015672> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0015967> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0016204> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0017152> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0017160> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0018021> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0018681> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0018965> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0020538> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0020565> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0021400> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0022346> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0022408> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0023218> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0024902> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0027497> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0027769> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0030193> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0030252> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0030305> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0030554> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0030794> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0031350> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0032285> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0032617> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0033774> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0035455> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0036572> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0038454> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0038990> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0039070> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0040264> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042029> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042109> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042267> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042571> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042798> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0042963> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0043094> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0085606> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0085631> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0085633> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0085649> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0149931> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0151315> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0231218> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0231528> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0497156> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0677481> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0702166> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C0917801> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect>  <http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/C1527304> .\n" +
                "    ?x0  a                     <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs> ;\n" +
                "         a                     <http://www4.wiwiss.fu-berlin.de/drugbank/vocab/resource/class/Offer> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/affectedOrganism>  \"Humans and other mammals\" ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/creationDate>  \"2005-06-13 13:24:05 UTC\" ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugType>  <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugtype/approved> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugType>  <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugtype/investigational> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugType>  <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugtype/smallMolecule> ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/reference>  \"\" ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/state>  \"Solid\" ;\n" +
                "         <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/structure>  \"1\"\n" +
                "  }";
        List<Query> queries = split(QueryFactory.create(query));
        queries.forEach(System.out::println);
    }
}
