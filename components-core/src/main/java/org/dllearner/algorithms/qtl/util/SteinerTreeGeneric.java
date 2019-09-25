package org.dllearner.algorithms.qtl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.graph.WeightedPseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteinerTreeGeneric<V, E> {

	private static final Logger logger = LoggerFactory.getLogger(SteinerTreeGeneric.class);


	Graph<V, E> graph;
	WeightedMultigraph<V, E> tree;
	List<V> steinerNodes;
	private final Class<? extends E> edgeClass;

	public SteinerTreeGeneric(Graph<V, E> graph, List<V> steinerNodes, Class<? extends E> edgeClass) {
		this.graph = graph;
		this.steinerNodes = steinerNodes;
		this.edgeClass = edgeClass;

		runAlgorithm();
	}

	/**
	 * Construct the complete undirected distance graph G1=(V1,EI,d1) from G and S.
	 */
	private Pseudograph<V, E> step1() {

		logger.debug("<enter");

		Pseudograph<V, E> g = new WeightedPseudograph<V, E>(edgeClass);

		for (V n : this.steinerNodes) {
			g.addVertex(n);
		}

		BellmanFordShortestPath<V, E> pathGen = new BellmanFordShortestPath<>(this.graph);

		for (V n1 : this.steinerNodes) {
			for (V n2 : this.steinerNodes) {

				if (n1.equals(n2))
					continue;

				if (g.containsEdge(n1, n2))
					continue;

				E e = g.addEdge(n1, n2);
				g.setEdgeWeight(e, pathGen.getPathWeight(n1, n2));

			}

		}

		logger.debug("exit>");

		return g;

	}

	/**
	 * Find the minimal spanning tree, T1, of G1. (If there are several minimal spanning trees, pick an arbitrary one.)
	 *
	 * @param g1
	 * @return
	 */
	private WeightedMultigraph<V, E> step2(Pseudograph<V, E> g1) {

		logger.debug("<enter");

		KruskalMinimumSpanningTree<V, E> mst = new KruskalMinimumSpanningTree<>(g1);

//    	logger.debug("Total MST Cost: " + mst.getSpanningTreeCost());

		Set<E> edges = mst.getSpanningTree().getEdges();

		WeightedMultigraph<V, E> g2 = new WeightedMultigraph<>(edgeClass);

		List<E> edgesSortedById = new ArrayList<>(edges);
//		edgesSortedById.sort();

		for (E edge : edgesSortedById) {
			g2.addVertex(g1.getEdgeSource(edge));
			g2.addVertex(g1.getEdgeTarget(edge));
			g2.addEdge(g1.getEdgeSource(edge), g1.getEdgeTarget(edge), edge);
		}

		logger.debug("exit>");

		return g2;
	}

	/**
	 * Construct the subgraph, Gs, of G by replacing each edge in T1 by its corresponding shortest path in G.
	 * (If there are several shortest paths, pick an arbitrary one.)
	 *
	 * @param g2
	 * @return
	 */
	private WeightedMultigraph<V, E> step3(WeightedMultigraph<V, E> g2) {

		logger.debug("<enter");

		WeightedMultigraph<V, E> g3 = new WeightedMultigraph<>(edgeClass);

		Set<E> edges = g2.edgeSet();
		DijkstraShortestPath<V, E> pathGen = new DijkstraShortestPath<>(this.graph);

		V source, target;

		for (E edge : edges) {
			source = g2.getEdgeSource(edge);
			target = g2.getEdgeTarget(edge);


			List<E> pathEdges = pathGen.getPath(source, target).getEdgeList();

			if (pathEdges == null)
				continue;

			for (int i = 0; i < pathEdges.size(); i++) {

				if (g3.edgeSet().contains(pathEdges.get(i)))
					continue;

				source = g2.getEdgeSource(pathEdges.get(i));
				target = g2.getEdgeTarget(pathEdges.get(i));

				if (!g3.vertexSet().contains(source))
					g3.addVertex(source);

				if (!g3.vertexSet().contains(target))
					g3.addVertex(target);

				g3.addEdge(source, target, pathEdges.get(i));
			}
		}

		logger.debug("exit>");

		return g3;
	}

	/**
	 * Find the minimal spanning tree, Ts, of Gs. (If there are several minimal spanning trees, pick an arbitrary one.)
	 *
	 * @param g3
	 * @return
	 */
	private WeightedMultigraph<V, E> step4(WeightedMultigraph<V, E> g3) {

		logger.debug("<enter");

		KruskalMinimumSpanningTree<V, E> mst = new KruskalMinimumSpanningTree<>(g3);

//    	logger.debug("Total MST Cost: " + mst.getSpanningTreeCost());

		Set<E> edges = mst.getSpanningTree().getEdges();

		WeightedMultigraph<V, E> g4 = new WeightedMultigraph<>(edgeClass);

		List<E> edgesSortedById = new ArrayList<>(edges);
//		Collections.sort(edgesSortedById);

		for (E edge : edgesSortedById) {
			g4.addVertex(g3.getEdgeSource(edge));
			g4.addVertex(g3.getEdgeTarget(edge));
			g4.addEdge(g3.getEdgeSource(edge), g3.getEdgeTarget(edge), edge);
		}

		logger.debug("exit>");

		return g4;
	}

	/**
	 * Construct a Steiner tree, Th, from Ts by deleting edges in Ts,if necessary,
	 * so that all the leaves in Th are Steiner points.
	 *
	 * @param g4
	 * @return
	 */
	private WeightedMultigraph<V, E> step5(WeightedMultigraph<V, E> g4) {

		logger.debug("<enter");

		WeightedMultigraph<V, E> g5 = g4;

		List<V> nonSteinerLeaves = new ArrayList<>();

		Set<V> vertexSet = g4.vertexSet();
		for (V vertex : vertexSet) {
			if (g5.degreeOf(vertex) == 1 && steinerNodes.indexOf(vertex) == -1) {
				nonSteinerLeaves.add(vertex);
			}
		}

		V source, target;
		for (int i = 0; i < nonSteinerLeaves.size(); i++) {
			source = nonSteinerLeaves.get(i);
			do {
				E e = g5.edgesOf(source).iterator().next();
				target = this.graph.getEdgeTarget(e);

				// this should not happen, but just in case of ...
				if (target.equals(source))
					target = g5.getEdgeSource(e);

				g5.removeVertex(source);
				source = target;
			} while (g5.degreeOf(source) == 1 && steinerNodes.indexOf(source) == -1);

		}

		logger.debug("exit>");

		return g5;
	}

	private void runAlgorithm() {

		logger.debug("<enter");

		logger.debug("step1 ...");
		Pseudograph<V, E> g1 = step1();
//		logger.info("after doing step 1 ....................................................................");
//		GraphUtil.printGraphSimple(g1);
//		GraphUtil.printGraph(g1);

		if (g1.vertexSet().size() < 2) {
			this.tree = new WeightedMultigraph<>(edgeClass);
			for (V n : g1.vertexSet()) this.tree.addVertex(n);
			return;
		}

		logger.debug("step2 ...");
		WeightedMultigraph<V, E> g2 = step2(g1);
//		logger.info("after doing step 2 ....................................................................");
//		GraphUtil.printGraphSimple(g2);
//		GraphUtil.printGraph(g2);


		logger.debug("step3 ...");
		WeightedMultigraph<V, E> g3 = step3(g2);
//		logger.info("after doing step 3 ....................................................................");
//		GraphUtil.printGraphSimple(g3);
//		GraphUtil.printGraph(g3);

		logger.debug("step4 ...");
		WeightedMultigraph<V, E> g4 = step4(g3);
//		logger.info("after doing step 4 ....................................................................");
//		GraphUtil.printGraphSimple(g4);
//		GraphUtil.printGraph(g4);


		logger.debug("step5 ...");
		WeightedMultigraph<V, E> g5 = step5(g4);
//		logger.info("after doing step 5 ....................................................................");
//		GraphUtil.printGraphSimple(g5);
//		GraphUtil.printGraph(g5);

		this.tree = g5;
		logger.debug("exit>");

		//Add all the force added vertices
//		for (V n : g1.vertexSet()) {
//			if (n.isForced())
//				this.tree.addVertex(n);
//		}
	}

	public WeightedMultigraph<V, E> getDefaultSteinerTree() {
		return this.tree;
	}
}