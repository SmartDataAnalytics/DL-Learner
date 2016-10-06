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
package org.dllearner.utilities;

import fuzzyowl2.*;
import fuzzyowl2.parser.Parser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLFacet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * General class translating from OWL 2 into some fuzzy OWLClassExpression Logic language.
 * Subclasses of Owl2ToFuzzyDescriptionLogic translate it into specific
 * languages, such as the language of fuzzyDL, the language of DeLorean...
 * The user must override most of the following methods:
 *
 * <ul>
    <li>protected String getAtomicConceptName(OWLClass c)</li>
    <li>protected String getAtomicDataPropertyName(OWLDataProperty p)</li>
    <li>protected String getAtomicObjectPropertyName(OWLObjectProperty p)</li>
    <li>protected String getBottomConceptName()</li>
    <li>protected String getBottomDataPropertyName()</li>
    <li>protected String getBottomObjectPropertyName()</li>
    <li>protected String getDataAllValuesFromName(OWLDataPropertyExpression p, OWLDataRange range)</li>
    <li>protected String getDataExactCardinalityRestrictionName(int card, OWLDataPropertyExpression p)</li>
    <li>protected String getDataExactCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)</li>
    <li>protected String getDataHasValueName(OWLDataPropertyExpression p, OWLLiteral lit)</li>
    <li>protected String getDataMaxCardinalityRestrictionName(int card, OWLDataPropertyExpression p)</li>
    <li>protected String getDataMaxCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)</li>
    <li>protected String getDataMinCardinalityRestrictionName(int card, OWLDataPropertyExpression p)</li>
    <li>protected String getDataMinCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)</li>
    <li>protected String getDataSomeValuesFromName(OWLDataPropertyExpression p, OWLDataRange range)</li>
    <li>protected String getIndividualName(OWLIndividual i)</li>
    <li>protected String getObjectAllValuesFromName(OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected String getObjectComplementOfName(OWLClassExpression c)</li>
    <li>protected String getObjectExactCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)</li>
    <li>protected String getObjectExactCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected String getObjectHasSelfName(OWLObjectPropertyExpression p)</li>
    <li>protected String getObjectHasValueName(OWLObjectPropertyExpression p, OWLIndividual i)</li>
    <li>protected String getObjectIntersectionOfName(Set<OWLClassExpression> operands)</li>
    <li>protected String getObjectMaxCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)</li>
    <li>protected String getObjectMaxCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected String getObjectMinCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)</li>
    <li>protected String getObjectMinCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected String getObjectOneOfName(Set<OWLIndividual> set)</li>
    <li>protected String getObjectSomeValuesFromName(OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected String getObjectUnionOfName(Set<OWLClassExpression> operands)</li>
    <li>protected String getShortName(OWLEntity e)</li>
    <li>protected String getTopConceptName()</li>
    <li>protected String getTopDataPropertyName()</li>
    <li>protected String getTopObjectPropertyName()</li>
    <li>protected void writeAsymmetricObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeChoquetConceptDefinition(String name, ChoquetConcept c)</li>
    <li>protected void writeConceptAssertionAxiom(OWLIndividual i, OWLClassExpression c, double d)</li>
    <li>protected void writeConceptDeclaration(OWLClassExpression c)</li>
    <li>protected void writeDataPropertyAssertionAxiom(OWLIndividual i1, OWLLiteral i2, OWLDataPropertyExpression p, double d)</li>
    <li>protected void writeDataPropertyDeclaration(OWLDataPropertyExpression dp)</li>
    <li>protected void writeDataPropertyDomainAxiom(OWLDataPropertyExpression p, OWLClassExpression c)</li>
    <li>protected void writeDataPropertyRangeAxiom(OWLDataPropertyExpression p, OWLDataRange c)</li>
    <li>protected void writeDifferentIndividualsAxiom(Set<OWLIndividual> set)</li>
    <li>protected void writeDisjointClassesAxiom(Set<OWLClassExpression> set)</li>
    <li>protected void writeDisjointDataPropertiesAxiom(Set<OWLDataPropertyExpression> set)</li>
    <li>protected void writeDisjointObjectPropertiesAxiom(Set<OWLObjectPropertyExpression> set)</li>
    <li>protected void writeDisjointUnionAxiom(Set<OWLClassExpression> set)</li>
    <li>protected void writeEquivalentClassesAxiom(Set<OWLClassExpression> set)</li>
    <li>protected void writeEquivalentDataPropertiesAxiom(Set<OWLDataPropertyExpression> set)</li>
    <li>protected void writeEquivalentObjectPropertiesAxiom(Set<OWLObjectPropertyExpression> set)</li>
    <li>protected void writeFunctionalDataPropertyAxiom(OWLDataPropertyExpression p)</li>
    <li>protected void writeFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeFuzzyLogic(FuzzyLogic logic)</li>
    <li>protected void writeFuzzyNominalConceptDefinition(String name, FuzzyNominalConcept c)</li>
    <li>protected void writeInverseFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeInverseObjectPropertiesAxiom(OWLObjectPropertyExpression p1, OWLObjectPropertyExpression p2)</li>
    <li>protected void writeIrreflexiveObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeLeftShoulderFunctionDefinition(String name, LeftShoulderFunction dat)</li>
    <li>protected void writeLinearFunctionDefinition(String name, LinearFunction dat)</li>
    <li>protected void writeLinearModifierDefinition(String name, LinearModifier mod)</li>
    <li>protected void writeModifiedConceptDefinition(String name, ModifiedConcept c)</li>
    <li>protected void writeModifiedFunctionDefinition(String name, ModifiedFunction dat)</li>
    <li>protected void writeModifiedPropertyDefinition(String name, ModifiedProperty c)</li>
    <li>protected void writeNegativeDataPropertyAssertionAxiom(OWLIndividual i1, OWLLiteral i2, OWLDataPropertyExpression p, double d)</li>
    <li>protected void writeNegativeObjectPropertyAssertionAxiom(OWLIndividual i1, OWLIndividual i2, OWLObjectPropertyExpression p, double d)</li>
    <li>protected void writeObjectPropertyAssertionAxiom(OWLIndividual i1, OWLIndividual i2, OWLObjectPropertyExpression p, double d)</li>
    <li>protected void writeObjectPropertyDeclaration(OWLObjectPropertyExpression op)</li>
    <li>protected void writeObjectPropertyDomainAxiom(OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected void writeObjectPropertyRangeAxiom(OWLObjectPropertyExpression p, OWLClassExpression c)</li>
    <li>protected void writeOwaConceptDefinition(String name, OwaConcept c)</li>
    <li>protected void writeQowaConceptDefinition(String name, QowaConcept c)</li>
    <li>protected void writeQuasiSugenoConceptDefinition(String name, QuasiSugenoConcept c)</li>
    <li>protected void writeReflexiveObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeRightShoulderFunctionDefinition(String name, RightShoulderFunction dat)</li>
    <li>protected void writeSameIndividualAxiom(Set<OWLIndividual> set)</li>
    <li>protected void writeSubclassOfAxiom(OWLClassExpression subclass, OWLClassExpression superclass, double d)</li>
    <li>protected void writeSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty, OWLDataPropertyExpression superProperty, double d)</li>
    <li>protected void writeSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty, OWLObjectPropertyExpression superProperty, double d)</li>
    <li>protected void writeSubPropertyChainOfAxiom(List<OWLObjectPropertyExpression> chain, OWLObjectPropertyExpression superProperty, double d)</li>
    <li>protected void writeSugenoConceptDefinition(String name, SugenoConcept c)</li>
    <li>protected void writeSymmetricObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeTransitiveObjectPropertyAxiom(OWLObjectPropertyExpression p)</li>
    <li>protected void writeTrapezoidalFunctionDefinition(String name, TrapezoidalFunction dat)</li>
    <li>protected void writeTriangularFunctionDefinition(String name, TriangularFunction dat)</li>
    <li>protected void writeTriangularModifierDefinition(String name, TriangularModifier mod)</li>
    <li>protected void writeWeightedConceptDefinition(String name, WeightedConcept c)</li>
    <li>protected void writeWeightedMaxConceptDefinition(String name, WeightedMaxConcept c)</li>
    <li>protected void writeWeightedMinConceptDefinition(String name, WeightedMinConcept c)</li>
    <li>protected void writeWeightedSumConceptDefinition(String name, WeightedSumConcept c)</li>
 * </ul>
 *
 * @author Fernando Bobillo
 */
public class FuzzyOwl2
{

	protected OWLDataFactory dataFactory;
	protected Hashtable<String, FuzzyConcept> definedConcepts;
	protected Hashtable<String, FuzzyProperty> definedProperties;
	public Hashtable<String, FuzzyDatatype> fuzzyDatatypes;
	protected Hashtable<String, FuzzyModifier> fuzzyModifiers;
	protected OWLAnnotationProperty label;
	protected OWLOntologyManager manager;
	protected String ontologyPath;
	protected OWLOntology ontology;
	protected Set<OWLOntology> ontologies;
	protected SimpleShortFormProvider pm;

	protected final double NEG_INFINITY = -10000;
	protected final double POS_INFINITY = 10000;

	/**
	 * Output (file, standard output...)
	 */
	protected static PrintStream out = System.out;

	/**
	 * Constructor.
	 * @param input Path of the input ontology.
	 * @param output Path of the output file; null for the standard output.
	 */
	public FuzzyOwl2(String input, String output)
	{
		definedConcepts = new Hashtable<>();
		definedProperties = new Hashtable<>();
		fuzzyDatatypes = new Hashtable<>();
		fuzzyModifiers = new Hashtable<>();
		manager = OWLManager.createOWLOntologyManager();
		ontologyPath = input;

		loadOntology(ontologyPath);
		ontologies = new HashSet<>();
		ontologies.add(ontology);

		// Imported ontologies
		Set<OWLOntology> imports = manager.getImportsClosure(ontology);
		if (imports != null)
			ontologies.addAll(imports);
		
		// If an output file is specified, try to open it.
		// If not, or if there are problems opening it, we use the standard output.
		if (output != null)
		{
			try {
				out = new PrintStream(new FileOutputStream(output));
			}
			catch (Exception ex)
			{
				printError("Could not load ontology: " + ex.getMessage());
			}
		}

		dataFactory = manager.getOWLDataFactory();
		if (ontology.getOntologyID().getOntologyIRI() == null)
			ontologyPath = "";
		else
			ontologyPath = ontology.getOntologyID().getOntologyIRI().toString();
		pm = new SimpleShortFormProvider();
		
		label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyPath + "#" + "fuzzyLabel"));
	}

	public void setPrintStream(PrintStream ps){
		out = ps;
	}
	
	protected void loadOntology(String ontologyPath)
	{
		try
		{
			// Try ontologyPath as the path of a local file
			File f = new File(ontologyPath);
			IRI iri = IRI.create(f);
			ontology = manager.loadOntologyFromOntologyDocument(iri);
		}
		catch (Exception e)
		{
			// Try ontologyPath as an IRI
			IRI iri = IRI.create(ontologyPath);
			try {
				ontology = manager.loadOntologyFromOntologyDocument(iri);
			}
			catch (OWLOntologyCreationException ex)
			{
				printError("Could not load ontology: " + ex.getMessage());
			}
		}
	}

	/**
	 * Prints an error message in the standard output and finishes the execution.
	 * @param s An error message.
	 */
	protected static void exit(String s)
	{
		System.out.println(s);
		System.exit(0);
	}

	/**
	 * Prints a string in the desired PrintStream, unless it contains a null value.
	 */
	protected static void print(String s)
	{
		if (s != null && !s.contains(" null"))
			out.println(s);
	}

	/**
	 * Prints an error string in the standard error.
	 * The parameter could be used in the future to write in the desired PrintStream.
	 */
	protected static void printError(String s)
	{
		System.err.println(s);
	}

	/**
	 * @param args Two arguments: the input OWL 2 ontology, and the output fuzzy ontology in fuzzyDL syntax.
	 */
	public static void main(String[] args)
	{
		String[] returnValue = processParameters(args);
		FuzzyOwl2 f = new FuzzyOwl2(returnValue[0], returnValue[1]);
		f.translateOwl2Ontology();
	}

	/**
	 * Translates an OWL 2 ontology into a fuzzy one, processing the OWL 2 annotations.
	 */
	public void translateOwl2Ontology()
	{
		processOntologyAnnotations();
		processDatatypeAnnotations();
		processConceptAnnotations();
		processPropertyAnnotations();
		processOntologyAxioms();
	}

	/**
	 * Write annotations on the ontology.
	 */
	protected void processOntologyAnnotations()
	{
		for(OWLOntology o : ontologies)
		{
			Set<OWLAnnotation> annotations = o.getAnnotations();
	
			for (OWLAnnotation ax : annotations)
			{
				if (ax.getProperty().compareTo(label) != 0)
					continue;
	
				OWLAnnotationValue value = ax.getValue();
				FuzzyLogic logic = Parser.getLogic(value.toString());
				writeFuzzyLogic(logic);
			}
		}
	}

	// We annotate left, right, triangular, and trapezoidal functions.
	private void writeType1Datatypes(Object o, String name)
	{
		if (o instanceof LeftShoulderFunction)
		{
			LeftShoulderFunction dat = (LeftShoulderFunction) o;
			double k[] = new double[2];
			getK1AndK2(name, k);
			setK1AndK2(dat, k);
			fuzzyDatatypes.put(name, dat);
			writeLeftShoulderFunctionDefinition(name, dat);
		}
		else if (o instanceof RightShoulderFunction)
		{
			RightShoulderFunction dat = (RightShoulderFunction) o;
			double k[] = new double[2];
			getK1AndK2(name, k);
			setK1AndK2(dat, k);
			fuzzyDatatypes.put(name, dat);
			writeRightShoulderFunctionDefinition(name, dat);
		}
		else if (o instanceof TriangularFunction)
		{
			TriangularFunction dat = (TriangularFunction) o;
			double k[] = new double[2];
			getK1AndK2(name, k);
			setK1AndK2(dat, k);
			fuzzyDatatypes.put(name, dat);
			writeTriangularFunctionDefinition(name, dat);
		}
		else if (o instanceof TrapezoidalFunction)
		{
			TrapezoidalFunction dat = (TrapezoidalFunction) o;
			double k[] = new double[2];
			getK1AndK2(name, k);
			setK1AndK2(dat, k);
			fuzzyDatatypes.put(name, dat);
			writeTrapezoidalFunctionDefinition(name, dat);
		}
		else if (o instanceof LinearFunction)
		{
			LinearFunction dat = (LinearFunction) o;
			double k[] = new double[2];
			getK1AndK2(name, k);
			setK1AndK2(dat, k);
			fuzzyDatatypes.put(name, dat);
			writeLinearFunctionDefinition(name, dat);
		}
	}

	// We annotate linear and triangular modifiers.
	private void writeType2Datatypes(Object o, String name)
	{
		if (o instanceof TriangularModifier)
		{
			TriangularModifier mod = (TriangularModifier) o;
			fuzzyModifiers.put(name, mod);
			writeTriangularModifierDefinition(name, mod);
		}
		else if (o instanceof LinearModifier)
		{
			LinearModifier mod = (LinearModifier) o;
			fuzzyModifiers.put(name, mod);
			writeLinearModifierDefinition(name, mod);
		}
	}

	// We annotate modified functions.
	private void writeType3Datatypes(Object o, String name)
	{
		if (o instanceof ModifiedFunction)
		{
			ModifiedFunction dat = (ModifiedFunction) o;
			fuzzyDatatypes.put(name, dat);
			writeModifiedFunctionDefinition(name, dat);
		}
	}

	/**
	 * Write fuzzy datatypes and modifiers definitions, defined with OWL 2 concept annotations.
	 */
	protected void processDatatypeAnnotations()
	{
		/*
		 * Step 1. We annotate left, right, triangular, trapezoidal, and linear functions.
		 * Step 2. We annotate linear and triangular modifiers.
		 * Step 3. We annotate modified functions.
		 */
		for(OWLOntology o : ontologies)
		{
			for (OWLDeclarationAxiom ax : o.getAxioms(AxiomType.DECLARATION))
			{  
				OWLEntity ent = ax.getEntity();
				if (ent.isOWLDatatype())
				{
					OWLDatatype dt = ent.asOWLDatatype();
					Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(dt, o, label);
					if (annotations != null)
					{
						if (annotations.size() > 1)
							exit("Error: There are more than" + annotations.size() + " annotations for datatype " + dt + ".");
						else if (annotations.size() == 1)
						{
							Iterator<OWLAnnotation> it = annotations.iterator();
							OWLAnnotation next = it.next();
							Object ob = Parser.getDatatype(next.getValue().toString());
							if (ob != null)
								writeType1Datatypes(ob, getShortName(dt));
						}
					}
				}
			}
	
			for (OWLDeclarationAxiom ax : o.getAxioms(AxiomType.DECLARATION))
			{
				OWLEntity ent = ax.getEntity();
				if (ent.isOWLDatatype())
				{
					OWLDatatype dt = ent.asOWLDatatype();
					Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(dt, o, label);
					if (annotations != null)
					{
						if (annotations.size() == 1)
						{
							Iterator<OWLAnnotation> it = annotations.iterator();
							OWLAnnotation next = it.next();
							Object ob = Parser.getDatatype(next.getValue().toString());
							if (ob != null)
								writeType2Datatypes(ob, getShortName(dt));
						}
					}
				}
			}
	
			for (OWLDeclarationAxiom ax : o.getAxioms(AxiomType.DECLARATION))
			{
				OWLEntity ent = ax.getEntity();
				if (ent.isOWLDatatype())
				{
					OWLDatatype dt = ent.asOWLDatatype();
					Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(dt, o, label);
					if (annotations != null)
					{
						if (annotations.size() == 1)
						{
							Iterator<OWLAnnotation> it = annotations.iterator();
							OWLAnnotation next = it.next();
							Object ob = Parser.getDatatype(next.getValue().toString());
							if (ob != null)
								writeType3Datatypes(ob, getShortName(dt));
						}
					}
				}
			}
		}
	}

	/**
	 * Write fuzzy concept definitions, defined with OWL 2 concept annotations.
	 */
	protected void processConceptAnnotations()
	{
		for(OWLOntology o : ontologies)
		{
			for (OWLDeclarationAxiom ax : o.getAxioms(AxiomType.DECLARATION))
			{
				OWLEntity ent = ax.getEntity();
				if (ent.isOWLClass())
				{
					OWLClass cls = ent.asOWLClass();
					Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(cls, o, label);
	
					if (annotations.size() > 1)
						exit("Error: There are " + annotations.size() + " class annotations for " + cls + ".");
					else if (annotations.size() == 1)
					{
						String name = getShortName(cls);
	
						ConceptDefinition c = Parser.getDefinedConcept(annotations.iterator().next().getValue().toString());
						if (c != null)
						{
							switch (c.getType())
							{
								case MODIFIED_CONCEPT:
									String modName = c.getFuzzyModifier();
									if (fuzzyModifiers.containsKey(modName))
									{
										ModifiedConcept md = new ModifiedConcept(modName, c.getFuzzyConcept());
										definedConcepts.put(name, md);
										writeModifiedConceptDefinition(name, md);
									}
									else
										exit("Error: Fuzzy modifier " + modName + " not defined.");
									break;
	
								case FUZZY_NOMINAL:
									FuzzyNominalConcept nc = new FuzzyNominalConcept(c.getNumber(), c.getIndividual());
									definedConcepts.put(name, nc);
									writeFuzzyNominalConceptDefinition(name, nc);
									break;
	
								case WEIGHTED_CONCEPT:
									WeightedConcept wc = new WeightedConcept(c.getNumber(), c.getFuzzyConcept());
									definedConcepts.put(name, wc);
									writeWeightedConceptDefinition(name, wc);
									break;
	
								case WEIGHTED_MAX:
									List<ConceptDefinition> sourceList = c.getWeightedConcepts();
									ArrayList<WeightedConcept> list = new ArrayList<>();
									for(ConceptDefinition def : sourceList)
										list.add(new WeightedConcept(def.getNumber(), def.getFuzzyConcept() ) );
									WeightedMaxConcept wmax = new WeightedMaxConcept(list);
									definedConcepts.put(name, wmax);
									writeWeightedMaxConceptDefinition(name, wmax);
									break;
									
								case WEIGHTED_MIN:
									sourceList = c.getWeightedConcepts();
									list = new ArrayList<>();
									for(ConceptDefinition def : sourceList)
										list.add(new WeightedConcept(def.getNumber(), def.getFuzzyConcept() ) );
									WeightedMinConcept wmin = new WeightedMinConcept(list);
									definedConcepts.put(name, wmin);
									writeWeightedMinConceptDefinition(name, wmin);
									break;
	
								case WEIGHTED_SUM:
									sourceList = c.getWeightedConcepts();
									list = new ArrayList<>();
									for(ConceptDefinition def : sourceList)
										list.add(new WeightedConcept(def.getNumber(), def.getFuzzyConcept() ) );
									WeightedSumConcept wsum = new WeightedSumConcept(list);
									definedConcepts.put(name, wsum);
									writeWeightedSumConceptDefinition(name, wsum);
									break;
	
								case OWA:
									List<Double> weights = c.getWeights();
									List<String> concepts = c.getConcepts();
									if (weights.size() != concepts.size())
										exit("Error: OWA concept " + name + " has different number of weights and concepts.");
									else
									{
										OwaConcept owa = new OwaConcept(weights, concepts);
										definedConcepts.put(name, owa);
										writeOwaConceptDefinition(name, owa);
									}
									break;
	
								case CHOQUET:
									weights = c.getWeights();
									concepts = c.getConcepts();
									if (weights.size() != concepts.size())
										exit("Error: Choquet concept " + name + " has different number of weights and concepts.");
									else
									{
										ChoquetConcept owa = new ChoquetConcept(weights, concepts);
										definedConcepts.put(name, owa);
										writeChoquetConceptDefinition(name, owa);
									}
									break;
	
								case SUGENO:
									weights = c.getWeights();
									concepts = c.getConcepts();
									if (weights.size() != concepts.size())
										exit("Error: Sugeno concept " + name + " has different number of weights and concepts.");
									else
									{
										SugenoConcept owa = new SugenoConcept(weights, concepts);
										definedConcepts.put(name, owa);
										writeSugenoConceptDefinition(name, owa);
									}
									break;
	
								case QUASI_SUGENO:
									weights = c.getWeights();
									concepts = c.getConcepts();
									if (weights.size() != concepts.size())
										exit("Error: QuasiSugeno concept " + name + " has different number of weights and concepts.");
									else
									{
										QuasiSugenoConcept owa = new QuasiSugenoConcept(weights, concepts);
										definedConcepts.put(name, owa);
										writeQuasiSugenoConceptDefinition(name, owa);
									}
									break;
	
								case QUANTIFIER_OWA:
									String q = c.getQuantifier();
									if (!fuzzyDatatypes.containsKey(q))
										exit("Error: Quantifier " + q + " not defined.");
									else // if (fuzzyDatatypes.containsKey(q))
									{
										FuzzyDatatype def = fuzzyDatatypes.get(q);
										if (!(def instanceof RightShoulderFunction) && !(def instanceof LinearFunction))
											exit("Error: Quantifier " + q + " must be a right-shoulder or a linear function.");
										else {
											concepts = c.getConcepts();
											QowaConcept qowa = new QowaConcept(q, concepts);
											definedConcepts.put(name, qowa);
											writeQowaConceptDefinition(name, qowa);
										}
									}
									break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Write fuzzy property definitions, defined with OWL 2 concept annotations.
	 */
	protected void processPropertyAnnotations()
	{
		for(OWLOntology o : ontologies)
		{
			for (OWLDeclarationAxiom ax : o.getAxioms(AxiomType.DECLARATION))
			{
				OWLEntity ent = ax.getEntity();
	
				if (ent.isOWLObjectProperty() || ent.isOWLDataProperty())
				{
					OWLProperty prop;
					if (ent.isOWLObjectProperty() )
						prop = ent.asOWLObjectProperty();
					else // if (ent.isOWLDataProperty() )
						prop = ent.asOWLDataProperty();
	
					Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(prop, o, label);
	
					if (annotations.size() > 1)
						exit("Error: There are " + annotations.size() + " property annotations for " + prop + ".");
					else if (annotations.size() == 1)
					{
						PropertyDefinition pro = Parser.getDefinedProperty(annotations.iterator().next().getValue().toString());
						if (pro != null)
						{
							if (pro.getType() == PropertyDefinition.PropertyType.MODIFIED_PROPERTY)
							{
								String name = getShortName(prop);
								String modName = pro.getFuzzyModifier();
								if (fuzzyModifiers.containsKey(modName))
								{
									ModifiedProperty mp = new ModifiedProperty(modName, pro.getProperty());
									definedProperties.put(name, mp);
									writeModifiedPropertyDefinition(name, mp);
								}
								else
									exit("Error: Fuzzy modifier " + modName + " not defined.");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Write the axioms of the OWL 2 ontology. They can have annotations or not.
	 */
	protected void processOntologyAxioms()
	{

		for(OWLOntology o : ontologies)
		{

			// ******
			//  TBOx
			// ******
			for (OWLDisjointClassesAxiom ax : o.getAxioms(AxiomType.DISJOINT_CLASSES))
			{
				Set<OWLClassExpression> c = ax.getClassExpressions();
				writeDisjointClassesAxiom(c);
			}
	
			for (OWLDisjointUnionAxiom ax : o.getAxioms(AxiomType.DISJOINT_UNION))
			{
				Set<OWLClassExpression> c = ax.getClassExpressions();
				writeDisjointUnionAxiom(c);
			}
	
			for (OWLSubClassOfAxiom ax : o.getAxioms(AxiomType.SUBCLASS_OF))
			{
				OWLClassExpression subclass = ax.getSubClass();
				OWLClassExpression superclass = ax.getSuperClass();
				double d = getDegree(ax);
				writeSubclassOfAxiom(subclass, superclass, d);
			}
	
			for (OWLEquivalentClassesAxiom ax : o.getAxioms(AxiomType.EQUIVALENT_CLASSES))
			{
				Set<OWLClassExpression> c = ax.getClassExpressions();
				writeEquivalentClassesAxiom(c);
			}
	
			for (OWLClass c : o.getClassesInSignature())
			{
				if (c.isTopEntity() == false)
					writeConceptDeclaration(c);
			}
	
	
			// ******
			//  RBOx
			// ******
	
			for (OWLSubObjectPropertyOfAxiom ax : o.getAxioms(AxiomType.SUB_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression subProperty = ax.getSubProperty();
				OWLObjectPropertyExpression superProperty = ax.getSuperProperty();
				double d = getDegree(ax);
				writeSubObjectPropertyOfAxiom(subProperty, superProperty, d);
			}
	
			for (OWLSubDataPropertyOfAxiom ax : o.getAxioms(AxiomType.SUB_DATA_PROPERTY))
			{
				OWLDataPropertyExpression subProperty = ax.getSubProperty();
				OWLDataPropertyExpression superProperty = ax.getSuperProperty();
				double d = getDegree(ax);
				writeSubDataPropertyOfAxiom(subProperty, superProperty, d);
			}
	
			for (OWLSubPropertyChainOfAxiom ax : o.getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF))
			{
				List<OWLObjectPropertyExpression> chain = ax.getPropertyChain();
				OWLObjectPropertyExpression superProperty = ax.getSuperProperty();
				double d = getDegree(ax);
				writeSubPropertyChainOfAxiom(chain, superProperty, d);
			}
	
			for (OWLEquivalentObjectPropertiesAxiom ax : o.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES))
			{
				Set<OWLObjectPropertyExpression> set = ax.getProperties();
				writeEquivalentObjectPropertiesAxiom(set);
			}
	
			for (OWLEquivalentDataPropertiesAxiom ax : o.getAxioms(AxiomType.EQUIVALENT_DATA_PROPERTIES))
			{
				Set<OWLDataPropertyExpression> set = ax.getProperties();
				writeEquivalentDataPropertiesAxiom(set);
			}
	
			for (OWLTransitiveObjectPropertyAxiom ax : o.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeTransitiveObjectPropertyAxiom(p);
			}
	
			for (OWLSymmetricObjectPropertyAxiom ax : o.getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeSymmetricObjectPropertyAxiom(p);
			}
	
			for (OWLAsymmetricObjectPropertyAxiom ax : o.getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeAsymmetricObjectPropertyAxiom(p);
			}
	
			for (OWLReflexiveObjectPropertyAxiom ax : o.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeReflexiveObjectPropertyAxiom(p);
			}
	
			for (OWLIrreflexiveObjectPropertyAxiom ax : o.getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeIrreflexiveObjectPropertyAxiom(p);
			}
	
			for (OWLFunctionalObjectPropertyAxiom ax : o.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeFunctionalObjectPropertyAxiom(p);
			}
	
			for (OWLFunctionalDataPropertyAxiom ax : o.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY))
			{
				OWLDataPropertyExpression p = ax.getProperty();
				writeFunctionalDataPropertyAxiom(p);
			}
	
			for (OWLInverseObjectPropertiesAxiom ax : o.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES))
			{
				OWLObjectPropertyExpression p1 = ax.getFirstProperty();
				OWLObjectPropertyExpression p2 = ax.getSecondProperty();
				writeInverseObjectPropertiesAxiom(p1, p2);
			}
	
			for (OWLInverseFunctionalObjectPropertyAxiom ax : o.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				writeInverseFunctionalObjectPropertyAxiom(p);
			}
	
			for (OWLObjectPropertyDomainAxiom ax : o.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				OWLClassExpression c = ax.getDomain();
				writeObjectPropertyDomainAxiom(p, c);
			}
	
			for (OWLObjectPropertyRangeAxiom ax : o.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				OWLClassExpression c = ax.getRange();
				writeObjectPropertyRangeAxiom(p, c);
			}
	
			for (OWLDataPropertyDomainAxiom ax : o.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN))
			{
				OWLDataPropertyExpression p = ax.getProperty();
				OWLClassExpression c = ax.getDomain();
				writeDataPropertyDomainAxiom(p, c);
			}
	
			for (OWLDataPropertyRangeAxiom ax : o.getAxioms(AxiomType.DATA_PROPERTY_RANGE))
			{
				OWLDataPropertyExpression p = ax.getProperty();
				OWLDataRange range = ax.getRange();
				writeDataPropertyRangeAxiom(p, range);
			}
	
			for (OWLDisjointObjectPropertiesAxiom ax : o.getAxioms(AxiomType.DISJOINT_OBJECT_PROPERTIES))
			{
				Set<OWLObjectPropertyExpression> properties = ax.getProperties();
				writeDisjointObjectPropertiesAxiom(properties);
			}
	
			for (OWLDisjointDataPropertiesAxiom ax : o.getAxioms(AxiomType.DISJOINT_DATA_PROPERTIES))
			{
				Set<OWLDataPropertyExpression> properties = ax.getProperties();
				writeDisjointDataPropertiesAxiom(properties);
			}
	
/*	
			for (OWLDataPropertyExpression dp : o.getDataPropertiesInSignature())
			{
				writeDataPropertyDeclaration(dp);
			}
	
	
			for (OWLObjectPropertyExpression op : o.getObjectPropertiesInSignature())
			{
				writeObjectPropertyDeclaration(op);
			}
*/
	
			// ******
			//  ABOx
			// ******
	
			for (OWLClassAssertionAxiom ax : o.getAxioms(AxiomType.CLASS_ASSERTION))
			{
				OWLClassExpression c = ax.getClassExpression();
				OWLIndividual i = ax.getIndividual();
				double d = getDegree(ax);
				writeConceptAssertionAxiom(i, c, d);
			}
	
			for (OWLObjectPropertyAssertionAxiom ax : o.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				OWLIndividual i1 = ax.getSubject();
				OWLIndividual i2 = ax.getObject();
				double d = getDegree(ax);
				writeObjectPropertyAssertionAxiom(i1, i2, p, d);
			}
	
			for (OWLDataPropertyAssertionAxiom ax : o.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION))
			{
				OWLDataPropertyExpression p = ax.getProperty();
				OWLIndividual i1 = ax.getSubject();
				OWLLiteral i2 = ax.getObject();
				double d = getDegree(ax);
				writeDataPropertyAssertionAxiom(i1, i2, p, d);
			}
	
			for (OWLNegativeObjectPropertyAssertionAxiom ax : o.getAxioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION))
			{
				OWLObjectPropertyExpression p = ax.getProperty();
				OWLIndividual i1 = ax.getSubject();
				OWLIndividual i2 = ax.getObject();
				double d = getDegree(ax);
				writeNegativeObjectPropertyAssertionAxiom(i1, i2, p, d);
			}
	
			for (OWLNegativeDataPropertyAssertionAxiom ax : o.getAxioms(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION))
			{
				OWLDataPropertyExpression p = ax.getProperty();
				OWLIndividual i1 = ax.getSubject();
				OWLLiteral i2 = ax.getObject();
				double d = getDegree(ax);
				writeNegativeDataPropertyAssertionAxiom(i1, i2, p, d);
			}
	
			for (OWLSameIndividualAxiom ax: o.getAxioms(AxiomType.SAME_INDIVIDUAL))
			{
				Set<OWLIndividual> set = ax.getIndividuals();
				writeSameIndividualAxiom(set);
			}
	
			for (OWLDifferentIndividualsAxiom ax: o.getAxioms(AxiomType.DIFFERENT_INDIVIDUALS))
			{
				Set<OWLIndividual> set = ax.getIndividuals();
				writeDifferentIndividualsAxiom(set);
			}
		}
	}

	/**
	 * Gets a String representation of an OWL 2 class.
	 *
	 * @param c An OWL 2 class.
	 * @return A String representation of c.
	 */
	public String getClassName(OWLClassExpression c)
	{

		switch (c.getClassExpressionType())
		{
			case OWL_CLASS:

				OWLClass d = (OWLClass) c;
				if (d.isOWLThing())
					return getTopConceptName();
				else if (d.isOWLNothing())
					return getBottomConceptName();
				else
				   // Atomic concept
					return getAtomicConceptName(d);

			case OBJECT_INTERSECTION_OF:

				Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) c).getOperands();
				return getObjectIntersectionOfName(operands);

			case OBJECT_UNION_OF:

				operands = ((OWLObjectUnionOf) c).getOperands();
				return getObjectUnionOfName(operands);

			case OBJECT_SOME_VALUES_FROM:

				OWLObjectPropertyExpression p = ((OWLObjectSomeValuesFrom) c).getProperty();
				OWLClassExpression e = ((OWLObjectSomeValuesFrom) c).getFiller();
				return getObjectSomeValuesFromName(p, e);

			case OBJECT_ALL_VALUES_FROM:

				p = ((OWLObjectAllValuesFrom) c).getProperty();
				e = ((OWLObjectAllValuesFrom) c).getFiller();
				return getObjectAllValuesFromName(p, e);

			case DATA_SOME_VALUES_FROM:

				OWLDataPropertyExpression dp = ((OWLDataSomeValuesFrom) c).getProperty();
				OWLDataRange range = ((OWLDataSomeValuesFrom) c).getFiller();
				return getDataSomeValuesFromName(dp, range);

			case DATA_ALL_VALUES_FROM:

				dp = ((OWLDataAllValuesFrom) c).getProperty();
				range = ((OWLDataAllValuesFrom) c).getFiller();
				return getDataAllValuesFromName(dp, range);

			case OBJECT_COMPLEMENT_OF:

				e = ((OWLObjectComplementOf) c).getOperand();
				return getObjectComplementOfName(e);

			case OBJECT_HAS_SELF:

				p = ((OWLObjectHasSelf) c).getProperty();
				return getObjectHasSelfName(p);

			case OBJECT_ONE_OF:

				Set<OWLIndividual> set = ((OWLObjectOneOf) c).getIndividuals();
				return getObjectOneOfName(set);

			case OBJECT_HAS_VALUE:

				OWLObjectHasValue hasValue = (OWLObjectHasValue) c;
				OWLIndividual i = hasValue.getFiller();
				p = hasValue.getProperty();
				return getObjectHasValueName(p, i);

			case DATA_HAS_VALUE:

				OWLDataHasValue dataHasValue = (OWLDataHasValue) c;
				OWLLiteral lit = dataHasValue.getFiller();
				dp = dataHasValue.getProperty();
				return getDataHasValueName(dp, lit);

			case OBJECT_MAX_CARDINALITY:

				OWLObjectCardinalityRestriction q = (OWLObjectMaxCardinality) c;
				p = q.getProperty();
				int card = q.getCardinality();
				if (q.isQualified())
					return getObjectMaxCardinalityRestrictionName(card, p, q.getFiller());
				else
					return getObjectMaxCardinalityRestrictionName(card, p);

			case OBJECT_MIN_CARDINALITY:

				q = (OWLObjectMinCardinality) c;
				p = q.getProperty();
				card = q.getCardinality();
				if (q.isQualified())
					return getObjectMinCardinalityRestrictionName(card, p, q.getFiller());
				else
					return getObjectMinCardinalityRestrictionName(card, p);

			case OBJECT_EXACT_CARDINALITY:

				q = (OWLObjectExactCardinality) c;
				p = q.getProperty();
				card = q.getCardinality();
				if (q.isQualified())
					return getObjectExactCardinalityRestrictionName(card, p, q.getFiller());
				else
					return getObjectExactCardinalityRestrictionName(card, p);

			case DATA_MAX_CARDINALITY:

				OWLDataCardinalityRestriction dq = (OWLDataMaxCardinality) c;
				dp = dq.getProperty();
				card = dq.getCardinality();
				if (dq.isQualified())
					return getDataMaxCardinalityRestrictionName(card, dp, dq.getFiller());
				else
					return getDataMaxCardinalityRestrictionName(card, dp);

			case DATA_MIN_CARDINALITY:

				dq = (OWLDataMinCardinality) c;
				dp = dq.getProperty();
				card = dq.getCardinality();
				if (dq.isQualified())
					return getDataMinCardinalityRestrictionName(card, dp, dq.getFiller());
				else
				   return getDataMinCardinalityRestrictionName(card, dp);

			case DATA_EXACT_CARDINALITY:

				dq = (OWLDataExactCardinality) c;
				dp = dq.getProperty();
				card = dq.getCardinality();
				if (dq.isQualified())
					return getDataExactCardinalityRestrictionName(card, dp, dq.getFiller());
				else
					return getDataExactCardinalityRestrictionName(card, dp);

				default:
					print("Print class of type " + c.getClassExpressionType());
				return "";
		}
	}

	/**
	 * Gets a String representation of an OWL 2 object property.
	 *
	 * @param p An OWL 2 object property.
	 * @return A String representation of p.
	 */
	public String getObjectPropertyName(OWLObjectPropertyExpression p)
	{
		if (p.isOWLTopObjectProperty())
			return getTopObjectPropertyName();
		else if (p.isOWLBottomObjectProperty())
			return getBottomObjectPropertyName();
		else
			return getAtomicObjectPropertyName(p.asOWLObjectProperty());
	}

	/**
	 * Gets a String representation of an OWL 2 data property.
	 *
	 * @param p An OWL 2 data property.
	 * @return A String representation of p.
	 */
	public String getDataPropertyName(OWLDataPropertyExpression p)
	{
		if (p.isOWLTopDataProperty())
			return getTopDataPropertyName();
		else if (p.isOWLBottomDataProperty())
			return getBottomDataPropertyName();
		else
			return getAtomicDataPropertyName(p.asOWLDataProperty());
	}

	/**
	 * Returns the degree in the annotation of an axiom.
	 * @param axiom An OWLAxiom.
	 * @return The degree in the annotation of an axiom; 1 if it does not exist.
	 */
	protected double getDegree(OWLAxiom axiom)
	{
		Set<OWLAnnotation> annotations = axiom.getAnnotations(label);

		if (annotations.size() != 1)
		{
			if (annotations.size() > 1)
				print("Error: There are " + annotations.size() + " annotations for axiom " + axiom + ".");
			return 1;
		}
		else
			return Parser.getDegree(annotations.iterator().next().getValue().toString());
	}

	private void setK1AndK2(FuzzyDatatype dat, double[] k)
	{
		dat.setMinValue(k[0]);
		dat.setMaxValue(k[1]);
	}

	private void getK1AndK2(String name, double[] k)
	{
		k[0] = NEG_INFINITY;
		k[1] = POS_INFINITY;

		for (OWLOntology o : ontologies)
		{
			Set<OWLDatatypeDefinitionAxiom> set = o.getAxioms(AxiomType.DATATYPE_DEFINITION);
			if (set != null)
			{
				Iterator<OWLDatatypeDefinitionAxiom> it2 = set.iterator();
	
				String datatypeName = "";
				OWLDatatypeDefinitionAxiom def;
				do
				{
					def = it2.next();
					datatypeName = pm.getShortForm(def.getDatatype()).replace(":", "");
				} while (it2.hasNext() && (datatypeName.compareTo(name) != 0));
	
				if (datatypeName.compareTo(name) == 0)
				{
					if (def.getDataRange().getDataRangeType() == DataRangeType.DATA_INTERSECTION_OF)
					{
						OWLDataIntersectionOf inter = (OWLDataIntersectionOf) def.getDataRange();
						Set<OWLDataRange> operands = inter.getOperands();
						if ((operands != null) && (operands.size() == 2))
						{
							Iterator<OWLDataRange> it3 = operands.iterator();
							OWLDataRange r1 = it3.next();
							OWLDataRange r2 = it3.next();
							if ( (r1.getDataRangeType() == DataRangeType.DATATYPE_RESTRICTION) && (r2.getDataRangeType() == DataRangeType.DATATYPE_RESTRICTION) )
							{
								OWLDatatypeRestriction rest1 = (OWLDatatypeRestriction) r1;
								OWLDatatypeRestriction rest2 = (OWLDatatypeRestriction) r2;
								Set<OWLFacetRestriction> set1 = rest1.getFacetRestrictions();
								Set<OWLFacetRestriction> set2 = rest2.getFacetRestrictions();
								if ( (set1 != null) && (set2 != null) && (set1.size() == 1) && (set2.size() == 1) )
								{
									OWLFacetRestriction f1 = rest1.getFacetRestrictions().iterator().next();
									OWLFacetRestriction f2 = rest2.getFacetRestrictions().iterator().next();
									if (f1.getFacet() == OWLFacet.MIN_INCLUSIVE)
										k[0] = Double.parseDouble(f1.getFacetValue().getLiteral());
									else if (f1.getFacet() == OWLFacet.MAX_INCLUSIVE)
										k[1] = Double.parseDouble(f1.getFacetValue().getLiteral());
									if (f2.getFacet() == OWLFacet.MIN_INCLUSIVE)
										k[0] = Double.parseDouble(f2.getFacetValue().getLiteral());
									else if (f2.getFacet() == OWLFacet.MAX_INCLUSIVE)
										k[1] = Double.parseDouble(f2.getFacetValue().getLiteral());
								}
							}
						}
					}
				}
			}
		}
	}

	protected static String[] processParameters(String[] args)
	{
		boolean versionRequested = false;
		int numParams = args.length;
		String[] returnValue = new String[2]; 

		if ((args.length >= 1) && args[0].equals("--version"))
		{
			System.out.println(FuzzyOwl2.class.getSimpleName() + " version: " + 1.0);
			versionRequested = true;
			numParams--;
		}
			
		if (numParams < 1)
			exit("Error. Use: java " + FuzzyOwl2.class.getSimpleName() + " <Owl2Ontology> ( <outputFileName> ).\n" +
				" Example: java " + FuzzyOwl2.class.getSimpleName() + " c:\\\\fuzzyWine.owl test.txt \n" +
				" Example: java " + FuzzyOwl2.class.getSimpleName() + " http://www.co-ode.org/ontologies/pizza/pizza.owl c:\\ont\\test.txt \n");
		else
		{
			if (versionRequested)
			{
				returnValue[0] = args[1];
				if (numParams == 2)
					returnValue[1] = args[2];
			}
			else
			{
				returnValue[0] = args[0];
				returnValue[1] = args[1];
			}
		}
		return returnValue;
	}

	// ******************************************************
	// Methods that should be overwritten by the subclasses.
	// ******************************************************

	/**
	 * Gets the short name (without namespaces) of an OWL 2 entity.
	 * @param e An OWL 2 entity.
	 * @return Short name of e.
	 */
	public String getShortName(OWLEntity e)
	{
		return pm.getShortForm(e);
	}

	/**
	 * Gets a String representation of an OWL 2 individual.
	 *
	 * @param i An OWL 2 individual.
	 * @return A String representation of i.
	 */
	public String getIndividualName(OWLIndividual i)
	{
		if (i.isAnonymous())
		{
			print("Anonymous individual not supported");
			return null;
		}
		else
		{
			String name = getShortName(i.asOWLNamedIndividual());
			print("Print individual " + name);
			return name;
		}
	}

	protected String getTopConceptName()
	{
		print("Print Top concept");
		return "";
	}

	protected String getBottomConceptName()
	{
		print("Print Bottom concept");
		return "";
	}

	protected String getAtomicConceptName(OWLClass c)
	{
		String name = getShortName(c);
		print("Print Atomic concept" + name);
		return "";
	}

	protected String getObjectIntersectionOfName(Set<OWLClassExpression> operands)
	{
		print("Print ObjectIntersectionOf" + operands);
		return "";
	}

	protected String getObjectUnionOfName(Set<OWLClassExpression> operands)
	{
		print("Print ObjectUnionOf" + operands);
		return "";
	}

	protected String getObjectSomeValuesFromName(OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Print ObjectSomeValuesFrom(" + p + " " + c + ")");
		return "";
	}

	protected String getObjectAllValuesFromName(OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Print ObjectAllValuesFrom(" + p + " " + c + ")");
		return "";
	}

	protected String getDataSomeValuesFromName(OWLDataPropertyExpression p, OWLDataRange range)
	{
		print("Print DataSomeValuesFrom(" + p + " " + range + ")");
		return "";
	}

	protected String getDataAllValuesFromName(OWLDataPropertyExpression p, OWLDataRange range)
	{
		print("Print DataAllValuesFrom(" + p + " " + range + ")");
		return "";
	}

	protected String getObjectComplementOfName(OWLClassExpression c)
	{
		print("Print ObjectComplement(" + c + ")");
		return "";
	}

	protected String getObjectHasSelfName(OWLObjectPropertyExpression p)
	{
		print("Print ObjectHasSelf(" + p + ")");
		return "";
	}

	protected String getObjectOneOfName(Set<OWLIndividual> set)
	{
		print("Print ObjectOneOf(" + set + ")");
		return "";
	}

	protected String getObjectHasValueName(OWLObjectPropertyExpression p, OWLIndividual i)
	{
		print("Print ObjectHasValue(" + p + " " + i + ")");
		return "";
	}

	protected String getDataHasValueName(OWLDataPropertyExpression p, OWLLiteral lit)
	{
		print("Print DataHasValue(" + p + " " + lit + ")");
		return "";
	}

	protected String getObjectMinCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Print ObjectMinCardinalityRestriction(" + card + " " + p + " " + c + ")");
		return "";
	}

	protected String getObjectMinCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)
	{
		print("Print ObjectMinCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getObjectMaxCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Print ObjectMaxCardinalityRestriction(" + card + " " + p + " " + c + ")");
		return "";
	}

	protected String getObjectMaxCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)
	{
		print("Print ObjectMaxCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getObjectExactCardinalityRestrictionName(int card, OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Print ObjectExactCardinalityRestriction(" + card + " " + p + " " + c + ")");
		return "";
	}

	protected String getObjectExactCardinalityRestrictionName(int card, OWLObjectPropertyExpression p)
	{
		print("Print ObjectExactCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getDataMinCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)
	{
		print("Print DataMinCardinalityRestriction(" + card + " " + p + " " + range + ")");
		return "";
	}

	protected String getDataMinCardinalityRestrictionName(int card, OWLDataPropertyExpression p)
	{
		print("Print DataMinCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getDataMaxCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)
	{
		print("Print DataMaxCardinalityRestriction(" + card + " " + p + " " + range + ")");
		return "";
	}

	protected String getDataMaxCardinalityRestrictionName(int card, OWLDataPropertyExpression p)
	{
		print("Print DataMaxCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getDataExactCardinalityRestrictionName(int card, OWLDataPropertyExpression p, OWLDataRange range)
	{
		print("Print DataExactCardinalityRestriction(" + card + " " + p + " " + range + ")");
		return "";
	}

	protected String getDataExactCardinalityRestrictionName(int card, OWLDataPropertyExpression p)
	{
		print("Print DataExactCardinalityRestriction(" + card + " " + p + " " + ")");
		return "";
	}

	protected String getTopObjectPropertyName()
	{
		print("Write top object property");
		return "";
	}

	protected String getBottomObjectPropertyName()
	{
		print("Write bottom object property");
		return "";
	}

	protected String getAtomicObjectPropertyName(OWLObjectProperty p)
	{
		print("Write object property + " + getShortName(p) );
		return "";
	}

	protected String getTopDataPropertyName()
	{
		print("Write top object property");
		return "";
	}

	protected String getBottomDataPropertyName()
	{
		print("Write bottom object property");
		return "";
	}

	protected String getAtomicDataPropertyName(OWLDataProperty p)
	{
		print("Write object property + " + getShortName(p) );
		return "";
	}

	protected void writeFuzzyLogic(FuzzyLogic logic)
	{
		print("Write fuzzy logic " + logic);
	}

	protected void writeConceptDeclaration(OWLClassExpression c)
	{
		print("Write declaration " + c);
	}

	protected void writeDataPropertyDeclaration(OWLDataPropertyExpression dp)
	{
		print("Write declaration " + dp);
	}

	protected void writeObjectPropertyDeclaration(OWLObjectPropertyExpression op)
	{
		print("Write declaration " + op);
	}

	protected void writeConceptAssertionAxiom(OWLIndividual i, OWLClassExpression c, double d)
	{
		print("Write axiom " + i + " : " + c + " >= " + d);
	}

	protected void writeObjectPropertyAssertionAxiom(OWLIndividual i1, OWLIndividual i2, OWLObjectPropertyExpression p, double d)
	{
		print("Write axiom (" + i1 + " , " + i2 + ") : " + p + " >= " + d);
	}

	protected void writeDataPropertyAssertionAxiom(OWLIndividual i1, OWLLiteral i2, OWLDataPropertyExpression p, double d)
	{
		print("Write axiom (" + i1 + " , " + i2 + ") : " + p + " >= " + d);
	}

	protected void writeNegativeObjectPropertyAssertionAxiom(OWLIndividual i1, OWLIndividual i2, OWLObjectPropertyExpression p, double d)
	{
		print("Write axiom (" + i1 + " , " + i2 + ") : NOT " + p + " >= " + d);
	}

	protected void writeNegativeDataPropertyAssertionAxiom(OWLIndividual i1, OWLLiteral i2, OWLDataPropertyExpression p, double d)
	{
		print("Write axiom (" + i1 + " , " + i2 + ") : NOT " + p + " >= " + d);
	}

	protected void writeSameIndividualAxiom(Set<OWLIndividual> set)
	{
		print("Write axiom SameIndividual(" + set + ")");
	}

	protected void writeDifferentIndividualsAxiom(Set<OWLIndividual> set)
	{
		print("Write axiom DifferentIndividuals(" + set + ")");
	}

	protected void writeDisjointClassesAxiom(Set<OWLClassExpression> set)
	{
		print("Write axiom DisjointClasses(" + set + ")");
	}

	protected void writeDisjointUnionAxiom(Set<OWLClassExpression> set)
	{
		print("Write axiom DisjointUnion(" + set + ")");
	}

	protected void writeSubclassOfAxiom(OWLClassExpression subclass, OWLClassExpression superclass, double d)
	{
		print("Write axiom SubClassOf(" + subclass + " is subclass of " + superclass + " >= " + d + ")");
	}

	protected void writeEquivalentClassesAxiom(Set<OWLClassExpression> set)
	{
		print("Write axiom EquivalentClasses(" + set + ")");
	}

	protected void writeSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty, OWLObjectPropertyExpression superProperty, double d)
	{
		print("Write axiom SubObjectPropertyOf(" + subProperty + " is subclass of " + superProperty + " >= " + d + ")");
	}

	protected void writeSubPropertyChainOfAxiom(List<OWLObjectPropertyExpression> chain, OWLObjectPropertyExpression superProperty, double d)
	{
		print("Write axiom SubPropertyChainOf(" + chain + " is subclass of " + superProperty + " >= " + d + ")");
	}

	protected void writeSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty, OWLDataPropertyExpression superProperty, double d)
	{
		print("Write axiom SubDataPropertyOf(" + subProperty + " is subclass of " + superProperty + " >= " + d + ")");
	}

	protected void writeEquivalentObjectPropertiesAxiom(Set<OWLObjectPropertyExpression> set)
	{
		print("Write axiom EquivalentObjectProperties(" + set + ")");
	}

	protected void writeEquivalentDataPropertiesAxiom(Set<OWLDataPropertyExpression> set)
	{
		print("Write axiom EquivalentDataProperties(" + set + ")");
	}

	protected void writeTransitiveObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom TransitiveObjectProperty(" + p + ")");
	}

	protected void writeSymmetricObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom SymmetricObjectProperty(" + p + ")");
	}

	protected void writeAsymmetricObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom AsymmetricObjectProperty(" + p + ")");
	}

	protected void writeReflexiveObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom ReflexiveObjectProperty(" + p + ")");
	}

	protected void writeIrreflexiveObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom IrreflexiveObjectProperty(" + p + ")");
	}

	protected void writeFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom FunctionalObjectProperty(" + p + ")");
	}

	protected void writeFunctionalDataPropertyAxiom(OWLDataPropertyExpression p)
	{
		print("Write axiom FunctionalDataProperty(" + p + ")");
	}

	protected void writeInverseObjectPropertiesAxiom(OWLObjectPropertyExpression p1, OWLObjectPropertyExpression p2)
	{
		print("Write axiom (" + p1 + " inverse of " + p2 + ")");
	}

	protected void writeInverseFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression p)
	{
		print("Write axiom InverseFunctionalObjectProperty(" + p + ")");
	}

	protected void writeObjectPropertyDomainAxiom(OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Write axiom domain (" + c + " of object property" + p + ")");
	}

	protected void writeObjectPropertyRangeAxiom(OWLObjectPropertyExpression p, OWLClassExpression c)
	{
		print("Write axiom range (" + c + " of object property" + p + ")");
	}

	protected void writeDataPropertyDomainAxiom(OWLDataPropertyExpression p, OWLClassExpression c)
	{
		print("Write axiom domain (" + c + " of data property" + p + ")");
	}

	protected void writeDataPropertyRangeAxiom(OWLDataPropertyExpression p, OWLDataRange c)
	{
		print("Write axiom range (" + c + " of data property" + p + ")");
	}

	protected void writeDisjointObjectPropertiesAxiom(Set<OWLObjectPropertyExpression> set)
	{
		print("Write axiom (" + set + ")");
	}

	protected void writeDisjointDataPropertiesAxiom(Set<OWLDataPropertyExpression> set)
	{
		print("Write axiom (" + set + ")");
	}

	protected void writeTriangularModifierDefinition(String name, TriangularModifier mod)
	{
		print("Write definition " + name + " = " + mod);
	}

	protected void writeLinearModifierDefinition(String name, LinearModifier mod)
	{
		print("Write definition " + name + " = " + mod);
	}

	protected void writeLeftShoulderFunctionDefinition(String name, LeftShoulderFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeRightShoulderFunctionDefinition(String name, RightShoulderFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeLinearFunctionDefinition(String name, LinearFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeTriangularFunctionDefinition(String name, TriangularFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeTrapezoidalFunctionDefinition(String name, TrapezoidalFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeModifiedFunctionDefinition(String name, ModifiedFunction dat)
	{
		print("Write definition " + name + " = " + dat);
	}

	protected void writeModifiedPropertyDefinition(String name, ModifiedProperty c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeModifiedConceptDefinition(String name, ModifiedConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeFuzzyNominalConceptDefinition(String name, FuzzyNominalConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeWeightedConceptDefinition(String name, WeightedConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeWeightedMaxConceptDefinition(String name, WeightedMaxConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeWeightedMinConceptDefinition(String name, WeightedMinConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeWeightedSumConceptDefinition(String name, WeightedSumConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeOwaConceptDefinition(String name, OwaConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeChoquetConceptDefinition(String name, ChoquetConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeSugenoConceptDefinition(String name, SugenoConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeQuasiSugenoConceptDefinition(String name, QuasiSugenoConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

	protected void writeQowaConceptDefinition(String name, QowaConcept c)
	{
		print("Write definition " + name + " = " + c);
	}

}