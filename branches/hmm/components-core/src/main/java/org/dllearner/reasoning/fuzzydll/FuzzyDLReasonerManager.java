/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.reasoning.fuzzydll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.reasoner.impl.DefaultNode;
import org.semanticweb.owlapi.reasoner.impl.NodeFactory;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.Version;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import fuzzydl.*;
import fuzzydl.exception.FuzzyOntologyException;
import fuzzydl.milp.Solution;
import fuzzydl.parser.*;
import fuzzydll.fuzzyowl2fuzzydlparser.*;

public class FuzzyDLReasonerManager implements OWLReasoner {

	// private static final String CHANGING_JUST_HIERARCHI_PROBLEM = "../test/fuzzydll/fuzzyOWL2fuzzyDLparserOutput_manual.fuzzyDL.txt";
	private static final String FUZZYOWL2FUZZYDLPARSEROUTPUT = "../test/fuzzydll/fuzzyOWL2fuzzyDLparserOutput.fuzzyDL.txt";
	private static String CONFIG_FILENAME = "../test/fuzzydll/CONFIG";

	private Solution queryResult;
	private KnowledgeBase fuzzyKB;
	private Parser parser;
	private SimpleShortFormProvider shortFormParser;

	private FuzzyOwl2toFuzzyDL fuzzyFileParser;
	private int auxCounter = 0;
	private PelletReasoner crispReasoner;
	private OWLDataFactory factory;
	private String baseURI;
	private NodeSet<OWLClass> newOwlInstances;
	
	// TODO: remove, just for testing purposes
	// private FileOutputStream errorFile;
//	private PrintStream out;
//	private int counter = 1;
//	private int counter2 = 1;

	public FuzzyDLReasonerManager(String ontologyFile, OWLOntology ontology, OWLReasonerConfiguration conf, OWLDataFactory factory, String baseURI) throws Exception {
		
		// TODO: remove, just for testing purposes
//		FileOutputStream fstream;
//		try {			
//			fstream = new FileOutputStream("../examples/fuzzydll/milpSolverLogs.log");
//			out = new PrintStream(fstream);		
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		this.factory = factory;
		
		this.baseURI = baseURI;
		
		startPellet(ontology, conf);
		
		queryResult = null;
		parser = null;

		shortFormParser = new SimpleShortFormProvider();

		ConfigReader.loadParameters(CONFIG_FILENAME, new String[0]);

		fuzzyKB = parseOWLontologyToFuzzyDLsyntax(ontologyFile);
//		fuzzyFileParser.setBaseKB(fuzzyKB);
		OWLAPI_fuzzyDLObjectParser.setParsingFuzzyKB(fuzzyFileParser, fuzzyKB);
		
		solveKB();
		
		  // errorFile = new FileOutputStream("errorFile.txt");
	}

	private void startPellet(OWLOntology ontology, OWLReasonerConfiguration conf) {
		// instantiate Pellet reasoner
		crispReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology, conf);
		// change log level to WARN for Pellet, because otherwise log
		// output will be very large
		Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
		pelletLogger.setLevel(Level.WARN);		
	}

	private void solveKB() {
		try {
			fuzzyKB.solveKB();
		} catch (FuzzyOntologyException e) {
			e.printStackTrace();
		}	
	}

	private KnowledgeBase parseOWLontologyToFuzzyDLsyntax(String ontologyFile) throws Exception {
		// TODO added by Josue: we may use an in-memory file in the future and not a HD one
		// As the parser doesn't work at 100% a manually-edited fuzzyDL-file is used

		fuzzyFileParser = new FuzzyOwl2toFuzzyDL(ontologyFile, FUZZYOWL2FUZZYDLPARSEROUTPUT);
		fuzzyFileParser.translateOwl2Ontology();

//		System.err.println("WARNING: you're using a particular fuzzy ontology");
//		parser = new Parser(new FileInputStream(CHANGING_JUST_HIERARCHI_PROBLEM));
		parser = new Parser(new FileInputStream(FUZZYOWL2FUZZYDLPARSEROUTPUT));

		parser.Start();
		return parser.getKB();
	}

	// added by Josue
	public double getFuzzyMembership(OWLClassExpression oce, OWLIndividual i) {

			Individual fIndividual = fuzzyKB.getIndividual(shortFormParser.getShortForm((OWLEntity) i));
			Concept fConcept = OWLAPI_fuzzyDLObjectParser.getFuzzyDLExpresion(oce);
			
			Query q = new MinInstanceQuery(fConcept, fIndividual);

			try {
				KnowledgeBase clonedFuzzyKB = fuzzyKB.clone();
				
				// TODO: just for testing, remove
//				long start = System.nanoTime();
				
				queryResult = q.solve(clonedFuzzyKB);
				
				// TODO: just for testing, remove
//				out.println(counter + " * " + (System.nanoTime() - start));
//				counter++;

				if (!queryResult.isConsistentKB()){
					System.err.println("Fuzzy KB is inconsistent.");
					System.err.println("This may be a fuzzyDL reasoner bug. Press enter to continue.");
					System.err.println("concept: " + fConcept + " individual: " + fIndividual);
					Scanner sc = new Scanner(System.in);
					sc.nextLine();
					// System.exit(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
//				try {
//					errorFile.write(fIndividual.toString().getBytes());
//					errorFile.write("\n".getBytes());
//					errorFile.write(fConcept.toString().getBytes());
//					errorFile.write("\n".getBytes());
//					errorFile.write(getStackTrace(e).getBytes());
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				Scanner sc = new Scanner(System.in);
//				sc.nextLine();		
			}
			
			// return (1 - Math.abs(truthDegree - queryResult.getSolution()));
			return queryResult.getSolution();
	}

	public KnowledgeBase getFuzzyKB() {
		return fuzzyKB;
	}
	
    public static String getStackTrace(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		crispReasoner.dispose();
	}

	@Override
	public void flush() {
		
		crispReasoner.flush();
	}

	@Override
	public Node<OWLClass> getBottomClassNode() {
		
		return crispReasoner.getBottomClassNode();
	}

	@Override
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		
		return crispReasoner.getBottomDataPropertyNode();
	}

	@Override
	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		
		return crispReasoner.getBottomObjectPropertyNode();
	}

	@Override
	public BufferingMode getBufferingMode() {
		
		return crispReasoner.getBufferingMode();
	}

	@Override
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0,
			boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.getDataPropertyDomains(arg0, arg1);
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual arg0,
			OWLDataProperty arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.getDataPropertyValues(arg0, arg1);
	}

	@Override
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			OWLNamedIndividual arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.getDifferentIndividuals(arg0);
	}

	@Override
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression arg0)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException {
		
		return crispReasoner.getDisjointClasses(arg0);
	}

	@Override
	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			OWLDataPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getDisjointDataProperties(arg0);
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getDisjointObjectProperties(arg0);
	}

	@Override
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression arg0)
			throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getEquivalentClasses(arg0);
	}

	@Override
	public Node<OWLDataProperty> getEquivalentDataProperties(
			OWLDataProperty arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.getEquivalentDataProperties(arg0);
	}

	@Override
	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getEquivalentObjectProperties(arg0);
	}

	@Override
	public FreshEntityPolicy getFreshEntityPolicy() {
		
		return crispReasoner.getFreshEntityPolicy();
	}

	@Override
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		
		return crispReasoner.getIndividualNodeSetPolicy();
	}

	@Override
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0,
			boolean arg1) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		// commented by Josue in order to use fuzzyDL and not Pellet to answer this OWLAPI method
		// return crispReasoner.getInstances(arg0, arg1);
		
		// added by Josue in order to use fuzzyDL and not Pellet to answer this OWLAPI method
		boolean differentInstances = false;
		Solution localQuerySolution = null;
		NodeSet<OWLNamedIndividual> owlApiOutput = crispReasoner.getInstances(arg0, arg1);
		Set<OWLNamedIndividual> owlApiInstances = owlApiOutput.getFlattened();
		for (Individual fuzzyIndividual : fuzzyKB.individuals.values()) {
			// TODO this "query process" is repeated several times --> create a (private) method
			Query localQuery = new MinInstanceQuery(OWLAPI_fuzzyDLObjectParser.getFuzzyDLExpresion(arg0), fuzzyIndividual);
			try {
				KnowledgeBase clonedFuzzyKB = fuzzyKB.clone();
				localQuerySolution = localQuery.solve(clonedFuzzyKB);
				if (!localQuerySolution.isConsistentKB()){
					System.err.println("Fuzzy KB is inconsistent.");
					System.err.println("This may be a fuzzyDL reasoner bug. Press enter to continue.");
					System.err.println("concept: " + arg0 + " individual: " + fuzzyIndividual);
					Scanner sc = new Scanner(System.in);
					sc.nextLine();
				}
			} catch (Exception e) {
				e.printStackTrace();		
			}
			if (localQuerySolution.getSolution() == 0) {
				for (OWLNamedIndividual owlApiSingleInstance : owlApiOutput.getFlattened()) {
					String a = baseURI.concat(fuzzyIndividual.toString());
					String b = owlApiSingleInstance.toStringID();
					if (a.equals(b)) {
						owlApiInstances.remove(owlApiSingleInstance);	
						differentInstances = true;					}
				}	
			}
		}
		
		if (differentInstances){
			Set<Node<OWLNamedIndividual>> instances = new HashSet<Node<OWLNamedIndividual>>();
			Iterator<OWLNamedIndividual> fi = owlApiInstances.iterator();
			while (fi.hasNext()) {
				DefaultNode<OWLNamedIndividual> e = NodeFactory.getOWLNamedIndividualNode( fi.next() );
				instances.add(e);
			}
			return new OWLNamedIndividualNodeSet( instances );
		}
		else 
			return owlApiOutput;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getInverseObjectProperties(arg0);
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyDomains(
			OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getObjectPropertyDomains(arg0, arg1);
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyRanges(
			OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getObjectPropertyRanges(arg0, arg1);
	}

	@Override
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getObjectPropertyValues(arg0, arg1);
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomAdditions() {
		
		return crispReasoner.getPendingAxiomAdditions();
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomRemovals() {
		
		return crispReasoner.getPendingAxiomRemovals();
	}

	@Override
	public List<OWLOntologyChange> getPendingChanges() {
		
		return crispReasoner.getPendingChanges();
	}

	@Override
	public Set<InferenceType> getPrecomputableInferenceTypes() {
		
		return crispReasoner.getPrecomputableInferenceTypes();
	}

	@Override
	public String getReasonerName() {
		
		return crispReasoner.getReasonerName();
	}

	@Override
	public Version getReasonerVersion() {
		
		return crispReasoner.getReasonerVersion();
	}

	@Override
	public OWLOntology getRootOntology() {
		
		return crispReasoner.getRootOntology();
	}

	@Override
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getSameIndividuals(arg0);
	}

	@Override
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression arg0, boolean arg1)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException,
			ClassExpressionNotInProfileException {
		
		return crispReasoner.getSubClasses(arg0, arg1);
	}

	@Override
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0,
			boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.getSubDataProperties(arg0, arg1);
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
			OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getSubObjectProperties(arg0, arg1);
	}

	@Override
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0,
			boolean arg1) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
//			System.out.println(arg0);
//
//			Query q1, q2;
//			for(Concept concept : fuzzyKB.atomicConcepts.values()) {
//				System.out.print(" - " + concept + " ");
//				q1 = new MinSubsumesQuery(concept, OWLAPI_fuzzyDLObjectParser.getFuzzyDLExpresion(arg0), MinSubsumesQuery.LUKASIEWICZ);
//				q2 = new MaxSubsumesQuery(concept, OWLAPI_fuzzyDLObjectParser.getFuzzyDLExpresion(arg0), MaxSubsumesQuery.LUKASIEWICZ);
//				KnowledgeBase clonedFuzzyKB = fuzzyKB.clone();
//				try {
//					Solution queryResult1 = q1.solve(clonedFuzzyKB);
//					Solution queryResult2 = q2.solve(clonedFuzzyKB);
//					System.out.print(queryResult1 + " " + queryResult2);
//					System.out.println();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}


		
		return crispReasoner.getSuperClasses(arg0, arg1);
	}

	@Override
	public NodeSet<OWLDataProperty> getSuperDataProperties(
			OWLDataProperty arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getSuperDataProperties(arg0, arg1);
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getSuperObjectProperties(arg0, arg1);
	}

	@Override
	public long getTimeOut() {
		
		return crispReasoner.getTimeOut();
	}

	@Override
	public Node<OWLClass> getTopClassNode() {
		
		return crispReasoner.getTopClassNode();
	}

	@Override
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		
		return crispReasoner.getTopDataPropertyNode();
	}

	@Override
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		
		return crispReasoner.getTopObjectPropertyNode();
	}

	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		
		return crispReasoner.getTypes(arg0, arg1);
	}

	@Override
	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		
		return crispReasoner.getUnsatisfiableClasses();
	}

	@Override
	public void interrupt() {
		
		crispReasoner.interrupt();
	}

	@Override
	public boolean isConsistent() throws ReasonerInterruptedException,
			TimeOutException {
		
		return crispReasoner.isConsistent();
	}

	@Override
	public boolean isEntailed(OWLAxiom arg0)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {

		// TODO: just for testing, remove
//		long start = System.nanoTime();
		
		boolean outBoolean = crispReasoner.isEntailed(arg0);
		
		// TODO: just for testing, remove
//		out.println(counter2 + " + " + (System.nanoTime() - start));
//		counter2++;
		
		 return outBoolean;
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> arg0)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		
		// commented by Josue
		// return crispReasoner.isEntailed(arg0);
		
		// added by Josue
		System.err.println("Method not supported yet");
		System.exit(0);
		return false;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> arg0) {
		
		return crispReasoner.isEntailmentCheckingSupported(arg0);
	}

	@Override
	public boolean isPrecomputed(InferenceType arg0) {
		
		return crispReasoner.isPrecomputed(arg0);
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression arg0)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		
		return crispReasoner.isSatisfiable(arg0);
	}

	@Override
	public void precomputeInferences(InferenceType... arg0)
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		crispReasoner.precomputeInferences(arg0);
	}
}
