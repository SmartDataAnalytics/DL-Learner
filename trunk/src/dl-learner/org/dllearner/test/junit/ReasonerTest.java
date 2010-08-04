package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.owl.ConceptComparator;
import org.junit.Test;

public class ReasonerTest {
	
	private ConceptComparator comparator = new ConceptComparator();
	
	@Test
	public void compareReasoners() throws FileNotFoundException, ComponentInitException, ParseException{
		
		ComponentManager cm = ComponentManager.getInstance();
		Start start;
		FastInstanceChecker reasoner;
		LearningProblem lp;
		LearningAlgorithm la;
		KnowledgeSource ks;
		
		for(File conf : getTestConfigFiles()){
			start = new Start(conf);
			lp = start.getLearningProblem();
			la = start.getLearningAlgorithm();
			ks = start.getSources().iterator().next();
			
			TreeSet<? extends EvaluatedDescription> result = new TreeSet<EvaluatedDescription>();
			
			for(String type : getReasonerTypes()){
				System.out.println("Using " + type + " reasoner...");
				try {
					reasoner = cm.reasoner(FastInstanceChecker.class, ks);
					reasoner.getConfigurator().setReasonerType(type);
					reasoner.init();
					
					lp.changeReasonerComponent(reasoner);
					lp.init();
					
					la.init();
					la.start();
					if(!result.isEmpty()){
						assertTrue(compareTreeSets(la.getCurrentlyBestEvaluatedDescriptions(), result));
					}
					
					result = la.getCurrentlyBestEvaluatedDescriptions();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private Set<File> getTestConfigFiles(){
		Set<File> files = new HashSet<File>();
		File directory = new File("examples" + File.separator + "testReasoners");
		for(File file : directory.listFiles()){
			if(file.toString().endsWith(".conf")){
				files.add(file);
			}
		}
		return files;
	}
	
	private List<String> getReasonerTypes(){
		List<String> reasonerTypes = new LinkedList<String>();
		reasonerTypes.add("pellet");
		reasonerTypes.add("hermit");
		reasonerTypes.add("fact");
		
		return reasonerTypes;
	}
	
	public boolean compareTreeSets(TreeSet<? extends EvaluatedDescription> tree1, TreeSet<? extends EvaluatedDescription> tree2){
		boolean equal = true;
		
		List<? extends EvaluatedDescription> list1 = new ArrayList<EvaluatedDescription>(tree1);
		List<? extends EvaluatedDescription> list2 = new ArrayList<EvaluatedDescription>(tree2);
		
		EvaluatedDescription d1;
		EvaluatedDescription d2;
		for(int i = 0; i < list1.size(); i++){
			d1 = list1.get(i);
			d2 = list2.get(i);
			if(!(comparator.compare(d1.getDescription(), d2.getDescription()) == 0) && 
					d1.getAccuracy() == d2.getAccuracy()){
				equal = false;
				break;
			}
		}
		
		return equal;
	}

}
