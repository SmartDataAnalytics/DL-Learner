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
        queries.addAll(splitOutgoing(query));
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
                .map(tp -> Sets.newHashSet(tp))
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
                "  { <...All_That_Might_Have_Been...>\n" +
                "              dbo:genre            ?s .\n" +
                "    <A_Promise_(album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Adam_Leonard_(singer-songwriter)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Apache_Beat>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Area_(band)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Barry_Andrews_(musician)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Bill_Laswell>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Captain_Beefheart>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Celestial_(Circle_X_album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Cesarians_1>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Christian_Smith-Pancorvo>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Dan_Monti>  dbo:genre         ?s .\n" +
                "    <Daniel_Patrick_Quinn>\n" +
                "              dbo:genre            ?s .\n" +
                "    <David_Singleton>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Deluxe_(Harmonia_album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Demetrio_Stratos>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Der_Panther>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Disco_Volante>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Doktor_Spira_i_Ljudska_Bića>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Donald_Knaack>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Electric_Sea>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Eraldo_Bernocchi>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Exit_Project>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Fabulous_Muscles>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Fancy_(Idiot_Flesh_album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Faun_Fables>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Ganymed_(band)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Grice_Peters>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Grizzly_Bear_(band)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Hanne_Hukkelberg>\n" +
                "              dbo:genre            ?s .\n" +
                "    <I_Am_Spoonbender>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Idiot_Flesh>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Isabella_Summers>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Jarboli>  dbo:genre           ?s .\n" +
                "    <Knife_Play>  dbo:genre        ?s .\n" +
                "    <Kokan_Popović>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Kozmetika>  dbo:genre         ?s .\n" +
                "    <La_Forêt_(album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Laurie_Anderson>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Life_and_Live>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Live_1974>  dbo:genre         ?s .\n" +
                "    <Lou_Reed>  dbo:genre          ?s .\n" +
                "    <Luke_Chrisinger>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Marionetki>  dbo:genre        ?s .\n" +
                "    <Mariya_Ocher>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Mellowosity>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Model_A_(band)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Monk_Time>  dbo:genre         ?s .\n" +
                "    <Musik_von_Harmonia>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Nico>    dbo:genre            ?s .\n" +
                "    <Not_Here_/_Not_Now>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Odbrana_i_poslednji_dani>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Owen_Pallett>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Pato_Fu>  dbo:genre           ?s .\n" +
                "    <Peter_Gabriel>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Presing>  dbo:genre           ?s .\n" +
                "    <Ryland_Bouchard>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Scott_Walker_(singer)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Silver_Monk_Time>\n" +
                "              dbo:genre            ?s .\n" +
                "    <So_Far_(Faust_song)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Spur_of_the_Moment_(album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Teapot_Industries>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Technodon>  dbo:genre         ?s .\n" +
                "    <The_Air_Force_(album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Bridgeheads>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Cesarians>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Gourishankar>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Nothing_Show>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Vicar_(music_producer,_The_Vicar_Chronicles)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <The_Walking>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Tilt_(Scott_Walker_album)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Trey_Gunn>  dbo:genre         ?s .\n" +
                "    <Tsk_Tsk_Tsk>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Veda_Hille>  dbo:genre        ?s .\n" +
                "    <Vib_Gyor>  dbo:genre          ?s .\n" +
                "    <Vlada_Divljan>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Vows_(band)>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Winter_Live_1981>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Women_as_Lovers>\n" +
                "              dbo:genre            ?s .\n" +
                "    <World_Unreal>\n" +
                "              dbo:genre            ?s .\n" +
                "    <Xiu_Xiu>  dbo:genre           ?s .\n" +
                "    <X∞Multiplies>\n" +
                "              dbo:genre            ?s .\n" +
                "    <New_wave_music>\n" +
                "              dbo:stylisticOrigin  ?s\n" +
                "  }";
        List<Query> queries = split(QueryFactory.create(query));
        queries.forEach(System.out::println);
    }
}
