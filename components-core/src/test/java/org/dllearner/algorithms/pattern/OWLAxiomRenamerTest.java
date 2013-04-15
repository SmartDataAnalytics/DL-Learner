package org.dllearner.algorithms.pattern;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class OWLAxiomRenamerTest {
	private OWLDataFactory df;
	
	private OWLClass clsA;
	private OWLClass clsB;
	private OWLClass clsC;
	private OWLObjectProperty propR;
	private OWLObjectProperty propS;
	private OWLIndividual indA;
	private OWLIndividual indB;
	
	private OWLAxiomRenamer renamer;

	@Before
	public void setUp() throws Exception {
		df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager("http://examples.org/ontology#");
		
		clsA = df.getOWLClass("A", pm);
		clsB = df.getOWLClass("B", pm);
		clsC = df.getOWLClass("C", pm);
		
		propR = df.getOWLObjectProperty("r", pm);
		propS = df.getOWLObjectProperty("s", pm);
		
		indA = df.getOWLNamedIndividual("a", pm);
		indB = df.getOWLNamedIndividual("b", pm);
		
		renamer = new OWLAxiomRenamer(df);
	}

	@Test
	public void testRename() {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		OWLAxiom ax1 = df.getOWLSubClassOfAxiom(clsA, clsB);
		OWLAxiom ax2 = df.getOWLSubClassOfAxiom(clsB, clsC);
		
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax1);
		assertEquals(ax1, ax2);
		
		ax1 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						clsB, 
						df.getOWLObjectSomeValuesFrom(propR, clsC)));
		ax2 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						df.getOWLObjectSomeValuesFrom(propS, clsB), 
						clsC));
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax2);
		assertEquals(ax1, ax2);
		
		ax1 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						clsB, 
						df.getOWLObjectSomeValuesFrom(
								propR, 
								df.getOWLObjectUnionOf(clsB, clsA))));
		ax2 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						df.getOWLObjectSomeValuesFrom(
								propS, 
								df.getOWLObjectUnionOf(clsB, clsA)), 
						clsB));
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax2);
		assertEquals(ax1, ax2);
		
		
	}
	
	public void testRenameOntology() throws OWLOntologyCreationException{
		String ontologyURL = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntology(IRI.create(ontologyURL));
		
		OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);
		Multiset<OWLAxiom> multiset = HashMultiset.create();
		for (OWLAxiom axiom : ontology.getLogicalAxioms()) {
			OWLAxiom renamedAxiom = renamer.rename(axiom);
			multiset.add(renamedAxiom);
//			System.out.println(axiom + "-->" + renamedAxiom);
		}
		for (OWLAxiom owlAxiom : multiset.elementSet()) {
			System.out.println(owlAxiom + ": " + multiset.count(owlAxiom));
		}
	}

}
