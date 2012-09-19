/**
 * Copyright (C) 2011, Jens Lehmann
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
 *
 */
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.IOException;

import org.dllearner.cli.CLI;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.springframework.context.ApplicationContext;

/**
 * Currently, the script is NLP2RDF evaluation specific, but may be generalised later.
 * 
 * - take array of conf files as arguments
 * - run cross val over them and get basic stats
 * - output to a CSV file
 * - run through gnumeric
 * 
 * @author Jens Lehmann
 *
 */
public class NLP2RDFEval {

	public static void main(String args[]) throws IOException {
		
		// TODO: convert those things into command line arguments to make the script generic		
		int nrOfFolds = 5;
		
//		String confs[] = new String[] {
//			"../examples/nlp2rdf/learning_7_components/stanford/gas9_vs_nat-gas26.conf",
//			"../examples/nlp2rdf/learning_7_components/opennlp/gas9_vs_nat-gas26.conf",
//			"../examples/nlp2rdf/learning_7_components/gateannie/gas9_vs_nat-gas26.conf",
//			"../examples/nlp2rdf/learning_7_components/dbpedia_spotlight/gas9_vs_nat-gas26.conf",
//			"../examples/nlp2rdf/learning_7_components/dbpedia_spotlight_plus/gas9_vs_nat-gas26.conf",
//		};
//		
//		String baseDir = "../examples/nlp2rdf/learning_7_components/";
		String baseDir = "../examples/nlp2rdf/learning_reduced/";
		
		String outputFile = "results.csv";
		String content = "";
		
		// gate + spotlight plus; gate + snowball; standford + spotlight plus
		String[] tools = new String[] {
				"gateannie_dbpedia_spotlight_plus", 
				// "gateannie_snowball", zu langsam
				"stanford_dbpedia_spotlight_plus", "stanford", "opennlp", "gateannie", "dbpedia_spotlight", "dbpedia_spotlight_plus", "snowball" 
		};
		
		String topics[] = new String[] {
			"gas9_vs_nat-gas26", "copper17_vs_gold35", "oilseed11_vs_soybean35", "veg-oil18_vs_palm-oil17"
		};
		// shorter labels for diagram
		String topiclabels[] = new String[] {
				"gas", "copper", "oilseed", "veg-oil"
		};
		
		for(String tool : tools) {
			content += "," + tool;
		}
		content += "\n";
		
		// loop through topics
		for(int i=0; i<topics.length; i++) {
			content += topiclabels[i] + ",";
			
			for(String tool : tools) {
				String conf = baseDir + tool + "/" + topics[i] + ".conf";
				File confFile = new File(conf);
				System.out.print("Next file: " + confFile);
				CLI cli = new CLI(confFile);
				cli.init();
				System.out.println("File " + confFile + " initialised.");
				
				// perform cross validation
				ApplicationContext context = cli.getContext();
				AbstractReasonerComponent rs = context.getBean(AbstractReasonerComponent.class);
				PosNegLP lp = context.getBean(PosNegLP.class);
				AbstractCELA la = context.getBean(AbstractCELA.class);
				CrossValidation cv = new CrossValidation(la,lp,rs,nrOfFolds,false);
				content += Math.round(cv.getfMeasure().getMean())+",";
			}
			content += "\n";
		}
		
		Files.createFile(new File(baseDir + outputFile), content);
		
	}
	
}
