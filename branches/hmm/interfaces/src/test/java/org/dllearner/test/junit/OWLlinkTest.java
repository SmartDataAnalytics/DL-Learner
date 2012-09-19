package org.dllearner.test.junit;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dllearner.algorithms.gp.GP;
import org.dllearner.cli.QuickStart;
import org.dllearner.cli.Start;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;

public class OWLlinkTest {
	
	private static final int EXAMPLE_COUNT = 5;
	private static final boolean RANDOMIZE = true;
	private static final String OWL_LINK_URL = "http://localhost:8080/";
	
	@Test
	public void testOWLlink(){
		try {
			// map containing a list of conf files for each path
			HashMap<String, ArrayList<String>> confFiles = new HashMap<String, ArrayList<String>>();
			String exampleDir = "." + File.separator + "examples";
			File f = new File(exampleDir);
			QuickStart.getAllConfs(f, exampleDir, confFiles);

			// put all examples in a flat list
			List<String> examples = new LinkedList<String>();
			for(Map.Entry<String,ArrayList<String>> entry : confFiles.entrySet()) {
				for(String file : entry.getValue()) {
					examples.add(entry.getKey() + file + ".conf");
				}
			}
			
			if(RANDOMIZE) {
				Collections.shuffle(examples, new Random());
			} else {
				Collections.sort(examples);
			}
			
			int cnt = 0;
			Start start;
			AbstractReasonerComponent rc;
			for(String conf : examples) {
				if(cnt == EXAMPLE_COUNT){
					break;
				}
				start = new Start(new File(conf));
				if(start.getLearningAlgorithm() instanceof GP || start.getSources().iterator().next() instanceof SparqlKnowledgeSource){
					continue;
				}
				rc = start.getReasonerComponent();
				if(rc instanceof OWLAPIReasoner){
					((OWLAPIReasoner)rc).setReasonerTypeString("owllink");
					((OWLAPIReasoner)rc).setOwlLinkURL(OWL_LINK_URL);
				} else if(rc instanceof FastInstanceChecker){
					((FastInstanceChecker)rc).getReasonerComponent().setReasonerTypeString("owllink");
					((FastInstanceChecker)rc).getReasonerComponent().setOwlLinkURL(OWL_LINK_URL);
				} else {
					continue;
				}
				System.out.println("Testing " + conf);
				rc.init();
				start.getLearningAlgorithm().start();
				cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			assert ( false );
		}
			

	}

}
