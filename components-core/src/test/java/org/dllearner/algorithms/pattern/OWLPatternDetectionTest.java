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

import org.apache.log4j.Logger;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.repository.LocalDirectoryOntologyRepository;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OWLPatternDetectionTest {
	
//	@Before
	public void setUp() throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
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

//	@Test
	public void testLocalDir(){
		OntologyRepository repository = new LocalDirectoryOntologyRepository(new File("/media/me/Work-Ext/datasets/owlxml_mowlcorp/files"));
		repository.initialize();
		OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository);
		patternFinder.start();
	}

}
