package org.dllearner.algorithms.pattern;

import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.junit.Test;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;


public class OWLPatternDetectionTest {
	
//	@Before
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
	
	@Test
	public void test(){
		
	}

}
