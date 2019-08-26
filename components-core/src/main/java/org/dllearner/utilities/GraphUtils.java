package org.dllearner.utilities;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.RandomWalkIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import static org.apache.jena.sys.JenaSystem.forEach;

/**
 *
 * Utility methods working on graph level by means of JGraphT API.
 *
 * @author Lorenz Buehmann
 */
public class GraphUtils {

    public static Graph<OWLIndividual, DefaultEdge> aboxToGraph(OWLOntology ont) {

        Set<OWLObjectPropertyAssertionAxiom> axioms = ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);

        Graph<OWLIndividual, DefaultEdge> g = GraphTypeBuilder
                .directed()
                .allowingMultipleEdges(true)
                .edgeClass(DefaultEdge.class)
                .vertexClass(OWLIndividual.class)
                .buildGraph();

        axioms.forEach(ax -> g.addEdge(ax.getSubject(), ax.getObject()));

        return g;
    }

    public static Graph<OWLIndividual, OWLPropertyEdge> aboxToLabeledGraph(OWLOntology ont) {

        Set<OWLObjectPropertyAssertionAxiom> axioms = ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);

        Graph<OWLIndividual, OWLPropertyEdge> g = GraphTypeBuilder
                .directed()
                .allowingMultipleEdges(true)
                .vertexClass(OWLIndividual.class)
                .edgeClass(OWLPropertyEdge.class)
                .buildGraph();

        axioms.forEach(ax -> {
            g.addVertex(ax.getSubject());
            g.addVertex(ax.getObject());
            g.addEdge(ax.getSubject(), ax.getObject(), new OWLPropertyEdge(ax.getProperty()));
        });

        return g;
    }

    static class OWLPropertyEdge extends LabeledEdge<OWLObjectPropertyExpression> {
        public OWLPropertyEdge(OWLObjectPropertyExpression label) {
            super(label);
        }
    }

    static class LabeledEdge<T> extends DefaultEdge {
        private T label;

        /**
         * Constructs a labeled edge
         *
         * @param label the label of the new edge.
         */
        public LabeledEdge(T label) {
            this.label = label;
        }

        /**
         * Gets the label associated with this edge.
         *
         * @return edge label
         */
        public T getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
        }
    }


    public static void main(String[] args) throws OWLOntologyCreationException {
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());

        OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("/home/user/work/datasets/poker/poker_straight_flush_p5-n347.owl"));
        OWLClass hand = OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://dl-learner.org/examples/uci/poker#Hand"));
        final String targetClass = "straight_flush";

        Graph<OWLIndividual, OWLPropertyEdge> g = aboxToLabeledGraph(ont);

        Set<OWLIndividual> startNodes = ont.getIndividualsInSignature().stream()
                .filter(ind -> ont.getAnnotationAssertionAxioms(ind.asOWLNamedIndividual().getIRI()).stream()
                        .map(OWLAnnotationAssertionAxiom::annotationValue)
                        .map(OWLAnnotationValue::asLiteral)
                        .anyMatch(lit -> lit.isPresent() && lit.get().getLiteral().equals(targetClass)))
                .limit(1)
                .collect(Collectors.toSet());

        int maxPathLength = 10;

        startNodes.forEach(node -> {

            // compute all path up to length
            List<GraphPath<OWLIndividual, OWLPropertyEdge>> paths = new AllPaths<>(g).getAllPaths(node,true, maxPathLength);

            // show all paths
            paths.forEach(System.out::println);

            // show all paths but just the edges
            List<List<OWLObjectPropertyExpression>> pathEdges = paths.stream()
                    .map(path -> path.getEdgeList().stream().map(LabeledEdge::getLabel).collect(Collectors.toList()))
                    .collect(Collectors.toList());
//            pathEdges.forEach(System.out::println);

            // show just the distinct list of the edge sequences
            List<List<OWLObjectPropertyExpression>> pathEdgesDistinct = new ArrayList<>(new HashSet<>(pathEdges));

            Comparator<List<OWLObjectPropertyExpression>> c = Comparator.<List<OWLObjectPropertyExpression>>comparingInt(List::size).thenComparing(Object::toString);

            Collections.sort(pathEdgesDistinct, c);
            pathEdgesDistinct.forEach(System.out::println);

        });
    }




    static class AllPaths<V, E> {
        private final Graph<V, E> graph;

        AllPaths(Graph<V, E> graph){
            this.graph = graph;
        }

        private List<GraphPath<V, E>> generatePaths(
                Set<V> sourceVertices, boolean simplePathsOnly,
                Integer maxPathLength)
        {
            /*
             * We walk forwards through the network from the source vertices, exploring all outgoing
             * edges whose minimum distances is small enough.
             */
            List<GraphPath<V, E>> completePaths = new ArrayList<>();
            Deque<List<E>> incompletePaths = new LinkedList<>();

            // Input sanity checking
            if (maxPathLength != null && maxPathLength < 0) {
                throw new IllegalArgumentException("maxPathLength must be non-negative if defined");
            }

            // Bootstrap the search with the source vertices
            for (V source : sourceVertices) {
                completePaths.add(GraphWalk.singletonWalk(graph, source, 0d));

                if (maxPathLength != null && maxPathLength == 0) {
                    continue;
                }

                for (E edge : graph.outgoingEdgesOf(source)) {
                    assert graph.getEdgeSource(edge).equals(source);

                    completePaths.add(makePath(Collections.singletonList(edge)));

                    if ((maxPathLength == null || maxPathLength > 1)) {
                        List<E> path = Collections.singletonList(edge);
                        incompletePaths.add(path);
                    }
                }
            }

            if (maxPathLength != null && maxPathLength == 0) {
                return completePaths;
            }

            // Walk through the queue of incomplete paths
            for (List<E> incompletePath; (incompletePath = incompletePaths.poll()) != null;) {
                Integer lengthSoFar = incompletePath.size();
                assert (maxPathLength == null) || (lengthSoFar < maxPathLength);

                E leafEdge = incompletePath.get(lengthSoFar - 1);
                V leafNode = graph.getEdgeTarget(leafEdge);

                Set<V> pathVertices = new HashSet<>();
                for (E pathEdge : incompletePath) {
                    pathVertices.add(graph.getEdgeSource(pathEdge));
                    pathVertices.add(graph.getEdgeTarget(pathEdge));
                }

                for (E outEdge : graph.outgoingEdgesOf(leafNode)) {
                    // Proceed if the outgoing edge is marked and the mark
                    // is sufficiently small
                    if (maxPathLength == null || lengthSoFar <= maxPathLength) {
                        List<E> newPath = new ArrayList<>(incompletePath);
                        newPath.add(outEdge);

                        // If requested, make sure this path isn't self-intersecting
                        if (simplePathsOnly && pathVertices.contains(graph.getEdgeTarget(outEdge))) {
                            continue;
                        }

                        GraphPath<V, E> completePath = makePath(newPath);
                        assert sourceVertices.contains(completePath.getStartVertex());
                        assert (maxPathLength == null) || (completePath.getLength() <= maxPathLength);
                        completePaths.add(completePath);

                        // If this path is short enough, consider further extensions of it
                        if ((maxPathLength == null) || (newPath.size() < maxPathLength)) {
                            incompletePaths.addFirst(newPath);
                        }
                    }
                }
            }

            assert incompletePaths.isEmpty();
            return completePaths;
        }

        /**
         * Transform an ordered list of edges into a GraphPath.
         *
         * The weight of the generated GraphPath is set to the sum of the weights of the edges.
         *
         * @param edges the edges
         *
         * @return the corresponding GraphPath
         */
        private GraphPath<V, E> makePath(List<E> edges)
        {
            V source = graph.getEdgeSource(edges.get(0));
            V target = graph.getEdgeTarget(edges.get(edges.size() - 1));
            double weight = edges.stream().mapToDouble(edge -> graph.getEdgeWeight(edge)).sum();
            return new GraphWalk<>(graph, source, target, edges, weight);
        }

        public List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, Integer maxPathLength) {
            return getAllPaths(
                    Collections.singleton(sourceVertex),
                    simplePathsOnly, maxPathLength);
        }

        public List<GraphPath<V, E>> getAllPaths(
                Set<V> sourceVertices, boolean simplePathsOnly,
                Integer maxPathLength)
        {
            if ((maxPathLength != null) && (maxPathLength < 0)) {
                throw new IllegalArgumentException("maxPathLength must be non-negative if defined");
            }

            if (!simplePathsOnly && (maxPathLength == null)) {
                throw new IllegalArgumentException(
                        "If search is not restricted to simple paths, a maximum path length must be set to avoid infinite cycles");
            }

            if ((sourceVertices.isEmpty())) {
                return Collections.emptyList();
            }

            // Generate all the paths
            return generatePaths(
                    sourceVertices, simplePathsOnly, maxPathLength);
        }
    }
}
