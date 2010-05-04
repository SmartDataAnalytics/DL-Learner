/* SimpleSelectionFunction - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package org.dllearner.tools.ore.explanation.relevance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleSelectionFunction {
	private Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
	public Map<OWLAxiom, Set<OWLEntity>> axiomEntities = new HashMap<OWLAxiom, Set<OWLEntity>>();


	public void init(OWLOntology onto) {
		allAxioms.clear();
		allAxioms = onto.getAxioms();

		initAxiomEntities(allAxioms);
	}

	public void init(HashSet<OWLAxiom> axioms) {
		allAxioms.clear();
		allAxioms.addAll(axioms);
		initAxiomEntities(allAxioms);
	}

	public void initAxiomEntities(Set<OWLAxiom> axioms) {
		axiomEntities.clear();
		for (OWLAxiom ax : axioms) {
			axiomEntities.put(ax, ax.getSignature());
		}

	}

	public Set<OWLAxiom> getRelatedAxioms(OWLEntity concept) {
		Set<OWLAxiom> relatedAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : allAxioms) {
			if (connected(concept, ax)) {
				relatedAxioms.add(ax);
			}
		}

		return relatedAxioms;
	}

	public Vector<OWLAxiom> getAllRelatedAxioms(OWLEntity concept) {
		Vector<OWLAxiom> allRelated = new Vector<OWLAxiom>();
		Set<OWLAxiom> relatedAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> relatedAxioms_all = new HashSet<OWLAxiom>();
		for (relatedAxioms = getRelatedAxioms(concept); relatedAxioms.size() > 0; relatedAxioms = getRelatedAxioms(relatedAxioms_all)) {
			allRelated
					.addAll((HashSet<OWLAxiom>) ((HashSet<OWLAxiom>) relatedAxioms)
							.clone());
			relatedAxioms_all
					.addAll((HashSet<OWLAxiom>) ((HashSet<OWLAxiom>) relatedAxioms)
							.clone());
		}
		return allRelated;
	}

	public Set<OWLAxiom> getRelatedAxioms(Set<OWLAxiom> originalAxioms_in) {
		Set<OWLAxiom> relatedAxioms = new HashSet<OWLAxiom>();
		Set<OWLEntity> originalEntities = new HashSet<OWLEntity>();
		Set<OWLAxiom> originalAxioms = new HashSet<OWLAxiom>(originalAxioms_in);
		for (OWLAxiom ax : originalAxioms) {
			if (axiomEntities.containsKey(ax)) {
				originalEntities.addAll(axiomEntities.get(ax));
			}
		}
		for (OWLEntity ent : originalEntities) {
			for (OWLAxiom ax : allAxioms) {
				if (!originalAxioms.contains(ax) && connected(ent, ax)) {
					relatedAxioms.add(ax);
				}
			}
		}
		relatedAxioms.removeAll(originalAxioms_in);
		return relatedAxioms;
	}

	public Vector<OWLAxiom> getAllRelatedAxioms(Set<OWLAxiom> originalAxioms_in) {
		Vector<OWLAxiom> allRelated = new Vector<OWLAxiom>();
		Set<OWLAxiom> relatedAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> relatedAxioms_all = new HashSet<OWLAxiom>();
		for (relatedAxioms = getRelatedAxioms(originalAxioms_in); relatedAxioms
				.size() > 0; relatedAxioms = getRelatedAxioms(relatedAxioms_all)) {
			allRelated
					.addAll((HashSet<OWLAxiom>) ((HashSet<OWLAxiom>) relatedAxioms)
							.clone());
			relatedAxioms_all
					.addAll((HashSet<OWLAxiom>) ((HashSet<OWLAxiom>) relatedAxioms)
							.clone());
		}
		return allRelated;
	}

	public boolean connected(HashSet<OWLAxiom> axioms, OWLAxiom a) {
		boolean flag = false;
		for (OWLAxiom ax : axioms) {
			if (connected(ax, a) > 0) {
				flag = true;
				break;
			}
		}

		return flag;
	}

	public int connected(OWLAxiom a1, OWLAxiom a2) {
		int num = 0;
		Set<OWLEntity> ents1 = axiomEntities.get(a1);
		Set<OWLEntity> ents2 = axiomEntities.get(a2);
		for(OWLEntity ent : ents1){
			if(ents2.contains(ent)){
				num++;
			}
		}
		
		return num;
	}

	public boolean connected(OWLEntity c, OWLAxiom a) {
		Set<OWLEntity> entities = axiomEntities.get(a);
		if (entities.contains(c))
			return true;
		return false;
	}
}
