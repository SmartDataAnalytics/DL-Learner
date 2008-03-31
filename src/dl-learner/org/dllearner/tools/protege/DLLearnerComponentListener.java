package org.dllearner.tools.protege;

import java.awt.event.*;

public class DLLearnerComponentListener implements ItemListener {

	 public void itemStateChanged(ItemEvent e) {
	        if (e.getStateChange() == ItemEvent.SELECTED) {
	            
	        	System.out.println("JUHU "+e.getStateChange());

	        } else {
	        
	        }
	    }
}
