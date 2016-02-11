package org.dllearner.algorithms.versionspace;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author Lorenz Buehmann
 *         created on 2/11/16
 */
public class GraphUtils {

	public static <V, E> void writeToDot(DirectedGraph<V, E> g, String fileName) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(fileName);

		//System.out.println("Writing '" + fileName + "'");

		out.print("digraph \"DirectedGraph\" { \n graph [label=\"");
		out.print(g.toString());
		out.print("\", labelloc=t, concentrate=true]; ");
		out.print("center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");

		for (V node : g.vertexSet()) {
			out.print("   \"");
			out.print(getId(node));
			out.print("\" ");
			out.print("[label=\"");
			out.print(node.toString());
			out.print("\" shape=\"box\" color=\"blue\" ] \n");
		}

		for (V src : g.vertexSet()) {
			for (E e : g.outgoingEdgesOf(src)) {
				V tgt = g.getEdgeTarget(e);

				out.print(" \"");
				out.print(getId(src));
				out.print("\" -> \"");
				out.print(getId(tgt));
				out.print("\" ");
				out.print("[label=\"");
				out.print(e.toString());
				out.print("\"]\n");
			}
		}

		out.print("\n}");

		out.flush();
		out.close();
	}

	public static <V, E> void writeGraphML(DirectedGraph<V, E> graph, String fileName) {

		VertexNameProvider<V> vertexNameProvider = new VertexNameProvider<V>() {
			@Override
			public String getVertexName(V vertex) {
				return vertex.toString();
			}
		};

		EdgeNameProvider<E> edgeLabelProvider = new EdgeNameProvider<E>() {
			@Override
			public String getEdgeName(E edge) {
				return edge.toString();
			}
		};
		GraphMLExporter<V, E> exporter = new GraphMLExporter<>(new IntegerNameProvider<V>(),
																	   vertexNameProvider,
																	   new IntegerEdgeNameProvider<E>(),
																	   edgeLabelProvider);
		try {
			exporter.export(new FileWriter(fileName), graph);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getId(Object o) {
		return "" + System.identityHashCode(o);
	}
}
