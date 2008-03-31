package org.dllearner.tools.protege;

import org.protege.editor.owl.OWLEditorKit;

public class DLLearnerConfig {

	
	
	public void DLLearnerStart()
	{
		System.out.println("blub");
		/*ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		String example = "examples/family/father.owl";
		cm.applyConfigEntry(source, "url", new File(example).toURI().toString());
		source.init();
		
		// create DIG reasoning service with standard settings
		ReasonerComponent reasoner = cm.reasoner(DIGReasoner.class, source);
		// ReasoningService rs = cm.reasoningService(DIGReasonerNew.class, source);
		reasoner.init();
		ReasoningService rs = cm.reasoningService(reasoner);
		
		// create a learning problem and set positive and negative examples
		LearningProblem lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		Set<String> positiveExamples = new TreeSet<String>();
		positiveExamples.add("http://example.com/father#stefan");
		positiveExamples.add("http://example.com/father#markus");
		positiveExamples.add("http://example.com/father#martin");
		Set<String> negativeExamples = new TreeSet<String>();
		negativeExamples.add("http://example.com/father#heinz");
		negativeExamples.add("http://example.com/father#anna");
		negativeExamples.add("http://example.com/father#michelle");
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		lp.init();
		
		// create the learning algorithm
		LearningAlgorithm la = null;
		try {
			la = cm.learningAlgorithm(RandomGuesser.class, lp, rs);
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cm.applyConfigEntry(la, "numberOfTrees", 100);
		cm.applyConfigEntry(la, "maxDepth", 5);
		la.init();
		
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getBestSolution());*/
	}
}
