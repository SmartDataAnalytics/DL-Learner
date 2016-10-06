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
package org.dllearner.algorithms.qtl.qald;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Lists;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class BioMedicalKB extends KB {
	
	File localBiomedicalDataDir = new File(
			"/home/me/work/datasets/qald4/biomedical"
//			"/home/user/work/datasets/qald4/biomedical"
			);
	
	public BioMedicalKB() throws Exception{
		id = "BioMedical";
		
		SparqlEndpoint endpoint = new SparqlEndpoint(
				new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
				"http://biomedical.org");
		
		Model model = loadBiomedicalData();
		ks = new LocalModelBasedSparqlEndpointKS(model);
	
		questionFiles = Lists.newArrayList(
				"org/dllearner/algorithms/qtl/qald-4_biomedical_train.xml",
				"org/dllearner/algorithms/qtl/qald-4_biomedical_test.xml"
				);
	
	}

	private Model loadBiomedicalData() {
		System.out.println("Loading QALD biomedical data from local directory " + localBiomedicalDataDir + " ...");
		Model model = ModelFactory.createDefaultModel();

		for (File file : localBiomedicalDataDir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".nt")) {
				try (FileInputStream is = new FileInputStream(file)) {
					model.read(is, null, "N-TRIPLES");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("...done.");
		return model;
	}

}
