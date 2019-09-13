package org.dllearner.utilities.graph;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphWalk;

/**
 * An algorithm to find all paths between two sets of nodes in a directed graph, with
 * options to search only simple paths and to limit the path length.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class AllPaths<V, E> {
    private final Graph<V, E> graph;

    public AllPaths(Graph<V, E> graph) {
        this.graph = graph;
    }

    private List<GraphPath<V, E>> generatePaths(
            Set<V> sourceVertices, boolean simplePathsOnly,
            Integer maxPathLength) {
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
        for (List<E> incompletePath; (incompletePath = incompletePaths.poll()) != null; ) {
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
     * <p>
     * The weight of the generated GraphPath is set to the sum of the weights of the edges.
     *
     * @param edges the edges
     * @return the corresponding GraphPath
     */
    private GraphPath<V, E> makePath(List<E> edges) {
        V source = graph.getEdgeSource(edges.get(0));
        V target = graph.getEdgeTarget(edges.get(edges.size() - 1));
        double weight = edges.stream().mapToDouble(graph::getEdgeWeight).sum();
        return new GraphWalk<>(graph, source, target, edges, weight);
    }

    public List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, Integer maxPathLength) {
        return getAllPaths(
                Collections.singleton(sourceVertex),
                simplePathsOnly, maxPathLength);
    }

    public List<GraphPath<V, E>> getAllPaths(
            Set<V> sourceVertices, boolean simplePathsOnly,
            Integer maxPathLength) {
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