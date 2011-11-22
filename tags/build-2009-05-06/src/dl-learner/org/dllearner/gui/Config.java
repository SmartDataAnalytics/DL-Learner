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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.cli.Start;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.parser.ParseException;

/**
 * Config save all together used variables: ComponentManager, KnowledgeSource,
 * Reasoner, ReasonerComponent, LearningProblem, LearningAlgorithm; also inits of
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
//	private ReasonerComponent rs;
	private LearningProblem lp;
	private LearningAlgorithm la;

	// stores which components need to be initialised (either
	// because they have not been initialiased or previous components
	// have changed configuration options, which require initialisation)
	private boolean[] needsInit = new boolean[4];

	// specifies whether the panel is enabled, i.e. the user
	// can select it (all mandatory variables in selected components have been
	// choosen)
	private boolean[] isEnabled = new boolean[4];

	private StartGUI gui;

	/**
	 * This constructor can be used systemwide to save configurations in conf files.
	 * Of course it should not really belong here, but either in core or utilities.
	 * Consider refactoring using a subclass of Config for the GUI.
	 * Nevertheless it still works.
	 * 
	 *  
	 * @param cm
	 * @param source
	 * @param reasoner
	 * @param rs
	 * @param lp
	 * @param la
	 */
	public Config(ComponentManager cm, KnowledgeSource source, ReasonerComponent reasoner, LearningProblem lp, LearningAlgorithm la) {
		super();
		this.cm = cm;
		this.source = source;
		this.reasoner = reasoner;
		this.lp = lp;
		this.la = la;
	}

	/**
	 * Create central configuration object.
	 * 
	 * @param gui
	 *            The main gui object. It is passed as parameter, such that the
	 *            configuration handler can update the GUI appropriately.
	 */
	public Config(StartGUI gui) {
		this.gui = gui;
		// none of the components is initialised
		for (int i = 0; i < 4; i++) {
			needsInit[i] = true;
			// TODO there might be knowledge source without mandatory options
			isEnabled[i] = false;
		}
	}

	/**
	 * Loads a file using the commandline interface and asks it for loaded
	 * components and config options. This way, we ensure that the loading
	 * process is always compatible with the command line and we do not need to
	 * implement the process again for the GUI. Afte loading, the GUI is updates
	 * appropriately.
	 * 
	 * @param file
	 *            The file to load.
	 */
	public void loadFile(File file) {
		// use CLI to load file
		try {
			// set all loaded components as active components
			Start start = null;
			try {
				start = new Start(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Set<KnowledgeSource> sources = start.getSources();
			if (sources.size() != 1) {
				gui.getStatusPanel().setExceptionMessage(
						"Warning: GUI supports only one knowledge source.");
			}
			source = sources.iterator().next();
			reasoner = start.getReasonerComponent();
//			rs = start.getReasonerComponent();
			lp = start.getLearningProblem();
			la = start.getLearningAlgorithm();

			// all components initialised and enabled
			for (int i = 0; i < 4; i++) {
				needsInit[i] = false;
				isEnabled[i] = true;
			}

			// update tabs in GUI such that algorithm can be run
			gui.updateTabs();
			gui.updateStatusPanel();
			for (int i = 0; i < 4; i++) {
				gui.panels[i].update();
			}

		} catch (ComponentInitException e) {
			gui.getStatusPanel().setExceptionMessage(e.getMessage());
			e.printStackTrace();
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
	 * Get KnowledgeSource.
	 * 
	 * @return KnowledgeSource
	 */
	public KnowledgeSource getKnowledgeSource() {
		return source;
	}

	/**
	 * Creates a knowledge source and makes it the active source.
	 * 
	 * @param clazz
	 *            knowledge source class
	 * @return knowledge source instance
	 */
	public KnowledgeSource newKnowledgeSource(Class<? extends KnowledgeSource> clazz) {
		source = cm.knowledgeSource(clazz);
		// logger.debug("new knowledge source " + clazz + " created");
		return source;
	}

	/**
	 * Changes active knowledge source. This method does not not only create a
	 * knowledge source, but also updates the active reasoner to use the new
	 * knowledge source.
	 * 
	 * @param clazz
	 *            knowledge source class
	 * @return knowledge source instance
	 */
	public KnowledgeSource changeKnowledgeSource(Class<? extends KnowledgeSource> clazz) {
		source = cm.knowledgeSource(clazz);
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		sources.add(source);
		reasoner.changeSources(sources);
		// logger.debug("knowledge source " + clazz + " changed");
		// create a new reasoner object using the current class and the selected
		// source
		// reasoner = cm.reasoner(reasoner.getClass(), source);
		// rs = cm.reasoningService(reasoner);
		// lp = cm.learningProblem(lp.getClass(), rs);
		// try {
		// la = cm.learningAlgorithm(la.getClass(), lp, rs);
		// } catch (LearningProblemUnsupportedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		needsInit[0] = true;
		needsInit[1] = true;
		needsInit[2] = true;
		needsInit[3] = true;		
		return source;
	}

	/**
	 * Set Reasoner.
	 * 
	 * @param reasoner
	 */
	// public void setReasoner(ReasonerComponent reasoner) {
	// this.reasoner = reasoner;
	// }
	/**
	 * Get Reasoner.
	 * 
	 * @return reasoner
	 */
	public ReasonerComponent getReasoner() {
		return this.reasoner;
	}

	/**
	 * Creates reasoner + reasoning service and makes it active.
	 * @param clazz The class of the reasoner.
	 * @return A reasoner instance.
	 */
	public ReasonerComponent newReasoner(Class<? extends ReasonerComponent> clazz) {
		reasoner = cm.reasoner(clazz, source);
//		rs = cm.reasoningService(reasoner);
		return reasoner;
	}

	/**
	 * Change the reasoner and notify the depending components
	 * (learning problem, learning algorithm).
	 * @param clazz The reasoner class.
	 * @return A reasoner instance.
	 */
	public ReasonerComponent changeReasoner(Class<? extends ReasonerComponent> clazz) {
		reasoner = cm.reasoner(clazz, source);
//		rs = cm.reasoningService(reasoner);
		lp.changeReasonerComponent(reasoner);
		la.changeReasonerComponent(reasoner);
		needsInit[1] = true;
		needsInit[2] = true;
		needsInit[3] = true;
		return reasoner;
	}

	/**
	 * Get LearningProblem.
	 * 
	 * @return learningProblem
	 */
	public LearningProblem getLearningProblem() {
		return this.lp;
	}

	/**
	 * Creates learning problem and makes it active.
	 * @param clazz The class of the learning problem.
	 * @return A learning problem instance.
	 */	
	public LearningProblem newLearningProblem(Class<? extends LearningProblem> clazz) {
		lp = cm.learningProblem(clazz, reasoner);
		return lp;
	}

	/**
	 * Change the learning problem and notify the depending components
	 * (learning algorithm).
	 * @param clazz The learning problem class.
	 * @return A learning problem instance.
	 */	
	public LearningProblem changeLearningProblem(Class<? extends LearningProblem> clazz) {
		lp = cm.learningProblem(clazz, reasoner);
		la.changeLearningProblem(lp);
		needsInit[2] = true;
		needsInit[3] = true;		
		return lp;
	}

	/**
	 * Get LearningAlgorithm.
	 * 
	 * @return LearningAlgorithm
	 */
	public LearningAlgorithm getLearningAlgorithm() {
		return this.la;
	}

	/**
	 * Creates learning algorithm and makes it active.
	 * @param clazz The class of the learning algorithm.
	 * @throws LearningProblemUnsupportedException If the learning algorithm
	 * does not support the learning problem (TODO should be handled intelligently
	 * by GUI).
	 * @return A learning algorithm instance.
	 */
	public LearningAlgorithm newLearningAlgorithm(Class<? extends LearningAlgorithm> clazz)
			throws LearningProblemUnsupportedException {
		la = cm.learningAlgorithm(clazz, lp, reasoner);
		return la;
	}

	/**
	 * Change the learning algorithm.
	 * @param clazz The learning algorithm class.
	 * @throws LearningProblemUnsupportedException If the learning algorithm
	 * does not support the learning problem (TODO should be handled intelligently
	 * by GUI).
	 * @return A learning algorithm instance.
	 */
	public LearningAlgorithm changeLearningAlgorithm(Class<? extends LearningAlgorithm> clazz)
			throws LearningProblemUnsupportedException {
		la = cm.learningAlgorithm(clazz, lp, reasoner);
		needsInit[3] = true;		
		return la;
	}

	/**
	 * Returns whether the corresponding tab needs to be initialised.
	 * @param tabIndex Index of the tab (0 to 3).
	 * @return True if the tab needs to be initialised for the learning
	 * algorithm to run. False, otherwise.
	 */
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
	 * Reasoner.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitReasoner() {
		return needsInit[1];
	}

	/**
	 * LearningProblem.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitLearningProblem() {
		return needsInit[2];
	}

	/**
	 * LearningAlgorithm.init() has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean needsInitLearningAlgorithm() {
		return needsInit[3];
	}

	/**
	 * Returns whether the corresponding tab is enabled (e.g.
	 * we can start to configure it, because all mandatory options
	 * of underlying components have been specified).
	 * @param tabIndex Index of the tab (0 to 3).
	 * @return True if the tab is enabled and false otherwise.
	 */	
	public boolean isEnabled(int tabIndex) {
		return isEnabled[tabIndex];
	}

	/**
	 * Initialises the specified components and records which ones where initialised.
	 * The init process is done in a different thread, so this method
	 * returns immediately.
	 * @param tabIndex A list of components (0 = knowledge source, 1 = reasoner, ...).
	 */ 
	public void init(List<Integer> tabIndex) {
		List<Component> components = new LinkedList<Component>();
		for (int i : tabIndex) {
			switch (i) {
			case 0:
				components.add(source);
				needsInit[i] = false;
				break;
			case 1:
				components.add(reasoner);
				needsInit[i] = false;
				break;
			case 2:
				components.add(lp);
				needsInit[i] = false;
				break;
			case 3:
				components.add(la);
				needsInit[i] = false;
				break;
			default:
				throw new Error("Illegal tab number " + i + " (needs to be 0-3).");
			}
		}
		InitWorker worker = new InitWorker(components, gui);
		worker.execute();

		if (tabIndex.size() == 1) {
			logger.info("Component " + tabIndex.get(0) + " initialised.");
		} else if (tabIndex.size() > 1) {
			logger.info("Components " + tabIndex + " initialised.");
		}

	}

	/**
	 * Applies a configuration option and cares for all consequences
	 * the GUI needs to take.
	 * @see ComponentManager#applyConfigEntry(Component, ConfigEntry)
	 * @param <T> The type of config entry.
	 * @param component The component to apply the entry to.
	 * @param entry The config entry to apply.
	 */
	public <T> void applyConfigEntry(Component component, ConfigEntry<T> entry) {
		System.out.println("Applying " + entry + " to " + component.getClass().getName() + ".");

		cm.applyConfigEntry(component, entry);
		// enable tabs if setting the value completed mandatory settings
		enableComponentsIfPossible();
		// invalidate components
		if (component instanceof KnowledgeSource) {
			needsInit[0] = true;
			needsInit[1] = true;
			needsInit[2] = true;
			needsInit[3] = true;
			if (isEnabled[0]) {
				gui
						.setStatusMessage("All mandatory options filled in. You can continue to the reasoner tab.");
			}
		} else if (component instanceof ReasonerComponent) {
			needsInit[1] = true;
			needsInit[2] = true;
			needsInit[3] = true;
			if (isEnabled[1]) {
				gui
						.setStatusMessage("All mandatory options filled in. You can continue to the learning problem tab.");
			}
		} else if (component instanceof LearningProblem) {
			needsInit[2] = true;
			needsInit[3] = true;
			if (isEnabled[2]) {
				gui
						.setStatusMessage("All mandatory options filled in. You can continue to the learning algorithm tab.");
			}
		} else if (component instanceof LearningAlgorithm) {
			needsInit[3] = true;
			if (isEnabled[3]) {
				gui
						.setStatusMessage("All mandatory options filled in. You can now run the algorithm.");
			}
		}

		gui.updateTabs();
	}

	/**
	 * Tests whether components can be enabled. A component is enabled,
	 * if it fulfills the following conditions:
	 * - the "previous" component has all mandatory options specified
	 * - all components before the previous one are enabled
	 * Otherwise, the component is disabled.
	 * Note it can also happend that enabled components become
	 * disabled if mandatory fields are cleared.
	 */
	public void enableComponentsIfPossible() {
		// 0: reasoner
		// 1: problem
		// 2: algorithm
		// 3: run

		isEnabled[0] = mandatoryOptionsSpecified(source);
		isEnabled[1] = isEnabled[0] && mandatoryOptionsSpecified(reasoner);
		isEnabled[2] = isEnabled[0] && isEnabled[1] && mandatoryOptionsSpecified(lp);
		isEnabled[3] = isEnabled[0] && isEnabled[1] && isEnabled[2]
				&& mandatoryOptionsSpecified(la);
	}

	/**
	 * Checks whether all mandatory options have been set for 
	 * a component. 
	 * TODO Use specification of mandatory variables.
	 * @param component The component to test.
	 * @return True if all mandatory options are set and false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean mandatoryOptionsSpecified(Component component) {
		// System.out.println("check mandatory options for " +
		// component.getClass().getName());
		if (component instanceof OWLFile) {
			if (cm.getConfigOptionValue(source, "url") == null) {
				return false;
			}
		} else if (component instanceof KBFile) {
			if (cm.getConfigOptionValue(source, "url") == null) {
				return false;
			}
		} else if (component instanceof PosNegLP) {
			if (cm.getConfigOptionValue(component, "positiveExamples") == null
					|| cm.getConfigOptionValue(component, "negativeExamples") == null
					|| ((Set<String>) cm.getConfigOptionValue(component, "positiveExamples"))
							.size() == 0
					|| ((Set<String>) cm.getConfigOptionValue(component, "negativeExamples"))
							.size() == 0) {
				return false;
			}
		} else if (component instanceof PosOnlyLP) {
			if (cm.getConfigOptionValue(component, "positiveExamples") == null
					|| ((Set<String>) cm.getConfigOptionValue(component, "positiveExamples"))
							.size() == 0) {
				return false;
			}
		} else if (component instanceof ClassLearningProblem) {
			if (cm.getConfigOptionValue(component, "classToDescribe") == null) {
				return false;
			}
		} else if (component instanceof SparqlKnowledgeSource) {
			if (cm.getConfigOptionValue(component, "instances") == null
					|| ((Set<String>) cm.getConfigOptionValue(component, "instances")).size() == 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Delegate method for getting config option values.
	 * @see ComponentManager#getConfigOptionValue(Component, ConfigOption)
	 * @param <T> Type of option.
	 * @param component Component, which has the option.
	 * @param option The option for which we want to know the value.
	 * @return The value of the specified option.
	 */
	public <T> T getConfigOptionValue(Component component, ConfigOption<T> option) {
		return cm.getConfigOptionValue(component, option);
	}

}