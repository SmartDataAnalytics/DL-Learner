package org.dllearner.algorithms.versionspace.operator;

import com.google.common.collect.Lists;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Lorenz Buehmann
 */
public class ComplexityBoundedOperatorALC extends ComplexityBoundedOperator implements OWLDataRangeVisitorEx<Set<OWLDataRange>>{

	private TIntObjectHashMap<Set<OWLClassExpression>> topRefinements = new TIntObjectHashMap<>();

	public ComplexityBoundedOperatorALC(AbstractReasonerComponent reasoner, ComplexityModel complexityModel) {
		super(reasoner, complexityModel);
	}

	@Override
	public void init() throws ComponentInitException {
		// compute refinements of top
		computeTopRefinements();
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression ce) {
		Set<OWLClassExpression> refinements = super.refine(ce);

//		System.err.println(ce + ":" + refinements);
//
//		if(refinements.size() == 1 && refinements.iterator().next().equals(ce)) {
//			System.out.println(":::" + ce);
//			System.exit(0);
//		}
		
		// if a refinement is not Bottom, Top, ALL r.Bottom a refinement of top can be appended
		for(int i = 1; i <= 3; i++) {
			Set<OWLClassExpression> topRefs = topRefinements.get(i);
			for (OWLClassExpression topRef : topRefs) {
				if(refinements.isEmpty() && topRef.equals(ce)) {// we can avoid the generation of A AND A if \rho(A) was empty
					continue;
				}
				List<OWLClassExpression> operands = new ArrayList<>();
				if(ce instanceof OWLObjectIntersectionOf) { // flatten to avoid (A AND (A AND A)) instead of (A AND A AND A)
					operands.addAll(((OWLObjectIntersectionOf) ce).getOperands());
				} else {
					operands.add(ce);
				}
				operands.add(topRef);
				Collections.sort(operands);
				OWLObjectIntersectionOf mc = new OWLObjectIntersectionOfImplExt(operands);
				refinements.add(mc);
			}
		}

		// filter by complexity here
		Iterator<OWLClassExpression> iterator = refinements.iterator();
		while (iterator.hasNext()) {
			OWLClassExpression ref =  iterator.next();
			if(!complexityModel.isValid(ref)) {
				iterator.remove();
			}
		}
		return refinements;
	}

	private void computeTopRefinements() {
		// 1. Subclasses of owl:Thing
		Set<OWLClassExpression> refinements1 = reasoner.getSubClasses(df.getOWLThing());
		topRefinements.put(1, refinements1);

		// 2. NOT A
		Set<OWLClassExpression> refinements2 = new HashSet<>(refinements1.size());
		for (OWLClassExpression ce : refinements1) {
			refinements2.add(df.getOWLObjectComplementOf(ce));
		}
		topRefinements.put(2, refinements2);

		// 3. EXISTS r.TOP
		Set<OWLClassExpression> refinements3 = new HashSet<>();
		for(OWLObjectProperty p : reasoner.getMostGeneralProperties()) {
			refinements3.add(df.getOWLObjectSomeValuesFrom(p, df.getOWLThing()));
		}

		for(OWLDataProperty p : reasoner.getMostGeneralDatatypeProperties()) {
			refinements3.add(df.getOWLDataSomeValuesFrom(p, df.getTopDatatype()));
		}
		topRefinements.put(3, refinements3);
	}



	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLClass ce) {
		return reasoner.getSubClasses(ce);
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> refinements = new HashSet<>();

		List<OWLClassExpression> operands = ce.getOperandsAsList();

		// refine each operand
		for(OWLClassExpression child : operands) {
			Set<OWLClassExpression> tmp = refine(child);

			// create new intersection
			for(OWLClassExpression c : tmp) {
				List<OWLClassExpression> newOperands = new ArrayList<>(operands);
				if(c instanceof OWLObjectIntersectionOf) { // flatten to avoid (A AND (A AND A)) instead of (A AND A AND A)
					newOperands.addAll(((OWLObjectIntersectionOf) c).getOperands());
				} else {
					newOperands.add(c);
				}
				newOperands.remove(child);
				Collections.sort(newOperands);
				OWLClassExpression newIntersection = new OWLObjectIntersectionOfImplExt(newOperands);
				refinements.add(newIntersection);
			}
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectUnionOf ce) {
		Set<OWLClassExpression> refinements = new HashSet<>();

		List<OWLClassExpression> operands = ce.getOperandsAsList();

		// refine each operand
		for(OWLClassExpression child : operands) {
			Set<OWLClassExpression> tmp = refine(child);

			// create new union
			for(OWLClassExpression c : tmp) {
				List<OWLClassExpression> newOperands = new ArrayList<>(operands);
				newOperands.add(c);
				newOperands.remove(child);
				Collections.sort(newOperands);
				OWLClassExpression newUnion = new OWLObjectUnionOfImplExt(newOperands);
				refinements.add(newUnion);
			}
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectComplementOf ce) {
		OWLClassExpression operand = ce.getOperand();
		if(!operand.isAnonymous()){
			// get super classes
			SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(operand);
			superClasses.remove(df.getOWLThing());

			// create refinements
			Set<OWLClassExpression> refinements = new HashSet<>(superClasses.size());
			for(OWLClassExpression sup : superClasses) {
				refinements.add(df.getOWLObjectComplementOf(sup));
			}

			return refinements;
		}
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectSomeValuesFrom ce) {
		OWLObjectProperty property = ce.getProperty().getNamedProperty();
		OWLClassExpression filler = ce.getFiller();

		Set<OWLClassExpression> refinements = new HashSet<>();

		// rule 1: EXISTS r.D => EXISTS r.E with E \sqsubset D
		Set<OWLClassExpression> tmp = refine(filler);
		for(OWLClassExpression c : tmp){
			refinements.add(df.getOWLObjectSomeValuesFrom(property, c));
		}

		// rule 2: EXISTS r.D => EXISTS s.D
		Set<OWLObjectProperty> subProperties = reasoner.getSubProperties(property);
		for (OWLObjectProperty sub : subProperties) {
			refinements.add(df.getOWLObjectSomeValuesFrom(sub, filler));
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectAllValuesFrom ce) {
		OWLObjectProperty property = ce.getProperty().getNamedProperty();
		OWLClassExpression filler = ce.getFiller();

		Set<OWLClassExpression> refinements = new HashSet<>();

		// rule 1: ALL r.D => ALL r.E with E \sqsubset D
		Set<OWLClassExpression> tmp = refine(filler);
		for(OWLClassExpression c : tmp){
			refinements.add(df.getOWLObjectAllValuesFrom(property, c));
		}

		// rule 2: ALL r.D => ALL s.D
		Set<OWLObjectProperty> subProperties = reasoner.getSubProperties(property);
		for (OWLObjectProperty sub : subProperties) {
			refinements.add(df.getOWLObjectAllValuesFrom(sub, filler));
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataSomeValuesFrom ce) {
		OWLDataProperty property = ce.getProperty().asOWLDataProperty();
		OWLDataRange filler = ce.getFiller();

		Set<OWLClassExpression> refinements = new HashSet<>();

		// rule 1: EXISTS r.D => EXISTS r.E
		Set<OWLDataRange> tmp = filler.accept(this);
		for(OWLDataRange dr : tmp){
			refinements.add(df.getOWLDataSomeValuesFrom(property, dr));
		}

		// rule 2: EXISTS r.D => EXISTS s.D
		Set<OWLDataProperty> subProperties = reasoner.getSubProperties(property);
		for (OWLDataProperty sub : subProperties) {
			refinements.add(df.getOWLDataSomeValuesFrom(sub, filler));
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataAllValuesFrom ce) {
		OWLDataProperty property = ce.getProperty().asOWLDataProperty();
		OWLDataRange filler = ce.getFiller();

		Set<OWLClassExpression> refinements = new HashSet<>();

		// rule 1: ALL r.D => ALL r.E
		Set<OWLDataRange> tmp = filler.accept(this);
		for(OWLDataRange dr : tmp){
			refinements.add(df.getOWLDataAllValuesFrom(property, dr));
		}

		// rule 2: ALL r.D => ALL s.D
		Set<OWLDataProperty> subProperties = reasoner.getSubProperties(property);
		for (OWLDataProperty sub : subProperties) {
			refinements.add(df.getOWLDataAllValuesFrom(sub, filler));
		}

		return refinements;
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectHasValue ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectMinCardinality ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectExactCardinality ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectMaxCardinality ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectHasSelf ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectOneOf ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataHasValue ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataMinCardinality ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataExactCardinality ce) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataMaxCardinality ce) {
		return Collections.emptySet();
	}

	/*
	Data range part
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDatatype node) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataOneOf node) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataComplementOf node) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataIntersectionOf node) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataUnionOf node) {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDatatypeRestriction node) {
		return Collections.emptySet();
	}
}
