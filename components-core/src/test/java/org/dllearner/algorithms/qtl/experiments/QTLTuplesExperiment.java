package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeComparator;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.tuples.QTLTuples;
import org.dllearner.algorithms.qtl.util.filters.MostSpecificTypesFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.vocabulary.DBpedia;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Lorenz Buehmann
 */
public class QTLTuplesExperiment {

    private final EvaluationDataset dataset;

    String baseIRI = DBpedia.BASE_IRI;
    PrefixMapping pm = DBpedia.PM;

    AbstractReasonerComponent reasoner;
    QueryExecutionFactory qef;

    QTLTuples qtl;

    int depth = 1;
    int numPosExamples = 5;
    int queryLimit = 1000;

    File out;

    public QTLTuplesExperiment(EvaluationDataset dataset) throws Exception{
        this.dataset = dataset;

        qef = dataset.ks.getQueryExecutionFactory();

        reasoner = new SPARQLReasoner(qef);
        reasoner.init();
        reasoner.prepareSubsumptionHierarchy();

        ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
//        cbdGen.setIgnoredProperties();
//        cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/property/", FOAF.NS));
//        cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));

        QueryTreeFactory tf = new QueryTreeFactoryBaseInv();

        qtl = new QTLTuples(dataset.ks.getQueryExecutionFactory());
        qtl.setCBDGenerator(cbdGen);
        qtl.setTreeFactory(tf);
        qtl.setMaxTreeDepth(depth);
        qtl.addTreeFilter(new MostSpecificTypesFilter(reasoner));
        qtl.addTreeFilter(new PredicateExistenceFilter() {
            @Override
            public boolean isMeaningless(Node predicate) {
                return predicate.getURI().startsWith("<http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
            }
        });
//        qtl.addTreeFilter(new PredicateExistenceFilterDBpedia(ks));
//        qtl.addTreeFilter(new MostSpecificTypesFilter(reasoner));
//        qtl.addTreeFilter(new PredicateExistenceFilter() {
//            @Override
//            public boolean isMeaningless(Node predicate) {
//                return predicate.getURI().startsWith("http://dbpedia.org/property/");
//            }
//        });

        out = new File("/tmp/qtl.out");
    }

    public void run() {
        dataset.sparqlQueries.forEach((id, query) -> {
            if(!id.equals("Query8")) return;
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
                    List<Map.Entry<RDFResourceTree, List<Node>>> solutions = qtl.run(examples);

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

    private List<List<RDFNode>> getExamples(Query query, QueryExecutionFactory qef) {
        List<List<RDFNode>> tuples = new ArrayList<>();

        query.setLimit(numPosExamples);
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

    private static SortedSet<Node> toSet(QuerySolution qs) {
        SortedSet<Node> nodes = new TreeSet<>(new NodeComparator());
        ArrayList<String> vars = Lists.newArrayList(qs.varNames());
        vars.forEach(var -> nodes.add(qs.get(var).asNode()));
        return nodes;
    }

    private Set<SortedSet<Node>> query(String qStr, QueryExecutionFactory qef) {
        Query query = QueryFactory.create(qStr);
        query.setLimit(queryLimit);
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

    private Stats evaluate(String query, String targetQuery, QueryExecutionFactory qef) {
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

    public static void main(String[] args) throws Exception{
        SparqlEndpoint endpoint = SparqlEndpoint.create("http://localhost:7200/repositories/lubm-inferred", Lists.newArrayList());
        LUBMEvaluationDataset ds = new LUBMEvaluationDataset(new File("/tmp/test"), endpoint);

        new QTLTuplesExperiment(ds).run();
    }
}
