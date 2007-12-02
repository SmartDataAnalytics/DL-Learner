package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Vector;

public class ExtractionAlgorithm {

	private Configuration Configuration;
	private Manipulator Manipulator;
	private int recursiondepth = 2;
	private boolean getAllBackground = true;

	public ExtractionAlgorithm(Configuration Configuration) {
		this.Configuration = Configuration;
		this.Manipulator = Configuration.getManipulator();

	}

	public Node getFirstNode(URI u) {
		return new InstanceNode(u);
	}

	public Vector<Node> expandAll(URI[] u, TypedSparqlQuery tsp) {
		Vector<Node> v = new Vector<Node>();
		for (URI one : u) {
			v.add(expandNode(one, tsp));
		}
		return v;
	}

	public Node expandNode(URI u, TypedSparqlQuery tsp) {
		Node n = getFirstNode(u);
		Vector<Node> v = new Vector<Node>();
		v.add(n);
		System.out.println("StartVector: " + v);
		// n.expand(tsp, this.Manipulator);
		// Vector<Node> second=
		for (int x = 1; x <= this.recursiondepth; x++) {

			Vector<Node> tmp = new Vector<Node>();
			while (v.size() > 0) {
				Node tmpNode = v.remove(0);
				System.out.println("Expanding " + tmpNode);
				Vector<Node> tmpVec = tmpNode.expand(tsp, this.Manipulator);

				tmp.addAll(tmpVec);
			}
			v = tmp;
			System.out.println("Rec: " + x + " with " + v);
		}
		if (this.getAllBackground) {
			Vector<Node> classes = new Vector<Node>();
			for (Node one : v) {
				if (one.isClass()) {
					classes.add(one);
				}
			}
			while (classes.size() > 0) {
				System.out.println(classes.size());
				classes.addAll(classes.remove(0).expand(tsp, this.Manipulator));
			}

		}
		return n;

	}

}
