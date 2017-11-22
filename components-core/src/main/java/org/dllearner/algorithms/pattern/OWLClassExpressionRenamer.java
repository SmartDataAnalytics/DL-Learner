/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLFacet;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OWLClassExpressionRenamer implements OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLIndividualVisitor, OWLDataRangeVisitor, OWLDataVisitor {
	
	private static final String NS = "http://dl-learner.org/pattern/";

	/*
	Some constants here used for abstraction
	 */

	private static final int MIN_INTEGER_VALUE = 0;
	private static final int MAX_INTEGER_VALUE = 9;
	private static final double MIN_DOUBLE_VALUE = 0d;
	private static final double MAX_DOUBLE_VALUE = 9d;
	private static final float MIN_FLOAT_VALUE = 0f;
	private static final float MAX_FLOAT_VALUE = 9f;


	private OWLDataFactory df;
	private Map<OWLEntity, OWLEntity> renaming;
	private OWLObject renamedOWLObject;
//	private OWLClassExpressionOrderingComparator comparator = new OWLClassExpressionOrderingComparator();
	private OWLObjectComparator comparator = new OWLObjectComparator();
	private Queue<String> classVarQueue = new LinkedList<>();
	private Queue<String> propertyVarQueue = new LinkedList<>();
	private Queue<String> individualVarQueue = new LinkedList<>();
	
	private OWLLiteralRenamer literalRenamer;
	
	private boolean normalizeCardinalities = true;
	private boolean normalizeHasValue = true;
	private boolean normalizeOneOf = true;

	private AtomicInteger clsCounter = new AtomicInteger(1);

	private boolean multipleClasses = false;

	
	public OWLClassExpressionRenamer(OWLDataFactory df, Map<OWLEntity, OWLEntity> renaming) {
		this.df = df;
		this.renaming = renaming;
		
		literalRenamer = new OWLLiteralRenamer(df);
		
		reset();
	}

	public void reset() {
		classVarQueue = new LinkedList<>();
		propertyVarQueue = new LinkedList<>();
		individualVarQueue = new LinkedList<>();

		for(int i = 65; i <= 90; i++){
			classVarQueue.add(String.valueOf((char)i));
		}
		for(int j = 2; j <=5; j++){
			for(int i = 65; i <= 90; i++){
				classVarQueue.add(String.valueOf((char)i) + "_" + j);
			}
		}
		for(int i = 97; i <= 111; i++){
			individualVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 112; i <= 122; i++){
			propertyVarQueue.add(String.valueOf((char)i));
		}
	}

	public void setMultipleClasses(boolean multipleClasses) {
		this.multipleClasses = multipleClasses;
	}

	public OWLClassExpression rename(OWLClassExpression expr){
		renamedOWLObject = null;
		expr.accept(this);
		return (OWLClassExpression) renamedOWLObject;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends OWLPropertyExpression> T rename(T expr){
		renamedOWLObject = null;
		expr.accept(this);
		return (T) renamedOWLObject;
	}
	
	public OWLIndividual rename(OWLIndividual ind){
		renamedOWLObject = null;
		ind.accept(this);
		return (OWLIndividual) renamedOWLObject;
	}
	
	public OWLDataRange rename(OWLDataRange range){
		renamedOWLObject = null;
		range.accept(this);
		return (OWLDataRange) renamedOWLObject;
	}
	
	public OWLLiteral rename(OWLLiteral lit){
		renamedOWLObject = null;
		renamedOWLObject = literalRenamer.rename(lit);
		return (OWLLiteral) renamedOWLObject;
	}
	
	@Override
	public void visit(OWLObjectIntersectionOf desc) {
		List<OWLClassExpression> operands = desc.getOperandsAsList();
//		Collections.sort(operands, comparator);
		Set<OWLClassExpression> renamedOperands = new HashSet<>();//new TreeSet<>(comparator);
		for(OWLClassExpression expr : operands){
			renamedOperands.add(rename(expr));
		}
		renamedOWLObject = df.getOWLObjectIntersectionOf(renamedOperands);
	}
	
	@Override
	public void visit(OWLObjectUnionOf desc) {
		List<OWLClassExpression> operands = desc.getOperandsAsList();
		Collections.sort(operands, comparator);
		SortedSet<OWLClassExpression> renamedOperands = new TreeSet<>(comparator);
		for(OWLClassExpression expr : operands){
			renamedOperands.add(rename(expr));
		}
		renamedOWLObject = df.getOWLObjectUnionOf(renamedOperands);
	}
	
	@Override
	public void visit(OWLDataHasValue desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLLiteral value = desc.getFiller();
		value = rename(value);
		renamedOWLObject = df.getOWLDataHasValue(property, value);
	}

	@Override
	public void visit(OWLObjectComplementOf desc) {
		OWLClassExpression operand = desc.getOperand();
		operand = rename(operand);
		renamedOWLObject = df.getOWLObjectComplementOf(operand);
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLClassExpression filler = desc.getFiller();
		filler = rename(filler);
		renamedOWLObject = df.getOWLObjectSomeValuesFrom(property, filler);
	}

	@Override
	public void visit(OWLObjectAllValuesFrom desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLClassExpression filler = desc.getFiller();
		filler = rename(filler);
		renamedOWLObject = df.getOWLObjectAllValuesFrom(property, filler);
	}

	@Override
	public void visit(OWLObjectHasValue desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLIndividual value = desc.getFiller();
		value = rename(value);
		renamedOWLObject = df.getOWLObjectHasValue(property, value);
	}

	@Override
	public void visit(OWLObjectMinCardinality desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLClassExpression filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLObjectMinCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLObjectExactCardinality desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLClassExpression filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLObjectExactCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLObjectMaxCardinality desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLClassExpression filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLObjectMaxCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLObjectHasSelf desc) {
		OWLObjectPropertyExpression property = desc.getProperty();
		property = rename(property);
		renamedOWLObject = df.getOWLObjectHasSelf(property);
	}

	@Override
	public void visit(OWLObjectOneOf desc) {
		Set<OWLIndividual> individuals = desc.getIndividuals();
		Set<OWLIndividual> renamedIndividuals = new TreeSet<>();
		for (OWLIndividual ind : individuals) {
			renamedIndividuals.add(rename(ind));
		}
		renamedOWLObject = df.getOWLObjectOneOf(renamedIndividuals);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLDataRange filler = desc.getFiller();
		filler = rename(filler);
		renamedOWLObject = df.getOWLDataSomeValuesFrom(property, filler);
	}

	@Override
	public void visit(OWLDataAllValuesFrom desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLDataRange filler = desc.getFiller();
		filler = rename(filler);
		renamedOWLObject = df.getOWLDataAllValuesFrom(property, filler);
	}

	@Override
	public void visit(OWLDataMinCardinality desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLDataRange filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLDataMinCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLDataExactCardinality desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLDataRange filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLDataExactCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLDataMaxCardinality desc) {
		OWLDataPropertyExpression property = desc.getProperty();
		property = rename(property);
		OWLDataRange filler = desc.getFiller();
		filler = rename(filler);
		int cardinality = desc.getCardinality();
		if(normalizeCardinalities){
			cardinality = 1;
		}
		renamedOWLObject = df.getOWLDataMaxCardinality(cardinality, property, filler);
	}

	@Override
	public void visit(OWLObjectInverseOf desc) {
		OWLObjectPropertyExpression inverse = desc.getInverse();
		inverse = rename(inverse);
		renamedOWLObject = df.getOWLObjectInverseOf(inverse);
	}

	Function<OWLEntity, OWLEntity> classRenamingFn = k -> df.getOWLClass(getIRI("A_" + clsCounter.getAndIncrement()));

	public void setClassRenamingFn(Function<OWLEntity, OWLEntity> classRenamingFn) {
		this.classRenamingFn = classRenamingFn;
	}

	@Override
	public void visit(OWLClass desc) {
		if(desc.isTopEntity()){
			renamedOWLObject = desc;
		} else {
//			OWLEntity newEntity = renaming.computeIfAbsent(desc, k -> df.getOWLClass(getIRI(classVarQueue.poll())));
			OWLEntity newEntity = renaming.computeIfAbsent(desc, classRenamingFn);
			renamedOWLObject = newEntity;

		}
	}
	
	@Override
	public void visit(OWLObjectProperty op) {
		if(op.isTopEntity()){
			renamedOWLObject = op;
		} else {
			OWLEntity newEntity = renaming.computeIfAbsent(op, k -> df.getOWLObjectProperty(
					getIRI(propertyVarQueue.poll())));
			renamedOWLObject = newEntity;
		}
	}

	@Override
	public void visit(OWLDataProperty dp) {
		if(dp.isTopEntity()){
			renamedOWLObject = dp;
		} else {
			OWLEntity newEntity = renaming.computeIfAbsent(dp,
														   k -> df.getOWLDataProperty(getIRI(propertyVarQueue.poll())));
			renamedOWLObject = newEntity;
		}
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty property) {

	}

	@Override
	public void visit(OWLNamedIndividual ind) {
		OWLEntity newEntity = renaming.computeIfAbsent(ind, k -> df.getOWLNamedIndividual(
				getIRI(individualVarQueue.poll())));
		renamedOWLObject = newEntity;
	}
	
	private IRI getIRI(String var){
		return IRI.create(NS + var);
	}

	@Override
	public void visit(OWLAnonymousIndividual ind) {
		OWLEntity newEntity = renaming.get(ind);
		if(newEntity == null){
			newEntity = df.getOWLNamedIndividual(getIRI(individualVarQueue.poll()));
//			renaming.put(ind, newEntity);
		}
		renamedOWLObject = newEntity;
	}

	@Override
	public void visit(OWLDatatype dt) {
		if(dt.isBuiltIn()) {
			renamedOWLObject = dt;
		} else {
			renamedOWLObject = PatternConstants.USER_DEFINED_DATATYPE;
		}
	}

	@Override
	public void visit(OWLDataOneOf desc) {
		Set<OWLLiteral> literals = desc.getValues();
		Set<OWLLiteral> renamedLiterals = new TreeSet<>();
		for (OWLLiteral lit : literals) {
			renamedLiterals.add(rename(lit));
		}
		renamedOWLObject = df.getOWLDataOneOf(renamedLiterals);
	}

	@Override
	public void visit(OWLDataComplementOf desc) {
		OWLDataRange dataRange = desc.getDataRange();
		dataRange = rename(dataRange);
		renamedOWLObject = df.getOWLDataComplementOf(dataRange);
	}

	@Override
	public void visit(OWLDataIntersectionOf desc) {
		List<OWLDataRange> operands = new ArrayList<>(desc.getOperands());
		Collections.sort(operands, comparator);
		SortedSet<OWLDataRange> renamedOperands = new TreeSet<>(comparator);
		for(OWLDataRange expr : operands){
			renamedOperands.add(rename(expr));
		}
		renamedOWLObject = df.getOWLDataIntersectionOf(renamedOperands);
	}

	@Override
	public void visit(OWLDataUnionOf desc) {
		List<OWLDataRange> operands = new ArrayList<>(desc.getOperands());
		Collections.sort(operands, comparator);
		SortedSet<OWLDataRange> renamedOperands = new TreeSet<>(comparator);
		for(OWLDataRange expr : operands){
			renamedOperands.add(rename(expr));
		}
		renamedOWLObject = df.getOWLDataUnionOf(renamedOperands);
	}

	@Override
	public void visit(OWLDatatypeRestriction desc) {
		OWLDatatype datatype = desc.getDatatype();
		Set<OWLFacetRestriction> facetRestrictions = desc.getFacetRestrictions();
		Set<OWLFacetRestriction> newFacets = facetRestrictions.stream()
				.map(facetRestriction -> normalize(facetRestriction))
				.sorted()
				.collect(Collectors.toSet());
		renamedOWLObject = df.getOWLDatatypeRestriction(datatype, newFacets);;
	}

	private OWLFacetRestriction normalize(OWLFacetRestriction fr) {
		OWLFacet facet = fr.getFacet();

		OWLLiteral value = fr.getFacetValue();

		return df.getOWLFacetRestriction(facet, normalize(value, facet));
	}

	private OWLLiteral normalize(OWLLiteral lit, OWLFacet facet) {
		OWLDatatype datatype = lit.getDatatype();
		OWLLiteral newLit = lit;
		if(datatype.isBuiltIn()) {
			switch(facet) {
				case MIN_INCLUSIVE:
				case MIN_EXCLUSIVE:
					if(datatype.isDouble()) {
						newLit = df.getOWLLiteral(MIN_DOUBLE_VALUE);
					} else if(datatype.isInteger()) {
						newLit = df.getOWLLiteral(MIN_INTEGER_VALUE);
					} else if(datatype.isFloat()) {
						newLit = df.getOWLLiteral(MIN_FLOAT_VALUE);
					}
					break;
				case MAX_INCLUSIVE:
				case MAX_EXCLUSIVE:
					if(datatype.isDouble()) {
						newLit = df.getOWLLiteral(MAX_DOUBLE_VALUE);
					} else if(datatype.isInteger()) {
						newLit = df.getOWLLiteral(MAX_INTEGER_VALUE);
					} else if(datatype.isFloat()) {
						newLit = df.getOWLLiteral(MAX_FLOAT_VALUE);
					}
					break;
					default:
			}

		}
		return newLit;
	}

	@Override
	public void visit(@Nonnull OWLLiteral node) {

	}

	@Override
	public void visit(@Nonnull OWLFacetRestriction node) {

	}
}
