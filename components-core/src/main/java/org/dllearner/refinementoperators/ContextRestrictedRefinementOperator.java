package org.dllearner.refinementoperators;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.math3.analysis.FunctionUtils.add;

/**
 * @author Lorenz Buehmann
 */
public class ContextRestrictedRefinementOperator {

	private final OWLDataFactory df;
	private final OWLObjectDuplicator objectDuplicator;
	private final OWLObjectGenerator classGen;
	private final AbstractReasonerComponent reasoner;

	public ContextRestrictedRefinementOperator(OWLDataFactory df, AbstractReasonerComponent reasoner) {
		this.df = df;
		this.objectDuplicator = new OWLObjectDuplicator(df);
		this.classGen = new OWLObjectGenerator(df);
		this.reasoner = reasoner;
	}


	public Set<OWLClassExpression> subExpressionsOf(OWLClassExpression ce) {
		Set<OWLClassExpression> result = new HashSet<>();

		// the concept itself
		result.add(ce);


		if(ce instanceof OWLQuantifiedObjectRestriction) {
			result.addAll(subExpressionsOf(((OWLQuantifiedObjectRestriction) ce).getFiller()));
		} else if(ce instanceof OWLNaryBooleanClassExpression) {
			for (OWLClassExpression sub : ((OWLNaryBooleanClassExpression) ce).getOperands()) {
				result.addAll(subExpressionsOf(sub));
			}
		}
		return result;
	}

	public Set<List<OWLObject>> roleSubExpressionContextsOf(OWLClassExpression ce) {
		Set<List<OWLObject>> allContexts = Sets.newHashSet();

		// the CE itself is a context
		allContexts.add(Lists.newArrayList(ce));

		roleSubExpressionContextsOf(ce, allContexts, Lists.newArrayList());
		return allContexts;
	}


	private void roleSubExpressionContextsOf(OWLClassExpression ce,
													Set<List<OWLObject>> allContexts,
													List<OWLObject> currentContext) {

		if(ce instanceof OWLQuantifiedRestriction) {
			List<OWLObject> context = Lists.newArrayList(currentContext);
			// create new filler
			List<OWLObject> splittedCE = split((OWLQuantifiedRestriction) ce);
			context.addAll(splittedCE);

			allContexts.add(context);

			// recursive call with the filler for object restrictions
			if(ce instanceof OWLQuantifiedObjectRestriction) {
				roleSubExpressionContextsOf((OWLClassExpression) splittedCE.get(1), allContexts, Lists.newArrayList(context));
			}
		} else if(ce instanceof OWLNaryBooleanClassExpression) {
			Set<OWLClassExpression> operands = ((OWLNaryBooleanClassExpression) ce).getOperands();
			for (OWLClassExpression op : operands) {
				if(op instanceof OWLQuantifiedRestriction) {
					List<OWLObject> splittedCE = split((OWLQuantifiedRestriction) op);

					Set<OWLClassExpression> newOperands = Sets.newHashSet(operands);
					newOperands.remove(op);
					newOperands.add((OWLClassExpression) splittedCE.get(0));

					OWLClassExpression newCE = ce instanceof OWLObjectIntersectionOf
							? df.getOWLObjectIntersectionOf(newOperands)
							: df.getOWLObjectUnionOf(newOperands);
					List<OWLObject> context = Lists.newArrayList(currentContext);
					context.add(newCE);
					context.add(splittedCE.get(1));

					allContexts.add(context);

					if(op instanceof OWLQuantifiedObjectRestriction) {
						ArrayList<OWLObject> newCurrentContext = Lists.newArrayList(currentContext);
						newCurrentContext.add(newCE);
						roleSubExpressionContextsOf((OWLClassExpression) splittedCE.get(1), allContexts, newCurrentContext);
					}

				}

			}

		}
	}

	private List<OWLObject> split(OWLQuantifiedRestriction ce) {
		OWLClassExpression newCE = null;
		OWLObject filler = ce.getFiller();
		int card = ce instanceof OWLCardinalityRestriction ? ((OWLCardinalityRestriction) ce).getCardinality() : 0;
		if(ce instanceof OWLQuantifiedObjectRestriction) {
			// create new filler
			OWLClassExpression newFiller = classGen.nextClass();

			OWLObjectPropertyExpression p = (OWLObjectPropertyExpression) ce.getProperty();

			switch(ce.getClassExpressionType()) {
				case OBJECT_SOME_VALUES_FROM: newCE = df.getOWLObjectSomeValuesFrom(p, newFiller);break;
				case OBJECT_ALL_VALUES_FROM: newCE = df.getOWLObjectAllValuesFrom(p, newFiller);break;
				case OBJECT_MIN_CARDINALITY: newCE = df.getOWLObjectMinCardinality(card, p, newFiller);break;
				case OBJECT_MAX_CARDINALITY: newCE = df.getOWLObjectMaxCardinality(card, p, newFiller);break;
				case OBJECT_EXACT_CARDINALITY: newCE = df.getOWLObjectExactCardinality(card, p, newFiller);break;
			}
		} else {
			// create new filler
			OWLDataRange newFiller = classGen.nextDataRange();

			OWLDataPropertyExpression p = (OWLDataPropertyExpression) ce.getProperty();

			switch(ce.getClassExpressionType()) {
				case DATA_SOME_VALUES_FROM: newCE = df.getOWLDataSomeValuesFrom(p, newFiller);break;
				case DATA_ALL_VALUES_FROM: newCE = df.getOWLDataAllValuesFrom(p, newFiller);break;
				case DATA_MIN_CARDINALITY: newCE = df.getOWLDataMinCardinality(card, p, newFiller);break;
				case DATA_MAX_CARDINALITY: newCE = df.getOWLDataMaxCardinality(card, p, newFiller);break;
				case DATA_EXACT_CARDINALITY: newCE = df.getOWLDataExactCardinality(card, p, newFiller);break;
			}
		}

		return Lists.newArrayList(newCE, filler);
	}

	/*
	 * Computes simple classes an individual can belong to, i.e.
	 *  sc = {A|K \models A(i)} union {\neg A|K \not \models A(i)}
	 * The idea behind is that those can be used in refinement steps
	 */
	private Set<OWLClassExpression> sc(OWLIndividual ind) {
		Set<OWLClass> types = reasoner.getTypes(ind);
		Set<OWLClassExpression> nonTypes = reasoner.getClasses().stream()
				.filter(c -> !types.contains(c))
				.map(df::getOWLObjectComplementOf)
				.collect(Collectors.toSet());
		return Sets.union(types, nonTypes);
	}

	private Set<OWLClassExpression> sc(Set<OWLIndividual> individuals) {
		return individuals.stream().flatMap(ind -> sc(ind).stream()).collect(Collectors.toSet());
	}

	/*
	 * pairs (D, n) in rf(S) simply reflect that exactly n individuals in S are instances of D
	 */
	private Set<Pair<OWLClassExpression, Integer>> rf(Set<OWLIndividual> individuals) {
		return sc(individuals).stream()
				.map(ce -> Pair.of(ce, Sets.intersection(individuals, reasoner.getIndividuals(ce)).size()))
				.filter(pair -> pair.getRight() >= 1)
				.collect(Collectors.toSet());
	}

	private Set<OWLIndividual> succ(OWLIndividual ind, OWLObjectProperty p) {
		return reasoner.getRelatedIndividuals(ind, p);
	}

	private Set<List<OWLClassExpression>> con(OWLIndividual ind, OWLObjectProperty p, Set<OWLIndividual> successors) {
		Set<List<OWLClassExpression>> contexts = new HashSet<>();
		for (OWLClassExpression type : sc(ind)) {
			for (Pair<OWLClassExpression, Integer> succType2Cnt : rf(successors)) {
				OWLClassExpression succType = succType2Cnt.getLeft();
				int cnt = succType2Cnt.getRight();

				//C, (D, n) ->  [C \sqcap \exists r.s0, D]
				contexts.add(Lists.newArrayList(
						df.getOWLObjectIntersectionOf(type,
													  df.getOWLObjectAllValuesFrom(p, classGen.nextClass())),
						succType
						)
				);

				//C, (D, n) ->  [C \sqcap \forall r.s0, D] if |S| = n
				if(successors.size() == cnt) {
					contexts.add(Lists.newArrayList(
							df.getOWLObjectIntersectionOf(type,
														  df.getOWLObjectSomeValuesFrom(p, classGen.nextClass())),
							succType
								 )
					);
				}


				//C, (D, n) ->  [C \sqcap \geq q r.s0, D] where 2 <= q <= |S| and n >= q
				for(int q = 2; q <= successors.size(); q++) {
					if(cnt >= q) {
						contexts.add(Lists.newArrayList(
								df.getOWLObjectIntersectionOf(type,
															  df.getOWLObjectMinCardinality(q, p, classGen.nextClass())),
								succType
									 )
						);
					}

				}

				//C, (D, n) ->  [C \sqcap \geq q r.s0, D] where 2 <= q <= |S| and n >= q
				// get max number of r-successors that any instance of C has
				int qMax = reasoner.getIndividuals(type).stream().mapToInt(i -> reasoner.getRelatedIndividuals(i, p).size()).max().getAsInt();
				for(int q = 1; q <= qMax; q++) {
					if(q >= successors.size()) {
						contexts.add(Lists.newArrayList(
								df.getOWLObjectIntersectionOf(type,
															  df.getOWLObjectMaxCardinality(q, p, classGen.nextClass())),
								succType
									 )
						);
					}

				}


			}

		}
		return contexts;
	}

	private void generateContextGraph(Set<OWLIndividual> individuals) {
		Set<List<OWLClassExpression>> vertices = new HashSet<>();
		Set<Pair<List<OWLClassExpression>, List<OWLClassExpression>>> edges = new HashSet<>();

		Queue<Triple<OWLIndividual, List<Pair<OWLIndividual, OWLIndividual>>, OWLIndividual>> queue = new ArrayDeque<>();
		for (OWLIndividual ind : individuals) {
			queue.add(Triple.of(ind, new ArrayList<>(), ind));
		}

		Set<Triple<List<OWLClassExpression>, OWLIndividual, OWLIndividual>> l = new HashSet<>();

		while(!queue.isEmpty()) {
			Triple<OWLIndividual, List<Pair<OWLIndividual, OWLIndividual>>, OWLIndividual> elt = queue.poll();
			OWLIndividual i = elt.getLeft();
			List<Pair<OWLIndividual, OWLIndividual>> ic = elt.getMiddle();
			OWLIndividual e = elt.getRight();


			Set<OWLClassExpression> i_lambda = sc(i);


			Set<OWLIndividual> successors = new HashSet<>();

			for (OWLObjectProperty p : reasoner.getObjectProperties()) {
				Set<OWLIndividual> p_successors = succ(i, p);
				successors.addAll(p_successors);

				for (List<OWLClassExpression> contexts : con(i, p, p_successors)) {

				}
			}

			for (OWLIndividual j : successors) {
				if(ic.stream().noneMatch(pair -> pair.getRight().equals(j) || pair.getLeft().equals(j))){
					List<Pair<OWLIndividual, OWLIndividual>> icNew = Lists.newArrayList(ic);
					icNew.add(Pair.of(i, j));
					queue.add(Triple.of(j, icNew, e));
				}
			}

		}
	}

	class Context extends ArrayList<OWLClassExpression> {

	}


	static class OWLObjectGenerator {
		private final OWLDataFactory df;
		int i = 0;
		private static final PrefixManager pm = new DefaultPrefixManager();
		static {pm.setDefaultPrefix("http://dl-learner.org/autogen/sub");}
		public OWLObjectGenerator(OWLDataFactory df) {
			this.df = df;
		}
		public OWLClassExpression nextClass() {
			return df.getOWLClass(String.valueOf(i++), pm);
		}
		public OWLDataRange nextDataRange() {
			return df.getOWLDatatype(String.valueOf(i++), pm);
		}

		public void reset() {
			i = 0;
		}
	}

	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());

		OWLDataFactory df = OWLManager.getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://dl-learner.org/test#");

		AbstractReasonerComponent reasoner = new OWLAPIReasoner();
		reasoner.init();

		OWLClassExpression ce = df.getOWLObjectIntersectionOf(
				df.getOWLClass("D", pm),
				df.getOWLObjectMinCardinality(2, df.getOWLObjectProperty("r", pm),
											  df.getOWLObjectUnionOf(
													  df.getOWLDataSomeValuesFrom(df.getOWLDataProperty("d", pm),
																				  df.getDoubleOWLDatatype()),
													  df.getOWLClass("B", pm)
											  )
				),
				df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty("s", pm),
											 df.getOWLObjectIntersectionOf(
											 		df.getOWLClass("A", pm), df.getOWLClass("C", pm)
											 )
				)



		);
		System.out.println(ce);

		ContextRestrictedRefinementOperator operator = new ContextRestrictedRefinementOperator(df, reasoner);

		Set<List<OWLObject>> roleSubExpressionContexts = operator.roleSubExpressionContextsOf(ce);

		for (List<OWLObject> ctx : roleSubExpressionContexts) {
			System.out.println(ctx);
		}


	}
}
