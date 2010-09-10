package org.dllearner.tools.ore.explanation.laconic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/*
 * This class computes the OPlus closure provided in 'Laconic and Precise Justifications in OWL' from 
 * Matthew Horridge, Bijan Parsia and Ulrike Sattler. A set of axioms is transformed into smaller and weaker axioms.
 */
public class OPlus implements OWLAxiomVisitorEx<Set<OWLAxiom>> {
	private OWLDataFactory dataFactory;
	private Beta beta;
	private Tau tau;
	private BottomTester bottomChecker;
	private TopTester topChecker;
	private Map<OWLAxiom, Set<OWLAxiom>> axiomsMap;

	public OPlus(OWLDataFactory dataFactory) {
		axiomsMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();
		this.dataFactory = dataFactory;
		beta = new Beta(dataFactory);
		tau = new Tau(dataFactory);
		bottomChecker = new BottomTester();
		topChecker = new TopTester();
	}

	public boolean isNothing(OWLClassExpression desc) {
		return ((Boolean) desc.accept(bottomChecker)).booleanValue();
	}

	public boolean isThing(OWLClassExpression desc) {
		return ((Boolean) desc.accept(topChecker)).booleanValue();
	}

	public void reset() {
		axiomsMap.clear();
	}

	public Map<OWLAxiom, Set<OWLAxiom>> getAxiomsMap() {
		return axiomsMap;
	}

	public Set<OWLAxiom> archive(OWLAxiom source, Set<OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {

			// Set<OWLAxiom> existing = (Set<OWLAxiom>)axiomsMap.get(axiom);
			// if(existing == null)
			// {
			// existing = new HashSet<OWLAxiom>();
			// axiomsMap.put(axiom, existing);
			// }
			// existing.add(source);
			if (!axiom.equals(source)) {
				Set<OWLAxiom> existing = (Set<OWLAxiom>) axiomsMap.get(axiom);
				if (existing == null) {
					existing = new HashSet<OWLAxiom>();
					axiomsMap.put(axiom, existing);
				}
				existing.add(source);
			}
		}
		return axioms;
	}

	public Set<OWLAxiom> archive(OWLAxiom source) {
		return archive(source, Collections.singleton(source));
	}

	@Override
	public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLClassExpression> tauAxioms = new HashSet<OWLClassExpression>();
		if (axiom.getSuperClass() instanceof OWLObjectIntersectionOf) {
			tauAxioms = new HashSet<OWLClassExpression>();
			for (OWLClassExpression desc : ((OWLObjectIntersectionOf) axiom.getSuperClass()).getOperands()) {

				if (desc.isAnonymous()) {
					tauAxioms.addAll(desc.accept(tau));
				} else {
					tauAxioms.add(desc);
				}
			}

			tauAxioms.add(dataFactory.getOWLThing());
		} else if (axiom.getSuperClass() instanceof OWLObjectUnionOf) {
			boolean allNamed = true;
			for (OWLClassExpression desc : ((OWLObjectUnionOf) axiom.getSuperClass()).getOperands()) {

				if (!desc.isAnonymous())
					continue;
				allNamed = false;
				break;
			}
			if (allNamed) {
				tauAxioms.add(axiom.getSuperClass());
			} else {
				tauAxioms = axiom.getSuperClass().accept(tau);
			}
		} else {
			tauAxioms = axiom.getSuperClass().accept(tau);
		}
		Set<OWLClassExpression> betaAxioms;
		if (axiom.getSubClass() instanceof OWLObjectUnionOf) {
			betaAxioms = new HashSet<OWLClassExpression>();
			for (OWLClassExpression desc : ((OWLObjectUnionOf) axiom.getSubClass()).getOperands()) {

				if (desc.isAnonymous()) {
					betaAxioms.addAll(desc.accept(beta));
				} else {
					betaAxioms.add(desc);
				}
			}

			betaAxioms.add(dataFactory.getOWLNothing());
		} else {
			betaAxioms = axiom.getSubClass().accept(beta);
		}
		for (OWLClassExpression tauDesc : tauAxioms) {

			if (!isThing(tauDesc)) {
				for (OWLClassExpression betaDesc : betaAxioms) {

					if (!isNothing(betaDesc) && !(tauDesc instanceof OWLObjectIntersectionOf)
							&& !(betaDesc instanceof OWLObjectUnionOf)) {
						axioms.add(dataFactory.getOWLSubClassOfAxiom(betaDesc, tauDesc));
					}
				}
			}
		}

		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
		boolean containAnonDescriptions = false;
		for (OWLClassExpression desc : axiom.getClassExpressions()) {

			if (!desc.isAnonymous())
				continue;
			containAnonDescriptions = true;
			break;
		}
		if (!containAnonDescriptions)
			return archive(axiom);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		List<OWLClassExpression> descs = new ArrayList<OWLClassExpression>(axiom.getClassExpressions());
		for (int i = 0; i < descs.size(); i++) {
			for (int j = i + 1; j < descs.size(); j++)
				axioms.addAll(dataFactory.getOWLSubClassOfAxiom(descs.get(i),
						dataFactory.getOWLObjectComplementOf(descs.get(j))).accept(this));

		}

		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLClassExpression desc : (Set<OWLClassExpression>) axiom.getDomain().accept(tau)) {

			if (!isThing(desc)) {
				axioms.add(dataFactory.getOWLObjectPropertyDomainAxiom((OWLObjectPropertyExpression) axiom
						.getProperty(), desc));
			}
		}
		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLObjectPropertyExpression prop1 : axiom.getProperties()) {
			for (OWLObjectPropertyExpression prop2 : axiom.getProperties()) {
				if (!prop1.equals(prop2)) {
					axioms.add(dataFactory.getOWLSubObjectPropertyOfAxiom(prop1, prop2));
				}
			}
		}

		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLIndividual ind1 : axiom.getIndividuals()) {
			for (OWLIndividual ind2 : axiom.getIndividuals()) {
				if (!ind1.equals(ind2)) {
					axioms.add(dataFactory.getOWLDifferentIndividualsAxiom(new OWLIndividual[] { ind1, ind2 }));
				}
			}
		}

		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLClassExpression range : axiom.getRange().accept(tau)) {
			if (!isThing(range)) {
				axioms.add(dataFactory.getOWLObjectPropertyRangeAxiom(
						(OWLObjectPropertyExpression) axiom.getProperty(), range));
			}
		}
		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDeclarationAxiom axiom) {
		return Collections.singleton((OWLAxiom) axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAnnotationAssertionAxiom axiom) {
		return Collections.singleton((OWLAxiom) axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLClassExpression desc : axiom.getClassExpression().accept(tau)) {
			if (!isThing(desc)) {
				axioms.add(dataFactory.getOWLClassAssertionAxiom(desc, axiom.getIndividual()));
			}
		}
		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLClassExpression desc1 : axiom.getClassExpressions()) {
			for (OWLClassExpression desc2 : axiom.getClassExpressions()) {
				if (!desc1.equals(desc2)) {
					axioms.addAll(dataFactory.getOWLSubClassOfAxiom(desc1, desc2).accept(this));
				}
			}
		}

		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSubDataPropertyOfAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSameIndividualAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.add(dataFactory.getOWLSubObjectPropertyOfAxiom(axiom.getFirstProperty(), axiom.getSecondProperty()
				.getInverseProperty()));
		axioms.add(dataFactory.getOWLSubObjectPropertyOfAxiom(axiom.getSecondProperty(), axiom.getFirstProperty()
				.getInverseProperty()));
		return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(SWRLRule axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLHasKeyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDatatypeDefinitionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAnnotationPropertyDomainAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAnnotationPropertyRangeAxiom axiom) {
		return archive(axiom);
	}
}
