package org.dllearner.algorithms.qtl.qald;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
