/**
 * 
 */
package org.dllearner.algorithms.miles;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.PosNegLP;

/**
 * First draft of a new kind of learning algorithm:
 * The basic idea is as follows: 
 * We take an existing learning algorithm which holds internally a search tree.
 * During the base algorithm run, we query periodically for nodes in the search tree,
 * and check if a linear combination gives better results.
 * @author Lorenz Buehmann
 *
 */
public class MILES {
	
	
	private AbstractCELA la;
	private PosNegLP lp;
	private AbstractReasonerComponent rc;
	
	private int delay = 0;
	private int period = 1000;

	public MILES(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rc) {
		this.la = la;
		this.lp = lp;
		this.rc = rc;
	}
	
	public void start(){
		// 1. start the base learning algorithm in a separate thread
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				la.start();
			}
		});
		t.start();
		
		// 2. each x seconds get the top n concepts and validate the linear combination
		Timer timer = new Timer();
		timer.schedule(new LinearClassificationTask(), delay, period);
		
	}
	
	class LinearClassificationTask extends TimerTask {
	    public void run() {
	       List<Description> descriptions = la.getCurrentlyBestDescriptions(5);
	       
	       DescriptionLinearClassifier classifier = new DescriptionLinearClassifier(lp, rc);
	       classifier.getLinearCombination(descriptions);
	    }
	}

}
