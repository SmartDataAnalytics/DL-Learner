package org.dllearner.algorithms.qtl.experiments;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.sparql.core.Var;

public class Path {
	
	OWLClass cls;
	List<String> properties;
	String object;

	public Path(OWLClass cls, List<String> properties, String object) {
		this.cls = cls;
		this.properties = properties;
		this.object = object;
	}

	public OWLClass getOWLClass() {
		return cls;
	}

	public List<String> getProperties() {
		return properties;
	}

	public String getObject() {
		return object;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return cls + ": " + properties + "-->" + object;
	}

	public String asSPARQLQuery(Var targetVar) {
		String query = "SELECT DISTINCT " + targetVar + " WHERE {";
		query += targetVar + " a <" + cls.toStringID() + "> .";
		String lastProperty = properties.get(properties.size() - 1);
		Var joinVar = targetVar;
		for (int i = 0; i < properties.size() - 1; i++) {
			String property = properties.get(i);
			Var joinVarTmp = Var.alloc("o" + i);
			query += joinVar + "<" + property + "> " + joinVarTmp;
			joinVar = joinVarTmp;
		}
		query += joinVar + " <" + lastProperty + "> <" + object + "> .";

		return query;
	}

	public String asSPARQLPathQuery(Var targetVar) {
		String query = "SELECT DISTINCT " + targetVar + " WHERE {";
		query += targetVar + " a <" + cls.toStringID() + "> ;";
		String first = properties.get(0);
		query += "<" + first + ">";
		for (int i = 1; i < properties.size(); i++) {
			String p = properties.get(i);
			query += "/" + "<" + p + ">";
		}
		query += " <" + object + "> .";
		return query;
	}
}