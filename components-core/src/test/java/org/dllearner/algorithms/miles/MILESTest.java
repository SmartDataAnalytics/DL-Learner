/**
 * 
 */
package org.dllearner.algorithms.miles;

import static org.junit.Assert.*;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Test;

/**
 * @author Lorenz Buehmann
 *
 */
public class MILESTest {

	/**
	 * Test method for {@link org.dllearner.algorithms.miles.MILES#start()}.
	 */
	@Test
	public void testStart() throws Exception{
		KnowledgeSource ks = new OWLFile("../examples/swore/swore.rdf");
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(new NamedClass("http://ns.softwiki.de/req/CustomerRequirement"));
		lp.init();
		
		CELOE celoe = new CELOE(lp, rc);
		celoe.setNoisePercentage(1.0);
		celoe.setMaxExecutionTimeInSeconds(20);
		celoe.init();
		
		MILES miles = new MILES(celoe, lp, rc);
		miles.start();
	}

}
