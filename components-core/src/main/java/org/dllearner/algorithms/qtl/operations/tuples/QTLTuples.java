package org.dllearner.algorithms.qtl.operations.tuples;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDFS;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.operations.traversal.PreOrderTreeTraversal;
import org.dllearner.algorithms.qtl.operations.traversal.TreeTraversal;
import org.dllearner.algorithms.qtl.util.filters.MostSpecificTypesFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilterDBpedia;
import org.dllearner.algorithms.qtl.util.filters.TreeFilter;
import org.dllearner.algorithms.qtl.util.vocabulary.DBpedia;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.*;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Experimental algorithm to generate SPARQL queries by example each of which is a tuple of RDF nodes, i.e.
 * either a resource or literal.
 *
 * For example
 * (:a 15) and (:b 16)
 * (:a :x "15-11-2000") and (:b :x "15-11-2000")
 *
 * The result should be a SPARQL query with n projection variables where n denotes the arity of the tuple (n-tuple).
 *
 * @author Lorenz Buehmann
 */
public class QTLTuples {

    private static final Logger log = LoggerFactory.getLogger(QTLTuples.class);

    private final QueryExecutionFactory qef;

    private ConciseBoundedDescriptionGenerator cbdGen;
    private QueryTreeFactory treeFactory;
    private LGGGenerator lggGenerator;

    private int maxTreeDepth = 1;

    private Set<TreeFilter<RDFResourceTree>> treeFilters = new LinkedHashSet<>();
    public boolean addTreeFilter(TreeFilter<RDFResourceTree> treeFilter) {
        return treeFilters.add(treeFilter);
    }
    public boolean removeTreeFilter(TreeFilter<RDFResourceTree> treeFilter) {
        return treeFilters.remove(treeFilter);
    }


    public QTLTuples(QueryExecutionFactory qef) {
        this.qef = qef;

        cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
        treeFactory = new QueryTreeFactoryBase();
        lggGenerator = new LGGGeneratorSimple();
    }

    /**
     * Run the QTL algorithm given the 2 tuples as input example.
     *
     * @param tuple1 the first example
     * @param tuple2 the second example
     */
    public void run(List<RDFNode> tuple1, List<RDFNode> tuple2) {
        Objects.requireNonNull(tuple1,"First tuple must not be null");
        Objects.requireNonNull(tuple2,"Second tuple must not be null");

        run(Lists.newArrayList(tuple1, tuple2));
    }

    /**
     * Run the QTL algorithm given the list of tuples as input examples.
     *
     * The elements of a tuple <code>t = (e_1, ..., e_n)</code> represent arbitrary RDF terms, i.e. each <code>e_i</code> can be
     * either an IRI, a literal, or a blank node.
     *
     * <p>
     *  Requirements:
     *  <ul>
     *      <li>at least 2 tuples </li>
     *      <li>a tuple must contain at least one element</li>
     *      <li>for all tuples the number of elements must be the same</li>
     *  </ul>
     * </p>
     *
     * @param tuples the examples
     */
    public List<Map.Entry<RDFResourceTree, List<Node>>> run(List<List<RDFNode>> tuples) {
        // sanity check first
        checkInput(tuples);

        log.debug("input tuples {}", tuples.stream().map(Object::toString).collect(Collectors.joining("||")));

        // handle case with tuples of length separately -> just use the LGG of the trees
        if(tuples.get(0).size() == 1) {
            return runSingleNodeTuples(tuples);
        }


        // 1. we have to retrieve data for each node
        // in particular each resource node
        // for literals it could be to complicated as
        // a) there are no outgoing triples and
        // b) the number of incoming triples could be too large as literals like numbers could be used anywhere as value

        // 2. for each mapping of trees, build graph(s) of connected trees


        List<Map<String, Map.Entry<RDFResourceTree, List<Node>>>> tuple2Trees = tuples.stream().map(this::computeConnectedTrees).collect(toList());

        // cluster by key
        Map<String, ArrayList<Map.Entry<RDFResourceTree, List<Node>>>> grouped = tuple2Trees.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey,
                        Collector.of(ArrayList::new, (s, p) -> s.add(p.getValue()), (s1, s2) -> {
                            s1.addAll(s2);
                            return s1;
                        })));

        // compute LGG per each key

        List<Map.Entry<RDFResourceTree, List<Node>>> solutions = grouped.entrySet().stream()
                .filter(e -> e.getValue().size() == tuples.size())
                .map(entry -> {

                    List<Map.Entry<RDFResourceTree, List<Node>>> list = entry.getValue();

                    List<RDFResourceTree> trees = list.stream().map(Map.Entry::getKey).collect(toList());

                    List<Node> nodes2Select = list.get(0).getValue();

//                    trees.forEach(t -> System.out.println(t.getStringRepresentation()));

                    RDFResourceTree lgg = lggGenerator.getLGG(trees);
                    log.debug("LGG (before filtering)\n {}", lgg.getStringRepresentation());
                    lgg = applyFilters(lgg, nodes2Select);

//            System.out.println("LGG\n" + lgg.getStringRepresentation());
//            System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg, nodes2Select, null, PrefixMapping.Standard));

                    return Maps.immutableEntry(lgg, nodes2Select);
                }).collect(Collectors.toList());

        return solutions;
    }

    private List<Map.Entry<RDFResourceTree, List<Node>>> runSingleNodeTuples(List<List<RDFNode>> tuples) {
        // map nodes to trees
        List<RDFResourceTree> trees = tuples.stream()
                .flatMap(Collection::stream) // flatten list of lists
                .map(this::asTree)
                .map(Optional::get)
                .collect(Collectors.toList());

        // compute LGG
        RDFResourceTree lgg = lggGenerator.getLGG(trees);
        log.debug("LGG (before filtering):\n{}", lgg.getStringRepresentation());
        lgg = applyFilters(lgg, Collections.emptyList());

        return Collections.singletonList(Maps.immutableEntry(lgg, Collections.emptyList()));
    }

    private void checkInput(List<List<RDFNode>> tuples) {
        Objects.requireNonNull(tuples,"Tuples must not be null");

        // check for at least 2 tuples
        if(tuples.size() < 2) {
            log.warn("Min. number of input tuples is 2.");
            throw new IllegalArgumentException("Min. number of input tuples is 2.");
        }

        // check for all tuples having same length
        boolean sameTupleLength = tuples.stream().mapToInt(List::size).distinct().count() == 1;
        if(!sameTupleLength) {
            log.warn("Not all tuples have the same length. Currently, this is required!");
            throw new IllegalArgumentException("Not all tuples have the same length. Currently, this is required!");
        }
    }

    private RDFResourceTree applyFilters(RDFResourceTree tree, List<Node> nodes2Keep) {
        RDFResourceTree filteredTree = tree;

        for (TreeFilter<RDFResourceTree> f : treeFilters) {
            if(f instanceof PredicateExistenceFilter) {
                ((PredicateExistenceFilter) f).setNodes2Keep(nodes2Keep);
            }
            filteredTree = f.apply(filteredTree);
        }

        return filteredTree;
    }

    private Map<String, Map.Entry<RDFResourceTree, List<Node>>> computeConnectedTrees(List<RDFNode> tuple) {

        // mapping to tree for each node in tuple
        Map<RDFNode, Optional<RDFResourceTree>> mapping = mapping(tuple);

        Map<String, Map.Entry<RDFResourceTree, List<Node>>> key2Trees = new HashMap<>();

        mapping.forEach((node, tree) -> {
            if(node.isURIResource()) {
                final StringBuilder key = new StringBuilder(tuple.indexOf(node));
                List<Node> nodes2Select = new ArrayList<>();

                RDFResourceTree newTree = new RDFResourceTree(tree.get());

                final AtomicInteger modified = new AtomicInteger(0);

                mapping.forEach((otherNode, otherTree) -> {
                    if(!node.equals(otherNode)) {
                        List<RDFResourceTree> matchingTreeNodes = getMatchingTreeNodes(newTree, otherNode);

                        if(!matchingTreeNodes.isEmpty()) {
                            modified.set(1);
                            key.append(tuple.indexOf(otherNode));

//                            log.info("Nodes {} occur in tree of {}", matchingTreeNodes, newTree);
                        }

                        // plugin tree of other node
                        matchingTreeNodes.forEach(treeNode -> {
                            if(treeNode.isResourceNode()) {
                                Node edge = treeNode.getEdgeToParent();
                                RDFResourceTree parent = treeNode.getParent();
                                // copy the tree that will be attached in the current tree
                                RDFResourceTree newChild = new RDFResourceTree(otherTree.get());
                                // replace the data with some anchor
                                Node newData = NodeFactory.createBlankNode("var" + tuple.indexOf(otherNode));
//                                newChild.setData(newData);
                                newChild.setAnchorVar(newData);

                                // attach the tree as child node
                                parent.replaceChild(treeNode, newChild, edge);

                                parent.addChild(newChild, edge);
                                nodes2Select.add(newData);
//                                System.out.println("TEST\n" + newTree.getStringRepresentation());
                            } else {
                                Node edge = treeNode.getEdgeToParent();
                                RDFResourceTree parent = treeNode.getParent();
                                parent.removeChild(treeNode, edge);
                                Node newData = NodeFactory.createBlankNode("var" + tuple.indexOf(otherNode));
                                treeNode.setData(newData);
                                parent.addChild(treeNode, edge);
                                nodes2Select.add(newData);
                            }

                        });


                    }
                });
                if(modified.get() == 1) {
                    log.debug("connected tree({}):\n{}", key, newTree.getStringRepresentation());
                    key2Trees.put(key.toString(), Maps.immutableEntry(newTree, nodes2Select));
                }
            }
        });

        return key2Trees;
    }

    /**
     * Find nodes matching the data in the given tree.
     */
    private List<RDFResourceTree> getMatchingTreeNodes(RDFResourceTree tree, RDFNode node) {
        List<RDFResourceTree> treeNodes = new ArrayList<>();

        TreeTraversal treeTraversal = new PreOrderTreeTraversal(tree);
        treeTraversal.forEachRemaining(treeNode -> {
           if(treeNode.getData().matches(node.asNode())) {
               treeNodes.add(treeNode);
            }
        });

        return treeNodes;
    }

    private Optional<RDFResourceTree> asTree(RDFNode node) {
        if (node.isURIResource()) {
            if(useIncomingTriples) {
                TreeBasedConciseBoundedDescriptionGenerator treeCBDGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
                try {
                    Model cbd = treeCBDGen.getConciseBoundedDescription(node.asResource().getURI(), CBDStructureTree.fromTreeString("root:[in:[out:[]],out:[]]"));
                    return Optional.of(treeFactory.getQueryTree(node.toString(), cbd, 2));
                } catch (Exception e) {
                    log.error("Failed to compute CBD for " + node, e);
                }
                return Optional.empty();
            } else {
                String iri = node.asResource().getURI();
                Model cbd = cbdGen.getConciseBoundedDescription(iri, maxTreeDepth);
                return Optional.of(treeFactory.getQueryTree(node.asResource(), cbd, maxTreeDepth));
            }
        } else {
            if(useIncomingTriples) {
                TreeBasedConciseBoundedDescriptionGenerator treeCBDGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
                try {
                    Model cbd = treeCBDGen.getConciseBoundedDescription(node.asLiteral(), CBDStructureTree.fromTreeString("root:[in:[out:[]]]"));
                    return Optional.of(treeFactory.getQueryTree(node.toString(), cbd, maxTreeDepth));
                } catch (Exception e) {
                    log.error("Failed to compute CBD for " + node, e);
                }
            } else {
                return Optional.of(new RDFResourceTree(node.asNode()));
            }
            return Optional.empty();
        }
    }

    private LinkedHashMap<RDFNode, Optional<RDFResourceTree>> mapping(List<RDFNode> tuple) {
        return tuple.stream().collect(
                Collectors.toMap(
                        Function.identity(),
                        this::asTree,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new)
        );
    }

    private boolean useIncomingTriples = false;

    public boolean isUseIncomingTriples() {
        return useIncomingTriples;
    }

    /**
     * @param cbdGen the generator used to create the CBD for each resource in an input tuple
     */
    public void setCBDGenerator(ConciseBoundedDescriptionGenerator cbdGen) {
        this.cbdGen = cbdGen;
    }

    /**
     * @param treeFactory the factory used to create a tree from a resource and its set of triples (CBD)
     */
    public void setTreeFactory(QueryTreeFactory treeFactory) {
        this.treeFactory = treeFactory;
    }

    /**
     * @param lggGenerator the LGG generator used during the QTL algorithm
     */
    public void setLggGenerator(LGGGenerator lggGenerator) {
        this.lggGenerator = lggGenerator;
    }

    /**
     * @param maxTreeDepth max. tree depth used for data retrieval and query tree generation of the input examples.
     */
    public void setMaxTreeDepth(int maxTreeDepth) {
        this.maxTreeDepth = maxTreeDepth;
    }

    public static void main(String[] args) throws Exception {
//        org.apache.log4j.Logger.getRootLogger().getLoggerRepository().resetConfiguration();
//        org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
//        org.apache.log4j.Logger.getLogger(QTLTuples.class).setLevel(Level.DEBUG);

        String queryStr = "select * where { " +
                "?company a <http://dbpedia.org/ontology/Organisation> . " +
                "?company <http://dbpedia.org/ontology/foundationPlace> <http://dbpedia.org/resource/California> . " +
                "?product <http://dbpedia.org/ontology/developer> ?company . " +
                "?product a <http://dbpedia.org/ontology/Software> . }";

        queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "PREFIX dbp: <http://dbpedia.org/property/> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX dbont: <http://dbpedia.org/ontology/> " +
                "SELECT ?name ?date WHERE { " +
                "?name dbont:artist <http://dbpedia.org/resource/The_Beatles> . " +
                "?name rdf:type dbont:Album . " +
                "?name dbont:releaseDate ?date}" +
                " ORDER BY ?date";

        queryStr = "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "SELECT distinct ?b ?c WHERE { \n" +
                "?b rdf:type dbpedia:Film . \n" +
                "?c rdf:type dbpedia:Artist . \n" +
                "?b dbpedia:director <http://dbpedia.org/resource/Clint_Eastwood> ; \n" +
                "dbpedia:starring ?c . }";

        queryStr = "SELECT DISTINCT  *\n" +
                "WHERE\n" +
                "  { ?company  a                     <http://dbpedia.org/ontology/Organisation> ;\n" +
                "              <http://dbpedia.org/ontology/foundationPlace>  <http://dbpedia.org/resource/California> .\n" +
                "    ?product  <http://dbpedia.org/ontology/developer>  ?company ;\n" +
                "              a                     <http://dbpedia.org/ontology/Software>\n" +
                "  }";

        queryStr = "PREFIX  :     <http://dbpedia.org/resource/>\n" +
                "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                "PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX  dbpedia: <http://dbpedia.org/>\n" +
                "PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" +
                "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" +
                "\n" +
                "SELECT DISTINCT  ?name ?birth ?death ?person\n" +
                "WHERE\n" +
                "  { ?person  dbpedia2:birthPlace  :France ;\n" +
                "             dbo:birthDate        ?birth ;\n" +
                "             foaf:name            ?name ;\n" +
                "             dbo:deathDate        ?death\n" +
                "  }\n" +
                "ORDER BY ?name";
        queryStr = "PREFIX  :     <http://dbpedia.org/resource/>\n" +
                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX  db-ont: <http://dbpedia.org/ontology/>\n" +
                "PREFIX  dbpedia: <http://dbpedia.org/>\n" +
                "PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" +
                "SELECT DISTINCT  *\n" +
                "WHERE\n" +
                "  { ?select  rdf:type        db-ont:Film ;\n" +
                "             dbpedia2:title  ?ft .\n" +
                "    ?id      db-ont:imdbId   ?imdb_id\n" +
                "  }";

        queryStr = "SELECT DISTINCT  *\n" +
                "WHERE\n" +
                "  { <http://dbpedia.org/resource/United_States>\n" +
                "              a                     <http://dbpedia.org/ontology/Country> ;\n" +
                "              ?p                    ?o\n" +
                "  }";

        queryStr = "PREFIX  :     <http://dbpedia.org/resource/>\n" +
                "PREFIX  dbpedia-owl: <http://dbpedia.org/ontology/>\n" +
                "PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX  dbpedia: <http://dbpedia.org/>\n" +
                "PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" +
                "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" +
                "\n" +
                "SELECT DISTINCT  ?homepage\n" +
                "WHERE\n" +
                "  { ?person  rdf:type       dbpedia-owl:Place ;\n" +
                "             foaf:homepage  ?homepage\n" +
                "  }";

        int limit = 3;

        Query query = QueryFactory.create(queryStr);
        query.setOffset(0);
        query.setLimit(limit);

        System.out.println("Input query:\n" + query);

        SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
        endpoint = SparqlEndpoint.create("http://localhost:7200/repositories/repo-dbpedia", Collections.emptyList());
        SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
        ks.init();
        AbstractReasonerComponent reasoner = new SPARQLReasoner(ks);
        reasoner.init();
        reasoner.prepareSubsumptionHierarchy();

        List<List<RDFNode>> tuples = new ArrayList<>();
        QueryExecutionFactory qef = ks.getQueryExecutionFactory();
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
            e.printStackTrace();
        }

        Model m = ModelFactory.createDefaultModel();

        tuples = Lists.newArrayList(
                        Lists.newArrayList(
                                NodeFactory.createURI("http://dbpedia.org/resource/Brad_Pitt"),
                                NodeFactory.createLiteral("1963-12-18", XSDDatatype.XSDdate)),
                Lists.newArrayList(
                        NodeFactory.createURI("http://dbpedia.org/resource/Tom_Hanks"),
                        NodeFactory.createLiteral("1956-07-09", XSDDatatype.XSDdate))).stream().map(tuple -> tuple.stream()
                .map(m::asRDFNode)
                .collect(Collectors.toList()))
                .collect(Collectors.toList());

        Set<String> ignoredProperties = Sets.newHashSet(
                "http://dbpedia.org/ontology/abstract",
                "http://dbpedia.org/ontology/wikiPageID",
                "http://dbpedia.org/ontology/wikiPageRevisionID",
                "http://dbpedia.org/ontology/wikiPageID","http://www.w3.org/ns/prov#wasDerivedFrom", "http://dbpedia.org/ontology/wikiPageDisambiguates",
                "http://dbpedia.org/ontology/wikiPageExternalLink");

        ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
        cbdGen.setIgnoredProperties(ignoredProperties);
        cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/property/"));
        cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));

        int depth = 1;

        QTLTuples qtl = new QTLTuples(qef);
        qtl.setCBDGenerator(cbdGen);

        QueryTreeFactory tf = new QueryTreeFactoryBaseInv();
        qtl.setTreeFactory(tf);

        List<TreeFilter<RDFResourceTree>> filters = Lists.newArrayList(
                new PredicateExistenceFilterDBpedia(ks),
                new MostSpecificTypesFilter(reasoner),
                new PredicateExistenceFilter() {
                    @Override
                    public boolean isMeaningless(Node predicate) {
                        return predicate.getURI().startsWith("http://dbpedia.org/property/") ||
                                predicate.getURI().startsWith("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#")||
                                predicate.getURI().startsWith("http://www.wikidata.org/entity/") ||
                                predicate.getURI().startsWith(RDFS.getURI());
                    }
                }
        );

        for (TreeFilter<RDFResourceTree> filter : filters) {
//            qtl.addTreeFilter(filter);
        }

        List<Map.Entry<RDFResourceTree, List<Node>>> solutions = qtl.run(tuples);

        solutions.forEach(sol -> {
            RDFResourceTree tree = sol.getKey();
            List<Node> nodes2Select = sol.getValue();

            System.out.println("LGG\n" + tree.getStringRepresentation());

            String learnedQuery = QueryTreeUtils.toSPARQLQueryString(tree, nodes2Select, DBpedia.BASE_IRI, DBpedia.PM);
            System.out.println(learnedQuery);

        });


    }
}
