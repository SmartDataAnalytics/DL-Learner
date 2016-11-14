package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.qtl.datastructures.impl.Tree;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.traverse.ClosestFirstIterator;

public class MinimumSteinerTreeApproximation<T> {
	/**
    * Computes an approximation of a directed minimum steiner tree.
    * @param graph The graph of which the minimum steiner tree should be computed
    * @param startVertex The vertex at which the directed minimum steiner tree should be rooted.
    * @param vertices A set of vertices that should be included in the minimum steiner tree.
    * @return A tree representation of the computed tree.
    */
	public static <V, E> Tree<V> approximateSteinerTree(DirectedWeightedPseudograph<V, E> graph, V startVertex, Set<V> vertices) {
		// 1. Construct the metric closure for the given set of vertices.
		DirectedWeightedPseudograph<V, MetricClosureEdge<V>> metricClosure = createMetricClosure(graph, vertices);
		
		// 2. Compute the edges that compose an aborescence of the metric closure (aka. directed minimum spanning tree).
		Set<MetricClosureEdge<V>> arborescenceEdges = minimumArborescenceEdges(metricClosure, startVertex);

		// 3. Reduce the metric closure by removing all edges not computed before.
		metricClosure.removeAllEdges(inverseEdgeSet(metricClosure, arborescenceEdges));

		// 4. Reconstruct a graph containing all vertices of the original graph.
		DirectedMultigraph<V, DefaultEdge> steinerTreeGraphApproximation = reconstructGraphFromMetricClosure(metricClosure);
		
		// 5. Construct a tree representation.
		Tree<V> steinerTree = constructSteinerTreeApproximation(startVertex, steinerTreeGraphApproximation);
		
		return steinerTree;
	}
	
	static class MetricClosureEdge<T> extends DefaultWeightedEdge {
		private static final long serialVersionUID = 1L;
		
		public final List<T> path;
		
		public MetricClosureEdge(List<T> path) {
			this.path = path;
		}
		
		public String toString() {
			return this.path.toString();// + " // "+this.weight;
		}
	}
	
	static <V,E> Set<E> inverseEdgeSet(DirectedWeightedPseudograph<V,E> graph, Set<E> edgeSet) {
		Set<E> inversedEdgeSet = new HashSet<>(graph.edgeSet());
		inversedEdgeSet.removeAll(edgeSet);
		return inversedEdgeSet;
	}
    /**
    * Constructs a Tree representation from this graph, by exploring it recursively. May only be called on a graph that does not contain cycles.
    * @param vertex The vertex at which to start exploring the graph.
    * @param graph The graph that should be explored.
    * @return A Tree representing the explored graph.
    */
	static <T> Tree<T> constructSteinerTreeApproximation(T vertex, DirectedMultigraph<T, DefaultEdge> graph) {
		Set<Tree<T>> children = new HashSet<>();
		
		if (!graph.containsVertex(vertex)) return null;
		
		for (DefaultEdge edge : graph.outgoingEdgesOf(vertex)) {
			T target = graph.getEdgeTarget(edge);
			
			children.add(constructSteinerTreeApproximation(target, graph));
		}

		return new Tree<T>(vertex, children);
	}
	static <V, E> Set<E> minimumArborescenceEdges(DirectedWeightedPseudograph<V, E> graph, V startVertex) {
		// Prim's algorithm
		ClosestFirstIterator<V, E> iterator = new ClosestFirstIterator<V, E>(graph, startVertex);
		iterator.setCrossComponentTraversal(false);
		
		while (iterator.hasNext()) iterator.next();
		
		Set<E> minimumSpanningTreeEdges = new HashSet<>();
		for (V vertex : graph.vertexSet()) {
			E edge = iterator.getSpanningTreeEdge(vertex);
			if (edge != null) minimumSpanningTreeEdges.add(edge);
		}
		
		return minimumSpanningTreeEdges;
	}
	static <T> void eliminateVertex(DirectedWeightedPseudograph<T, MetricClosureEdge<T>> graph, T vertex) {
		Set<MetricClosureEdge<T>> incomingEdges = new HashSet<>(graph.incomingEdgesOf(vertex));
		Set<MetricClosureEdge<T>> outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(vertex));
		
		for (MetricClosureEdge<T> incomingEdge : incomingEdges) {
			for (MetricClosureEdge<T> outgoingEdge : outgoingEdges) {
				List<T> path = new ArrayList<>();
				path.addAll(incomingEdge.path);
				path.add(vertex);
				path.addAll(outgoingEdge.path);
				MetricClosureEdge<T> newEdge = new MetricClosureEdge<>(path);
				graph.addEdge(graph.getEdgeSource(incomingEdge), graph.getEdgeTarget(outgoingEdge), newEdge);
				graph.setEdgeWeight(newEdge, graph.getEdgeWeight(incomingEdge) + graph.getEdgeWeight(outgoingEdge));
			}
		}
		
		graph.removeVertex(vertex);
	}
    /**
    * Computes the metric closure with a given set of vertices over a graph.
    * @param originalGraph The Graph over which the metric closure should be computed.
    * @param destinations A set of vertices which should exist in the metric closure.
    * @return A Graph that uses MetricClosureEdgeAnnotations that store the paths in the original graph to allow later reconstruction.
    */
	public static <T, E> DirectedWeightedPseudograph<T, MetricClosureEdge<T>> createMetricClosure(DirectedWeightedPseudograph<T, E> originalGraph, Set<T> destinations) {
		DirectedWeightedPseudograph<T, MetricClosureEdge<T>> metricClosure = new DirectedWeightedPseudograph<>(new EdgeFactory<T, MetricClosureEdge<T>>() {
			@Override
			public MetricClosureEdge<T> createEdge(T sourceVertex, T targetVertex) {
				return new MetricClosureEdge<>(new ArrayList<T>());
			}
		});
		
		for (E edge : originalGraph.edgeSet()) {
			T start = originalGraph.getEdgeSource(edge);
			T end = originalGraph.getEdgeTarget(edge);
			
			metricClosure.addVertex(start);
			metricClosure.addVertex(end);
			MetricClosureEdge<T> newEdge = metricClosure.addEdge(start, end);
			double edgeWeight = originalGraph.getEdgeWeight(edge);
			metricClosure.setEdgeWeight(newEdge, edgeWeight);
		}
		
		Set<T> removedVertices = new HashSet<T>(originalGraph.vertexSet());
		removedVertices.removeAll(destinations);
		
		for (T removedVertex : removedVertices) {
			eliminateVertex(metricClosure, removedVertex);
		}
		
		return metricClosure;
	}
    /**
    * Reconstructs the original graph from a metric closure (i.e. a graph that contains all vertices that were used in the edges of the metric closure).
    * @param metricClosure a metric closure
    * @return The reconstructed graph
    */
	public static <T> DirectedMultigraph<T, DefaultEdge> reconstructGraphFromMetricClosure(DirectedWeightedPseudograph<T, MetricClosureEdge<T>> metricClosure) {
		DirectedMultigraph<T, DefaultEdge> reconstructedGraph = new DirectedMultigraph<>(DefaultEdge.class);
		
		for (MetricClosureEdge<T> edge : metricClosure.edgeSet()) {
			T previousVertex = metricClosure.getEdgeSource(edge);
			T endVertex = metricClosure.getEdgeTarget(edge);
			reconstructedGraph.addVertex(previousVertex);
			reconstructedGraph.addVertex(endVertex);
			
			for (T subedge : edge.path) {
				if (reconstructedGraph.addVertex(subedge)) {
					reconstructedGraph.addEdge(previousVertex, subedge);
				}
				previousVertex = subedge;
			}
			
			reconstructedGraph.addEdge(previousVertex, endVertex);
		}
		
		return reconstructedGraph;
	}
}