package org.dllearner.algorithms.pattern;

import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.junit.Before;
import org.semanticweb.owlapi.io.ToStringRenderer;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;


public class OWLPatternDetectionTest {
	
	@Before
	public void setUp() throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
	}

//	@Test
	public void testTONESRepository(){
		OntologyRepository repository = new TONESRepository();
		repository.initialize();
		OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository);
		patternFinder.start();
	}
	
//	@Test
	public void testBioPortalRepository(){
		OntologyRepository repository = new BioPortalRepository();
		repository.initialize();
		OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository);
		patternFinder.start();
	}

}
