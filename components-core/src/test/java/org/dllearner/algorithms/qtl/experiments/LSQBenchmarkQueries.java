package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeComparator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.tuples.QTLTuples;
import org.dllearner.algorithms.qtl.util.filters.MostSpecificTypesFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilterDBpedia;
import org.dllearner.algorithms.qtl.util.vocabulary.DBpedia;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.QueryUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.experimental.GraphTests;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public class LSQBenchmarkQueries {

    static int numQueries = 2000;
    static int limit = 10;

    public static void main(String[] args) throws Exception{
        SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
        endpoint = SparqlEndpoint.create("http://localhost:7200/repositories/repo-dbpedia", Collections.emptyList());
        SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
        ks.init();

        String baseIRI = DBpedia.BASE_IRI;
        PrefixMapping pm = DBpedia.PM;

        AbstractReasonerComponent reasoner = new SPARQLReasoner(ks);
        reasoner.init();
        reasoner.prepareSubsumptionHierarchy();

        Set<String> ignoredProperties = Sets.newHashSet(
                "http://dbpedia.org/ontology/abstract",
                "http://dbpedia.org/ontology/wikiPageID",
                "http://dbpedia.org/ontology/wikiPageRevisionID",
                "http://dbpedia.org/ontology/wikiPageID","http://www.w3.org/ns/prov#wasDerivedFrom", "http://dbpedia.org/ontology/wikiPageDisambiguates",
                "http://dbpedia.org/ontology/wikiPageExternalLink", FOAF.isPrimaryTopicOf.getURI());

        ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
        cbdGen.setIgnoredProperties(ignoredProperties);
        cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/property/", FOAF.NS));
        cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));

        QueryTreeFactory tf = new QueryTreeFactoryBaseInv();

        int depth = 1;

        QueryExecutionFactory qef = ks.getQueryExecutionFactory();

        QTLTuples qtl = new QTLTuples(qef);
        qtl.setCBDGenerator(cbdGen);
        qtl.setTreeFactory(tf);
        qtl.addTreeFilter(new PredicateExistenceFilterDBpedia(ks));
        qtl.addTreeFilter(new MostSpecificTypesFilter(reasoner));
        qtl.addTreeFilter(new PredicateExistenceFilter() {
            @Override
            public boolean isMeaningless(Node predicate) {
                return predicate.getURI().startsWith("http://dbpedia.org/property/");
            }
        });


        List<Query> queries = getQueries();

        File out = new File("/tmp/qtl.out");

        queries.forEach(query -> {
            try {
                query.setOffset(Query.NOLIMIT);
                query.setDistinct(true);

                System.out.println("#####################################################");
                System.out.println("#####################################################");

                System.out.println("Input query:\n" + query);

                StringBuilder sb = new StringBuilder();
                sb.append("#####################################################");
                sb.append("Input query:\n" + query);

                List<List<RDFNode>> examples = getExamples(query, qef);

                if(!examples.isEmpty()) {
                    List<Map.Entry<RDFResourceTree, List<Node>>> solutions = qtl.computeLGG(examples);

                    solutions.forEach(sol -> {
                        RDFResourceTree tree = sol.getKey();
                        List<Node> nodes2Select = sol.getValue();
                        System.out.println("LGG\n" + tree.getStringRepresentation());
                        sb.append("LGG\n" + tree.getStringRepresentation());

                        String learnedQuery = QueryTreeUtils.toSPARQLQueryString(tree, nodes2Select, baseIRI, pm);
                        System.out.println("Learned query\n" + learnedQuery);
                        sb.append("Learned query\n" + learnedQuery);

                        Stats stats = evaluate(learnedQuery, query.toString(), qef);

                        System.out.println(stats);
                        sb.append(stats);

                        try {
                            Files.asCharSink(out, com.google.common.base.Charsets.UTF_8, FileWriteMode.APPEND).write(sb.toString());
                        } catch (IOException e) {
//                            e.printStackTrace();
                        }
                    });
                }
            } catch(Exception e) {
                System.err.println("Processing input query failed: " + e.getMessage());
                e.printStackTrace();
            }

        });
    }

    private static List<List<RDFNode>> getExamples(Query query, QueryExecutionFactory qef) {
        List<List<RDFNode>> tuples = new ArrayList<>();

        query.setLimit(limit);
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                List<RDFNode> tuple = new ArrayList<>();
                ArrayList<String> vars = Lists.newArrayList(qs.varNames());
                Collections.sort(vars);
                vars.forEach(var -> tuple.add(qs.get(var)));
                tuples.add(tuple);
            }
        } catch (Exception e) {
            System.err.println("Failed to get examples for query.");
        }
        query.setLimit(Query.NOLIMIT);

        return tuples;
    }

    private static List<Query> getQueries() {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://lsq.aksw.org/sparql");

        String queryStr = "PREFIX sp: <http://spinrdf.org/sp#>\n" +
                "PREFIX lsqv: <http://lsq.aksw.org/vocab#>\n" +
                "SELECT ?resultSize ?tp ?query FROM <http://dbpedia.org>\n" +
                "WHERE {  \n" +
                "  ?id a sp:Select ;\n" +
                "      sp:text ?query ;\n" +
                "      lsqv:triplePatterns ?tp \n" +
                "\n" +
                "  FILTER NOT EXISTS {\n" +
                "  VALUES ?feature {lsqv:Union lsqv:Filter lsqv:Optional}\n" +
                "  ?id  lsqv:usesFeature ?feature\n" +
                "  }\n" +
                "\n" +
                "  ?id lsqv:resultSize ?resultSize \n" +
                "  FILTER(?resultSize >= 10)\n" +
                "  FILTER(?tp >= 2)\n" +
                "}";

        Query query = QueryFactory.create(queryStr);
        query.setLimit(numQueries);

        List<Query> queries = new ArrayList<>();

        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                String qStr = qs.getLiteral("query").getLexicalForm();
                try {

                    Query q = QueryFactory.create(qStr);
                    q.setLimit(Query.NOLIMIT);
                    q.setOffset(Query.NOLIMIT);

                    // we have to analyze the query here first
                    if(valid(q)) {
                        queries.add(q);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse LSQ query\n" + qStr);
                    e.printStackTrace();
                }
            }
        }

        // LSQ contains a lot of the same queries just beeing syntactically different
        dropIsomorphQueries(queries);

//        queries = Lists.newArrayList(QueryFactory.create("PREFIX  dbpr: <http://dbpedia.org/resource/>\n" +
//                "PREFIX  dbpo: <http://dbpedia.org/ontology/>\n" +
//                "\n" +
//                "SELECT DISTINCT  *\n" +
//                "WHERE\n" +
//                "  { ?company  a                     dbpo:Organisation ;\n" +
//                "              dbpo:foundationPlace  dbpr:California .\n" +
//                "    ?product  dbpo:developer        ?company ;\n" +
//                "              a                     dbpo:Software\n" +
//                "  }"));

//        Query q = QueryFactory.create("" +
//                "PREFIX  :     <http://dbpedia.org/resource/>\n" +
//                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "PREFIX  db-ont: <http://dbpedia.org/ontology/>\n" +
//                "PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" +
//                "SELECT DISTINCT  *\n" +
//                "WHERE\n" +
//                "  { ?select  rdf:type        db-ont:Film ;\n" +
//                "             dbpedia2:title  ?ft .\n" +
//                "    ?id      db-ont:imdbId   ?imdb_id\n" +
//                "  }");
//        analyze(q);
//        queries = Lists.newArrayList(q);

        System.out.println("Evaluating " + queries.size() + " queries...");
        return queries;
    }

    private static void dropIsomorphQueries(List<Query> queries) {
        for(Iterator<Query> it = queries.iterator(); it.hasNext();) {
            Query q1 = it.next();
            DirectedGraph<Node, QueryUtils.LabeledEdge> g1 = QueryUtils.asJGraphT(q1);
            Set<Triple> tp1 = QueryUtils.getTriplePatterns(q1);
            Set<Node> nodes1 = tp1.stream().flatMap(tp -> getNodes(tp).stream()).filter(n -> !n.isVariable()).collect(Collectors.toSet());

            boolean drop = false;

            for (Query q2 : new ArrayList<>(queries)) {
                if(!q1.equals(q2)) {

                    Set<Triple> tp2 = QueryUtils.getTriplePatterns(q1);
                    Set<Node> nodes2 = tp1.stream().flatMap(tp -> getNodes(tp).stream()).filter(n -> !n.isVariable()).collect(Collectors.toSet());
                    if(tp1.size() != tp2.size() || !nodes1.equals(nodes2) ) {
                        continue;
                    }

                    DirectedGraph<Node, QueryUtils.LabeledEdge> g2 = QueryUtils.asJGraphT(q2);

                    boolean isomorph = new VF2GraphIsomorphismInspector<>(g1, g2,
                                        (v1, v2) -> { return new NodeComparator().compare(v1, v2);},
                                        (e1, e2) -> {return new NodeComparator().compare(e1.getEdge(), e2.getEdge());})
                            .isomorphismExists();
                    if(isomorph) {
//                        System.out.println(">>>>>>>>>>>>>\n" + q1 + "\n" + q2 + "\n<<<<<<<<<<<<<<<<");
                        drop = true;
                        break;
                    }
                }
            }

            if(drop) {
                it.remove();
            }
        }
    }

    private static boolean valid(Query query) {
        // check for connectedness
        boolean connected = isConnected(query);
        if(!connected) {
//            System.err.println(query);
            return false;
        }

        // check for projection vars that do not exists in query pattern
        List<Var> vars = query.getProjectVars();
        Set<Triple> triplePatterns = QueryUtils.getTriplePatterns(query);
        Set<Node> nodes = triplePatterns.stream().flatMap(tp -> getNodes(tp).stream()).collect(Collectors.toSet());
        Set<Var> superflousVars = vars.stream().filter(var -> !nodes.contains(var.asNode())).collect(Collectors.toSet());
        if(!superflousVars.isEmpty()) {
//            System.err.println(query);
//            System.err.println("Proj. vars: " + superflousVars);
            return false;
        }

        // check if projection var doesn't refer to a predicate
        boolean hasPredicateProjVar = QueryUtils.getTriplePatterns(query).stream()
                .map(Triple::getPredicate)
                .filter(Node::isVariable)
                .map(Var::alloc)
                .anyMatch(vars::contains);

        return !hasPredicateProjVar;
    }

    private static Set<Node> getNodes(Triple tp) {
        return Sets.newHashSet(tp.getSubject(), tp.getPredicate(), tp.getObject());
    }

    private static boolean isConnected(Query query) {
        DirectedGraph<Node, QueryUtils.LabeledEdge> g = QueryUtils.asJGraphT(query);
        return GraphTests.isConnected(g);
    }

    private static SortedSet<Node> toSet(QuerySolution qs) {
        SortedSet<Node> nodes = new TreeSet<>(new NodeComparator());
        ArrayList<String> vars = Lists.newArrayList(qs.varNames());
        vars.forEach(var -> nodes.add(qs.get(var).asNode()));
        return nodes;
    }

    private static Set<SortedSet<Node>> query(String query, QueryExecutionFactory qef) {
        Set<SortedSet<Node>> result = new HashSet<>();
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                result.add(toSet(qs));
            }
        } catch(Exception e) {
            System.err.println("Query execution failed for\n" + query);
        }
        return result;
    }

    private static Stats evaluate(String query, String targetQuery, QueryExecutionFactory qef) {
        Set<SortedSet<Node>> nodes = query(query, qef);
//        System.out.println("S:" + nodes);
        Set<SortedSet<Node>> targetNodes = query(targetQuery, qef);
//        System.out.println("T:" + targetNodes);
        Sets.SetView<SortedSet<Node>> intersection = Sets.intersection(nodes, targetNodes);
        double precision = nodes.isEmpty() ? 0 : (double) intersection.size() / (double) nodes.size();
        double recall = nodes.isEmpty() ? 0 : (double) intersection.size() / (double) targetNodes.size();
        double f1 = precision + recall == 0 ? 0 : 2 * precision * recall / (precision + recall);

        return new Stats(precision, recall, f1);
    }

    private static class Stats {
        double p, r, f1;
        Stats(double p, double r, double f1) {
            this.p = p;
            this.r = r;
            this.f1 = f1;
        }

        @Override
        public String toString() {
            return String.format("P=%4.3f | R=%4.3f | F1=%4.3f", p, r, f1);
        }
    }
}
