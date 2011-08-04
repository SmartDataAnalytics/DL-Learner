/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.gui;

import java.awt.Color;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;

/**
 * Class, which is responsible for executing the init method of
 * a component in a different thread and displaying a "please wait"
 * message while the initialisation is in process.
 * 
 * @author Jens Lehmann
 *
 */
public class InitWorker extends SwingWorker<Boolean, Boolean> {
	
	private List<AbstractComponent> components;
	private StartGUI gui;
	private boolean timeIntensive;
	
	public InitWorker(List<AbstractComponent> components, StartGUI gui) {
		this.components = components;
		this.gui = gui;
		
		// create a list of components, which do need virtually
		// no time to initialise (and where displaying a please
		// wait message is an unnecessary overhead)
		List<Class<? extends AbstractComponent>> nonTimeIntensiveComponents = new LinkedList<Class<? extends AbstractComponent>>();
		nonTimeIntensiveComponents.add(OWLFile.class);
		nonTimeIntensiveComponents.add(KBFile.class);
		
		// we check if any of the components is time-intensive
		timeIntensive = false;
		for(AbstractComponent component : components) {
			if(!nonTimeIntensiveComponents.contains(component.getClass())) {
				timeIntensive = true;
			}		
		}

	}	    	
	
	@Override
	protected Boolean doInBackground() throws Exception {
		
		JFrame waitFrame = null;
		if(timeIntensive) { 
	//		gui.getStatusPanel().setStatus("Initialising reasoner ... ");
	    	gui.disableTabbedPane();
	    	gui.setEnabled(false);
	    	waitFrame = new JFrame();
	    	waitFrame.setUndecorated(true);
	    	waitFrame.setSize(160, 100);
	    	waitFrame.getContentPane().setBackground(Color.WHITE);
	    	URL imgURL = Config.class.getResource("ajaxloader.gif");
	//    	ImageIcon waitIcon = new ImageIcon(imgURL, "wait");	        	
	//    	waitFrame.add(new JLabel("Wait!"), waitIcon, SwingConstants.RIGHT);
	//    	waitFrame.add(new JLabel("Wait"));
	//    	JLabel iconLabel = new JLabel(waitIcon);
	//    	iconLabel.setOpaque(true);
	//    	iconLabel.setBackground(Color.RED);
	//    	iconLabel.setForeground(Color.RED);
	//    	waitFrame.add(iconLabel, BorderLayout.NORTH);
	    	waitFrame.add(new JLabel("<html><br /><p align=\"center\"><img src=\"" + imgURL + "\" /><br /> &nbsp;&nbsp;Initialising component.<br />Please wait.</p></html>"));
	    	waitFrame.setLocationRelativeTo(gui);
	    	waitFrame.setVisible(true);
		}
    	
    	try {
    		for(AbstractComponent component : components) {
    			component.init();
    			
    			// when the reasoner has been initialised, we need to update
    			// the option panel (such that the user can see the existing
    			// examples, classes etc.)
    			if(component instanceof AbstractReasonerComponent) {
    				gui.panels[2].updateOptionPanel();
    				gui.panels[3].updateOptionPanel();
    			}		
    		}
		} catch (ComponentInitException e) {
			gui.getStatusPanel().setExceptionMessage(e.getMessage());
			e.printStackTrace();
		}
		
		if(timeIntensive) {
			gui.enableTabbedPane();
			gui.setEnabled(true);
	//		gui.getStatusPanel().extendMessage("done.");
			waitFrame.dispose();
		}

		return true;
	}
}
