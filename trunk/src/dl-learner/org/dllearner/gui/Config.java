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

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLP;

// import org.dllearner.core.Component;

/**
 * Config save all together used variables: ComponentManager, KnowledgeSource,
 * Reasoner, ReasoningService, LearningProblem, LearningAlgorithm; also inits of
 * these components.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 */
public class Config {
	
	private ComponentManager cm = ComponentManager.getInstance();
	private static Logger logger = Logger.getLogger(Config.class);
	
	// the components currently active
	private KnowledgeSource source;
	private ReasonerComponent reasoner;
	private ReasoningService rs;
	private LearningProblem lp;
	private LearningAlgorithm la;

	// stores which components need to be initialised (either 
	// because they have not been initialiased or previous components
	// have changed configuration options, which require initialisation)
	private boolean[] needsInit = new boolean[4];
	
	// specifies whether the panel is enabled, i.e. the user
	// can select it (all mandatory variables in selected components have been choosen)
	private boolean[] isEnabled = new boolean[4];
	
	// learning algorithm status
	private boolean threadIsRunning = false;
	private Long algorithmRunStartTime = null;
	private Long algorithmRunStopTime = null;
	
	private StartGUI gui;
	
	public Config(StartGUI gui) {
		this.gui = gui;
		// none of the components is initialised
		for(int i=0; i<4; i++) {
			needsInit[i] = true;
			// TODO there might be knowledge source without mandatory options
			isEnabled[i] = false;
		}
	}
	
	/**
	 * Get ComponentManager.
	 * 
	 * @return ComponentManager
	 */
	public ComponentManager getComponentManager() {
		return cm;
	}

	/**
	 * It is necessary for init KnowledgeSource.
	 * 
	 * @return true, if url was set otherwise false
	 */
	public boolean isSetURL() {
		if (cm.getConfigOptionValue(source, "url") != null
				|| cm.getConfigOptionValue(source, "filename") != null)
			return true;
		else
			return false;
	}

	/**
	 * Set KnowledgeSource.
	 * 
	 * @param knowledgeSource
	 */
//	public void setKnowledgeSource(KnowledgeSource knowledgeSource) {
//		source = knowledgeSource;
//	}

	/**
	 * Get KnowledgeSource.
	 * 
	 * @return KnowledgeSource
	 */
	public KnowledgeSource getKnowledgeSource() {
		return source;
	}

	/**
	 * Creates a knowledge source and makes it the active source.
	 * @param clazz
	 * @return
	 */
	public KnowledgeSource newKnowledgeSource(Class<? extends KnowledgeSource> clazz) {
		source = cm.knowledgeSource(clazz);
//		logger.debug("new knowledge source " + clazz + " created");
		return source;
	}
	
	/**
	 * Changes active knowledge source. This method does not not only 
	 * create a knowledge source, but also updates the active reasoner
	 * to use the new knowledge source.
	 * @param clazz
	 */
	public KnowledgeSource changeKnowledgeSource(Class<? extends KnowledgeSource> clazz) {
		source = cm.knowledgeSource(clazz);
//		logger.debug("knowledge source " + clazz + " changed");
		// create a new reasoner object using the current class and the selected source
		reasoner = cm.reasoner(reasoner.getClass(), source);
		return source;
	}
	
	/**
	 * Set Reasoner.
	 * 
	 * @param reasoner
	 */
//	public void setReasoner(ReasonerComponent reasoner) {
//		this.reasoner = reasoner;
//	}

	/**
	 * Get Reasoner.
	 * 
	 * @return reasoner
	 */
	public ReasonerComponent getReasoner() {
		return this.reasoner;
	}

	// creates reasoner + reasoning service and makes it active
	public ReasonerComponent newReasoner(Class<? extends ReasonerComponent> clazz) {
		reasoner = cm.reasoner(clazz, source);
		rs = cm.reasoningService(reasoner);
		return reasoner;
	}	
	
	/**
	 * Set ReasoningService.
	 * 
	 * @param reasoningService
	 */
//	public void setReasoningService(ReasoningService reasoningService) {
//		this.rs = reasoningService;
//	}

	/**
	 * Get ReasoningService.
	 * 
	 * @return ReasoningService
	 */
	public ReasoningService getReasoningService() {
		return this.rs;
	}

	/**
	 * Set LearningProblem.
	 * 
	 * @param learningProblem
	 */
//	public void setLearningProblem(LearningProblem learningProblem) {
//		this.lp = learningProblem;
//	}

	/**
	 * Get LearningProblem.
	 * 
	 * @return learningProblem
	 */
	public LearningProblem getLearningProblem() {
		return this.lp;
	}

	public LearningProblem newLearningProblem(Class<? extends LearningProblem> clazz) {
		lp = cm.learningProblem(clazz, rs);
		return lp;
	}
	
	/**
	 * Set LearningAlgorithm.
	 * 
	 * @param learningAlgorithm
	 */
//	public void setLearningAlgorithm(LearningAlgorithm learningAlgorithm) {
//		this.la = learningAlgorithm;
//	}

	/**
	 * Get LearningAlgorithm.
	 * 
	 * @return LearningAlgorithm
	 */
	public LearningAlgorithm getLearningAlgorithm() {
		return this.la;
	}

	public LearningAlgorithm newLearningAlgorithm(Class<? extends LearningAlgorithm> clazz) throws LearningProblemUnsupportedException {
		la = cm.learningAlgorithm(clazz, lp, rs);
		return la;
	}	
	
	public boolean tabNeedsInit(int tabIndex) {
		return needsInit[tabIndex];
	}
	
	/**
	 * KnowledgeSource.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitKnowledgeSource() {
		return needsInit[0];
	}

	/**
	 * Set true if you run KnowwledgeSource.init. The inits from other tabs
	 * behind will automatic set to false.
	 */
//	public void setInitKnowledgeSource(Boolean is) {
//		needsInit[0] = is;
//		for (int i = 1; i < 4; i++)
//			needsInit[i] = false;
//	}

	/**
	 * Reasoner.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitReasoner() {
		return needsInit[1];
	}

	/**
	 * Set true if you run Reasoner.init. The inits from other tabs behind will
	 * automatic set to false.
	 */
//	public void setInitReasoner(Boolean is) {
//		needsInit[1] = is;
//		for (int i = 2; i < 4; i++)
//			needsInit[i] = false;
//	}

	/**
	 * LearningProblem.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitLearningProblem() {
		return needsInit[2];
	}

	/**
	 * Set true if you run LearningProblem.init. The inits from other tabs
	 * behind will automatic set to false.
	 */
//	public void setInitLearningProblem(Boolean is) {
//		needsInit[2] = is;
//		for (int i = 3; i < 4; i++)
//			needsInit[i] = false;
//	}

	/**
	 * LearningAlgorithm.init() has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitLearningAlgorithm() {
		return needsInit[3];
	}

	public boolean needsInit(int tabIndex) {
		return needsInit[tabIndex];
	}
	
	public boolean isEnabled(int tabIndex) {
		return isEnabled[tabIndex];
	}
	
	/**
	 * set true if you run LearningAlgorithm.init
	 */
//	public void setInitLearningAlgorithm(Boolean is) {
//		needsInit[3] = is;
//	}

	/**
	 * Set true if you start the algorithm.
	 * 
	 * @param isThreadRunning
	 */
	public void setThreadIsRunning(Boolean isThreadRunning) {
		if (isThreadRunning)
			algorithmRunStartTime = System.nanoTime();
		else if (algorithmRunStartTime != null)
			if (algorithmRunStartTime < System.nanoTime())
				algorithmRunStopTime = System.nanoTime();
		this.threadIsRunning = isThreadRunning;
	}

	/**
	 * Get true if algorithm has started, false if not.
	 * 
	 * @return true if algorithm is running, false if not.
	 */
	public Boolean getThreadIsRunning() {
		return this.threadIsRunning;
	}

	/**
	 * Get time in ns for run of algorithm. If algorithm is still running return
	 * time between RunStartTime and now.
	 * 
	 * @return time in ns
	 */
	public Long getAlgorithmRunTime() {
		if (algorithmRunStartTime != null)
			if (algorithmRunStopTime != null) {
				if (algorithmRunStartTime < algorithmRunStopTime)
					return algorithmRunStopTime - algorithmRunStartTime;
			} else
				return System.nanoTime() - algorithmRunStartTime;
		return null;
	}

	/**
	 * It is necessary for init LearningProblem.
	 * 
	 * @return true, if necessary example was set otherwise false
	 */
	public Boolean isSetExample() {
		if (lp.getClass().getSimpleName().equals("PosOnlyDefinitionLP")) {
			if (cm.getConfigOptionValue(lp, "positiveExamples") != null)
				return true;
		} else if (cm.getConfigOptionValue(lp, "positiveExamples") != null
				&& cm.getConfigOptionValue(lp, "negativeExamples") != null)
			return true;
		return false;
	}

//	public void reInit() {
//		cm = ComponentManager.getInstance();
//		source = null;
//		reasoner = null;
//		rs = null;
//		lp = null;
//		la = null;
//		needsInit = new boolean[4];
//		threadIsRunning = false;
//		algorithmRunStartTime = null;
//		algorithmRunStopTime = null;
//	}
	
	// init the specified component and record which ones where initialised
	public void init(int tabIndex) {
		
		// a thread class for handling inits (GUI has to remain
		// responsive while init process is executed)
		class InitThread extends Thread {
			private Component component;
			public InitThread(Component component) {
				this.component = component;
			}
	        @Override
	        public void run() {
	        	gui.disableTabbedPane();
	        	try {
					component.init();
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				gui.enableTabbedPane();
	        }
	    }
		
//	    final SwingWorker worker = new SwingWorker() {
		class InitWorker extends SwingWorker<Boolean,Boolean> {
			private Component component;
			public InitWorker(Component component) {
				this.component = component;
			}	    	
	    	
			@Override
			protected Boolean doInBackground() throws Exception {
				gui.getStatusPanel().setStatus("Initialising reasoner ... ");
//				gui.getStatusPanel().repaint();
	        	gui.disableTabbedPane();
	        	gui.setEnabled(false);
	        	JFrame waitFrame = new JFrame();
	        	waitFrame.setUndecorated(true);
	        	waitFrame.setSize(200, 200);
	        	URL imgURL = Config.class.getResource("ajaxloader.gif");
	        	ImageIcon waitIcon = new ImageIcon(imgURL, "wait");	        	
//	        	waitFrame.add(new JLabel("Wait!"), waitIcon, SwingConstants.RIGHT);
//	        	waitFrame.add(new JLabel("Wait"));
	        	waitFrame.add(new JLabel(waitIcon), BorderLayout.NORTH);
	        	waitFrame.add(new JLabel("Initialising reasoner. Please wait."), BorderLayout.SOUTH);
	        	waitFrame.setLocationRelativeTo(gui);
	        	waitFrame.setVisible(true);
	        	try {
					component.init();
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				gui.enableTabbedPane();
				gui.setEnabled(true);
				gui.getStatusPanel().extendMessage("done.");
				waitFrame.dispose();
				// TODO Auto-generated method stub
//				return null;
				return true;
			}
	      };

		
		try {
			if(tabIndex==0) {
				gui.disableTabbedPane();
				gui.getStatusPanel().setStatus("Initialising knowledge source ... ");
				source.init();
				gui.getStatusPanel().extendMessage("done.");
				gui.enableTabbedPane();
			} else if(tabIndex==1) {
//				gui.disableTabbedPane();
//				gui.getStatusPanel().setStatus("Initialising reasoner ... ");
//				gui.repaint();
//				Thread initThread = new InitThread(reasoner);
//				initThread.run();
//				SwingWorker worker;
//				try {
//					SwingUtilities.invokeAndWait(initThread);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
			    InitWorker worker = new InitWorker(reasoner);
			    worker.execute();						
				
//				reasoner.init();
//				gui.getStatusPanel().extendMessage("done.");
//				gui.enableTabbedPane();
			} else if(tabIndex==2) {
				gui.disableTabbedPane();
				gui.getStatusPanel().setStatus("Initialising learning problem ... ");
				lp.init();
				gui.getStatusPanel().extendMessage("done.");
				gui.enableTabbedPane();
			} else if(tabIndex == 3) {
				gui.disableTabbedPane();
				gui.getStatusPanel().setStatus("Initialising learning algorithm ... ");
				la.init();
				gui.getStatusPanel().extendMessage("done.");
				gui.enableTabbedPane();
			}
		} catch (ComponentInitException e) {
			// TODO display message in status bar
			e.printStackTrace();
		}
		
		needsInit[tabIndex] = false;
		System.out.println("component " + tabIndex + " initialised.");
	}
	
	// applies a configuration option - used as delegate method, which invalidates components
	public <T> void applyConfigEntry(Component component, ConfigEntry<T> entry) {
		System.out.println("Applying " + entry + " to " + component.getClass().getName() + ".");
		
		cm.applyConfigEntry(component, entry);
		// enable tabs if setting the value completed mandatory settings
		enableTabsIfPossible();
		// invalidate components
		if(component instanceof KnowledgeSource) {
			needsInit[0] = true;
			needsInit[1] = true;
			needsInit[2] = true;
			needsInit[3] = true;
			if(isEnabled[0]) {
				gui.setStatusMessage("All mandatory options filled in. You can continue to the reasoner tab.");
			}
		} else if(component instanceof ReasonerComponent) {
			needsInit[1] = true;
			needsInit[2] = true;
			needsInit[3] = true;
			if(isEnabled[1]) {
				gui.setStatusMessage("All mandatory options filled in. You can continue to the learning problem tab.");
			}			
		} else if(component instanceof LearningProblem) {
			needsInit[2] = true;
			needsInit[3] = true;
			if(isEnabled[2]) {
				gui.setStatusMessage("All mandatory options filled in. You can continue to the learning algorithm tab.");
			}			
		} else if(component instanceof LearningAlgorithm) {
			needsInit[3] = true;	
			if(isEnabled[3]) {
				gui.setStatusMessage("All mandatory options filled in. You can now run the algorithm.");
			}			
		}
		
		gui.updateTabs();
	}
	
	private void enableTabsIfPossible() {
		if(mandatoryOptionsSpecified(source)) {
			isEnabled[0] = true;
		} 
		if(mandatoryOptionsSpecified(reasoner) && isEnabled[0]) {
			isEnabled[1] = true;
		} 
		if(mandatoryOptionsSpecified(lp) && isEnabled[1]) {
			isEnabled[2] = true;
		} 
		if(mandatoryOptionsSpecified(la) && isEnabled[1] && isEnabled[2]) {
			isEnabled[3] = true;
		}
	}
	
	// TODO use specification of mandatory variables
	private boolean mandatoryOptionsSpecified(Component component) {
//		System.out.println("check mandatory options for " + component.getClass().getName());
		if(component instanceof OWLFile) {
			if(cm.getConfigOptionValue(source, "url") == null) {
				return false;
			}
		} else if(component instanceof KBFile) {
			if(cm.getConfigOptionValue(source, "url") == null && cm.getConfigOptionValue(source, "filename") == null) {
				return false;
			}
		} else if(component instanceof PosNegLP) {
			if(cm.getConfigOptionValue(component, "positiveExamples")==null || cm.getConfigOptionValue(component, "negativeExamples") == null) {
				return false;
			}
		}
		return true;
	}
	
	// delegate method for getting config option values
	public <T> T getConfigOptionValue(Component component, ConfigOption<T> option) {
		return cm.getConfigOptionValue(component, option);
	}
	
}
