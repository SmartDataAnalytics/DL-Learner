package org.dllearner.algorithms.qtl.experiments;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.sparql.core.Var;

public class Path {
	
	OWLClass cls;
	List<Set<String>> properties;
	String object;

	public Path(OWLClass cls, List<Set<String>> properties, String object) {
		this.cls = cls;
		this.properties = properties;
		this.object = object;
	}

	public OWLClass getOWLClass() {
		return cls;
	}

	public List<Set<String>> getProperties() {
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
		Set<String> lastPropertyCluster = properties.get(properties.size() - 1);
		Var joinVar = targetVar;
		for (int i = 0; i < properties.size() - 1; i++) {
			Set<String> cluster = properties.get(i);
			for (String property : cluster) {
				Var joinVarTmp = Var.alloc("o" + i);
				query += joinVar + " <" + property + "> " + joinVarTmp + " .";
				joinVar = joinVarTmp;
			}
		}
		for (String lastProperty : lastPropertyCluster) {
			query += joinVar + " <" + lastProperty + "> <" + object + "> .";
		}

		query += "}";
		return query;
	}

	public String asSPARQLPathQuery(Var targetVar) {
		String query = "SELECT DISTINCT " + targetVar + " WHERE {";
		query += targetVar + " a <" + cls.toStringID() + "> ;";
		
		Set<String> firstPropertyCluster = properties.get(0);
		for (String firstProperty : firstPropertyCluster) {
			query += " <" + firstProperty + "> ";
		}
		
		for (int i = 1; i < properties.size(); i++) {
			for (String p : properties.get(i)) {
				query += "/" + "<" + p + ">";
			}
		}
		query += " <" + object + "> .";
		query += "}";
		return query;
	}
}