package org.dllearner.algorithms.qtl.operations.tuples;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.logging.log4j.LogManager;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.rendering.Edge;
import org.dllearner.algorithms.qtl.datastructures.rendering.Vertex;
import org.dllearner.algorithms.qtl.exception.QTLException;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.operations.traversal.PreOrderTreeTraversal;
import org.dllearner.algorithms.qtl.operations.traversal.TreeTraversal;
import org.dllearner.algorithms.qtl.util.filters.AbstractTreeFilter;
import org.dllearner.algorithms.qtl.util.filters.MostSpecificTypesFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilterDBpedia;
import org.dllearner.algorithms.qtl.util.filters.SymmetricPredicatesFilter;
import org.dllearner.algorithms.qtl.util.vocabulary.DBpedia;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.*;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.QueryUtils;
import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
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

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(QTLTuples.class);

    private final QueryExecutionFactory qef;

    private ConciseBoundedDescriptionGenerator cbdGen;
    private QueryTreeFactory treeFactory;
    private LGGGenerator lggGenerator;

    // used for rendering
    private PrefixMapping pm;
    private String baseIRI;

    private int maxTreeDepth = 1;

    private Set<AbstractTreeFilter<RDFResourceTree>> treeFilters = new LinkedHashSet<>();
    public boolean addTreeFilter(AbstractTreeFilter<RDFResourceTree> treeFilter) {
        return treeFilters.add(treeFilter);
    }
    public boolean removeTreeFilter(AbstractTreeFilter<RDFResourceTree> treeFilter) {
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
    public void run(List<Node> tuple1, List<Node> tuple2) {
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
    public List<Map.Entry<RDFResourceTree, List<Node>>> run(List<List<Node>> tuples) {
        Objects.requireNonNull(tuples,"Tuples must not be null");

        // sanity checks first
        checkInput(tuples);

        log.info("input tuples {}", tuples.stream().map(Object::toString).collect(Collectors.joining("\n")));

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


        List<Map<String, Map.Entry<RDFResourceTree, List<Node>>>> tuple2Trees = tuples.stream().map(this::connect).collect(toList());
//        List<Map<String, Map.Entry<RDFResourceTree, List<Node>>>> tuple2Trees = tuples.stream().map(this::computeConnectedTrees).collect(toList());

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
                .flatMap(entry -> {
                    log.debug("computing LGG for " + entry.getKey());

                    List<Map.Entry<RDFResourceTree, List<Node>>> list = entry.getValue();

                    List<RDFResourceTree> trees = list.stream().map(Map.Entry::getKey).collect(toList());

                    List<Node> nodes2Select = list.get(0).getValue();

                    trees.forEach(t -> log.trace("tree:\n{}", t::getStringRepresentation));

                    RDFResourceTree lgg = lggGenerator.getLGG(trees);
                    log.debug("lgg:\n{}", lgg::getStringRepresentation);

                    if(lgg.isResourceNode()) {
                        log.warn("lgg was not generalizing with root {}", lgg);
                        return Stream.empty();
                    }

//            System.out.println("LGG\n" + lgg.getStringRepresentation());
//            System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg, nodes2Select, null, PrefixMapping.Standard));

                    return Stream.of(Maps.immutableEntry(lgg, nodes2Select));
                })
                .collect(Collectors.toList());

        return solutions;
    }

    private List<Map.Entry<RDFResourceTree, List<Node>>> runSingleNodeTuples(List<List<Node>> tuples) {
        // map nodes to trees
        List<RDFResourceTree> trees = tuples.stream()
                .flatMap(Collection::stream) // flatten list of lists of nodes
                .map(this::asTree) // map node to tree
                .map(Optional::get)
                .collect(Collectors.toList());

        // compute LGG
        RDFResourceTree lgg = lggGenerator.getLGG(trees);
        log.debug("lgg:\n{}", lgg::getStringRepresentation);

        return Collections.singletonList(Maps.immutableEntry(lgg, Collections.emptyList()));
    }

    private void checkInput(List<List<Node>> tuples) {
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

        for (AbstractTreeFilter<RDFResourceTree> f : treeFilters) {
            f.setNodes2Keep(nodes2Keep);
            filteredTree = f.apply(filteredTree);
        }

        return filteredTree;
    }

//    private Map<Node, RDFResourceTree> asTrees(Set<Node> nodes) {
//
//    }

    private boolean useLiteralData = true;

    public void setUseLiteralData(boolean useLiteralData) {
        this.useLiteralData = useLiteralData;
    }

    int cnt = 0;

    private Map<String, Map.Entry<RDFResourceTree, List<Node>>> connect(List<Node> tuple) {
        log.debug("generating connected tree for tuple {}", tuple);

        // filter URI resources
        Set<String> resources = tuple.stream()
                .filter(Node::isURI)
                .map(Node::getURI)
                .collect(Collectors.toSet());

        // map to one large model
        Model model = cbdGen.getConciseBoundedDescription(resources);
        if(model.isEmpty()) {
            throw new RuntimeException(new QTLException("Could not get data for tuple " + tuple));
        }
        log.debug("#triples:{}", model.size());

//        PseudoGraphJenaGraph g = new PseudoGraphJenaGraph(model.getGraph());

//        List<RDFNode> steinerNodes = tuple.stream().map(model::asRDFNode).collect(Collectors.toList());
//        SteinerTreeGeneric<RDFNode, Statement> steinerTreeGen = new SteinerTreeGeneric<>(g, steinerNodes,
//                new EdgeFactoryJenaModel(model, anyProp));
//        WeightedMultigraph<RDFNode, Statement> steinerTree = steinerTreeGen.getDefaultSteinerTree();

//        GraphMLExporter<Node, Triple> exporter = new GraphMLExporter<>(
//                Node::toString, n -> FmtUtils.stringForNode(n, pm),
//                new IntegerComponentNameProvider<>(), e -> FmtUtils.stringForNode(e.getPredicate(), pm));
//        try {
//            exporter.exportGraph(g, new FileWriter(new File("/tmp/steiner_tree_" + cnt++ + ".graphml")));
//        } catch (IOException | ExportException e) {
//            log.error("failed to write graph to file", e);
//        }

        List<Node> nodes = new ArrayList<>(tuple);

        // starting from each node n, create the tree with n as root
        Map<String, Map.Entry<RDFResourceTree, List<Node>>> result = new TreeMap<>();
        tuple.stream()
                .filter(Node::isURI)
                .forEach(node -> {
                    RDFResourceTree tree = treeFactory.getQueryTree(node.getURI(), model, 3);
                    Set<Node> nodes2Select = new LinkedHashSet<>(nodes);
//                    nodes.remove(node);
//                    Set<String> keys = keys(tree, asNodes(nodes));

//                    System.out.println(nodes);
//                    System.out.println(node);
//                    System.out.println(tree.getStringRepresentation());
//                    System.out.println(QueryTreeUtils.getNodes(tree).containsAll(nodes));

                    String key = Integer.toString(nodes.indexOf(node));
                    if(QueryTreeUtils.getNodeLabels(tree).containsAll(nodes)) {
                        nodes2Select.remove(node);

                        Set<Node> nodes2Project = new LinkedHashSet<>();
                        nodes2Select.forEach(n -> {
                            Node anchor = NodeFactory.createBlankNode("var" + tuple.indexOf(n));
                            getMatchingTreeNodes(tree, n).forEach(child -> {
                                child.setAnchorVar(anchor);
                                nodes2Project.add(anchor);
                            });
                        });
                        log.debug("connected tree\n{}", tree::getStringRepresentation);
                        QueryTreeUtils.asGraph(tree, baseIRI, pm, new File(System.getProperty("java.io.tmpdir") + File.separator + "tree-" + FmtUtils.stringForNode(node, pm) + ".graphml"));
                        result.put(key, Maps.immutableEntry(tree, new ArrayList<>(nodes2Project)));
                    };



                });


        log.debug("got {} possible connected trees", result.size());
        return result;
    }

//    private Set<String> keys(RDFResourceTree tree, List<Node> nodes) {
//        List<RDFResourceTree> children = tree.getChildren();
//        String key = "" + nodes.indexOf(tree.getData());
//        children.stream().map(child -> {
//            int pos = nodes.indexOf(child.getData());
//            if( pos >= 0) {
//                key += pos + ""
//            }
//        });
//
//    }


    private Map<String, Map.Entry<RDFResourceTree, List<Node>>> computeConnectedTrees(List<Node> tuple) {

        // mapping to tree for each node in tuple
        Map<Node, Optional<RDFResourceTree>> mapping = mapping(tuple);

        Map<String, Map.Entry<RDFResourceTree, List<Node>>> key2Trees = new HashMap<>();

        mapping.forEach((node, tree) -> {
            if(node.isURI()) {
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
//                                treeNode.setData(newData);
                                treeNode.setAnchorVar(newData);
                                parent.addChild(treeNode, edge);
                                nodes2Select.add(newData);
                            }

                        });


                    }
                });
                if(modified.get() == 1) {
                    log.debug("connected tree({}):\n{}", () -> key, newTree::getStringRepresentation);

//                    QueryTreeUtils.asGraph(newTree, baseIRI, pm, new File("/tmp/tree-" + pm.shortForm(node.getURI()) + ".graphml"));
                    key2Trees.put(key.toString(), Maps.immutableEntry(newTree, nodes2Select));
                }
            }
        });

        return key2Trees;
    }

    /**
     * Find nodes matching the data in the given tree.
     */
    private List<RDFResourceTree> getMatchingTreeNodes(RDFResourceTree tree, Node node) {
        List<RDFResourceTree> treeNodes = new ArrayList<>();

        TreeTraversal<RDFResourceTree> treeTraversal = new PreOrderTreeTraversal<>(tree);
        treeTraversal.forEachRemaining(treeNode -> {
           if(treeNode.getData().matches(node)) {
               treeNodes.add(treeNode);
            }
        });

        return treeNodes;
    }

    private Optional<RDFResourceTree> asTree(Node node) {
        if (node.isURI()) {
            if(useIncomingTriples) {
                TreeBasedConciseBoundedDescriptionGenerator treeCBDGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
                try {
                    Model cbd = treeCBDGen.getConciseBoundedDescription(node.getURI(), CBDStructureTree.fromTreeString("root:[in:[out:[]],out:[]]"));
                    return Optional.of(treeFactory.getQueryTree(node.toString(), cbd, 2));
                } catch (Exception e) {
                    log.error("Failed to compute CBD for " + node, e);
                }
                return Optional.empty();
            } else {
                String iri = node.getURI();
                Model cbd = cbdGen.getConciseBoundedDescription(iri, maxTreeDepth);
                RDFResourceTree tree = treeFactory.getQueryTree(node.getURI(), cbd, maxTreeDepth);
                log.debug("tree({}):\n{}", node::toString, tree::getStringRepresentation);
                return Optional.of(tree);
            }
        } else {
            if(useIncomingTriples) {
                TreeBasedConciseBoundedDescriptionGenerator treeCBDGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
                try {
                    Model cbd = treeCBDGen.getConciseBoundedDescription(node.getLiteral(), CBDStructureTree.fromTreeString("root:[in:[out:[]]]"));
                    return Optional.of(treeFactory.getQueryTree(node.toString(), cbd, maxTreeDepth));
                } catch (Exception e) {
                    log.error("Failed to compute CBD for " + node, e);
                }
            } else {
                return Optional.of(new RDFResourceTree(node));
            }
            return Optional.empty();
        }
    }

    private LinkedHashMap<Node, Optional<RDFResourceTree>> mapping(List<Node> tuple) {
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

    public void setUseIncomingTriples(boolean useIncomingTriples) {
        this.useIncomingTriples = useIncomingTriples;
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

    public void setPrefixMapping(PrefixMapping pm) {
        this.pm = pm;
    }

    public void setBaseIRI(String baseIRI) {
        this.baseIRI = baseIRI;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("logFilename", "log4j2.properties");
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

        queryStr = "PREFIX  property: <http://dbpedia.org/property/>\n" +
                "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX  db:   <http://dbpedia.org/ontology/>\n" +
                "\n" +
                "SELECT DISTINCT  *\n" +
                "WHERE\n" +
                "  { ?musician  a                    db:MusicalArtist ;\n" +
                "              db:activeYearsStartYear  ?activeyearsstartyear ;\n" +
                "              db:associatedBand     ?associatedband ;\n" +
                "              db:birthPlace         ?birthplace ;\n" +
                "              db:genre              ?genre ;\n" +
                "              db:recordLabel        ?recordlable\n" +
                "  }";

        int limit = 10;

        Query query = QueryFactory.create(queryStr);
        query.setOffset(0);
        query.setLimit(limit);

        System.out.println("Input query:\n" + query);

        String baseIRI = DBpedia.BASE_IRI;
        PrefixMapping pm = DBpedia.PM;

        SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
        endpoint = SparqlEndpoint.create("http://localhost:7200/repositories/dbpedia?infer=false", Collections.emptyList());
        SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
        ks.init();
        AbstractReasonerComponent reasoner = new SPARQLReasoner(ks);
        reasoner.init();
        reasoner.prepareSubsumptionHierarchy();

        List<List<Node>> tuples = new ArrayList<>();
        QueryExecutionFactory qef = ks.getQueryExecutionFactory();
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            List<Var> projectVars = query.getProjectVars();
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                List<Node> tuple = new ArrayList<>();
                projectVars.forEach(var -> tuple.add(qs.get(var.getName()).asNode()));
                tuples.add(tuple);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        tuples = Lists.newArrayList(
//                        Lists.newArrayList(
//                                NodeFactory.createURI("http://dbpedia.org/resource/Brad_Pitt"),
//                                NodeFactory.createLiteral("1963-12-18", XSDDatatype.XSDdate)),
//                Lists.newArrayList(
//                        NodeFactory.createURI("http://dbpedia.org/resource/Tom_Hanks"),
//                        NodeFactory.createLiteral("1956-07-09", XSDDatatype.XSDdate))
//        );

        Set<String> ignoredProperties = DBpedia.BLACKLIST_PROPERTIES;

        ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
        cbdGen.setIgnoredProperties(ignoredProperties);
        cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/property/"));
        cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));

        QueryTreeFactory tf = new QueryTreeFactoryBaseInv();
        tf.setMaxDepth(2);

        int depth = 1;

        QTLTuples qtl = new QTLTuples(qef);
        qtl.setMaxTreeDepth(depth);
        qtl.setBaseIRI(baseIRI);
        qtl.setPrefixMapping(pm);
        qtl.setCBDGenerator(cbdGen);
        qtl.setTreeFactory(tf);


        List<AbstractTreeFilter<RDFResourceTree>> filters = Lists.newArrayList(
                new PredicateExistenceFilterDBpedia(ks)
                ,new MostSpecificTypesFilter(reasoner)
//                ,new PredicateExistenceFilter() {
//                    @Override
//                    public boolean isMeaningless(Node predicate) {
//                        return predicate.getURI().startsWith("http://dbpedia.org/property/") ||
//                                predicate.getURI().startsWith("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#")||
//                                predicate.getURI().startsWith("http://www.wikidata.org/entity/") ||
//                                predicate.getURI().startsWith(RDFS.getURI());
//                    }
//                }
        );

        List<Map.Entry<RDFResourceTree, List<Node>>> solutions = qtl.run(tuples);

        solutions.forEach(sol -> {
            RDFResourceTree tree = sol.getKey();
            List<Node> nodes2Select = sol.getValue();
            QueryTreeUtils.rebuildNodeIDs(tree);

            System.out.println("LGG\n" + tree.getStringRepresentation(
                    true,
                    RDFResourceTree.Rendering.INDENTED, baseIRI, pm, true));

            System.out.println("nodes to select:" + nodes2Select);
            for (AbstractTreeFilter<RDFResourceTree> filter : filters) {
                filter.setNodes2Keep(nodes2Select);
                tree = filter.apply(tree);
            }
            QueryTreeUtils.rebuildNodeIDs(tree);

            System.out.println("LGG (filtered)\n" + tree.getStringRepresentation(
                    false,
                    RDFResourceTree.Rendering.INDENTED, baseIRI, pm, true));
            tree = new SymmetricPredicatesFilter(Collections.singleton(NodeFactory.createURI("http://dbpedia.org/ontology/spouse"))).apply(tree);

            String learnedQuery = QueryTreeUtils.toSPARQLQueryString(tree, nodes2Select, baseIRI, pm);
            Query q = QueryFactory.create(learnedQuery);
            QueryUtils.prunePrefixes(q);
            System.out.println(q);

            Graph<Vertex, Edge> g = QueryTreeUtils.toGraph(tree, baseIRI, pm);
            
            QueryTreeUtils.asGraph(tree, baseIRI, pm, new File(System.getProperty("java.io.tmpdir") + File.separator + "lgg.graphml"));

        });


    }
}
