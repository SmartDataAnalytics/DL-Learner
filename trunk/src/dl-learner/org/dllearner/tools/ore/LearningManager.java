package org.dllearner.tools.ore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.mindswap.pellet.utils.progress.ProgressMonitor;

public class LearningManager {

	private static LearningManager instance;

	public enum LearningMode {
		AUTO, MANUAL, OFF
	};

	public enum LearningType {
		EQUIVALENT, SUPER
	};

	private List<LearningManagerListener> listeners;

	private LearningMode learningMode = LearningMode.AUTO;
	private LearningType learningType = LearningType.EQUIVALENT;

	private ComponentManager cm;
	private ClassLearningProblem lp;
	private CELOE la;
	private ReasonerComponent reasoner;

	private NamedClass currentClass2Describe;

	private int maxExecutionTimeInSeconds;
	private double noisePercentage;
	private double threshold;
	private int maxNrOfResults;
	private int minInstanceCount;

	private List<EvaluatedDescriptionClass> newDescriptions;

	private List<EvaluatedDescriptionClass> equivalentDescriptions;
	private List<EvaluatedDescriptionClass> superDescriptions;

	private int currentDescriptionIndex = 0;

	private boolean learningInProgress = false;

	private ProgressMonitor progressMonitor;

	public static synchronized LearningManager getInstance() {
		if (instance == null) {
			instance = new LearningManager();
		}
		return instance;
	}

	public LearningManager() {
		cm = ComponentManager.getInstance();
		reasoner = OREManager.getInstance().getReasoner();
		listeners = new ArrayList<LearningManagerListener>();
		newDescriptions = new ArrayList<EvaluatedDescriptionClass>();
		progressMonitor = TaskManager.getInstance().getStatusBar();
	}

	public void setLearningMode(LearningMode learningMode) {
		this.learningMode = learningMode;
	}
	
	public LearningMode getLearningMode() {
		return learningMode;
	}

	public void setLearningType(LearningType learningType) {
		this.learningType = learningType;
	}

	public LearningType getLearningType() {
		return learningType;
	}

	public void initLearningProblem() {
		reasoner = OREManager.getInstance().getReasoner();
		lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		try {
			if (learningType.equals(LearningType.EQUIVALENT)) {
				cm.applyConfigEntry(lp, "type", "equivalence");
			} else {
				cm.applyConfigEntry(lp, "type", "superClass");
			}
			lp.getConfigurator().setClassToDescribe(new URL(currentClass2Describe.toString()));
			lp.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void initLearningAlgorithm() {
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			la.getConfigurator().setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
			la.getConfigurator().setUseNegation(false);
			la.getConfigurator().setNoisePercentage(noisePercentage);
			la.getConfigurator().setMaxNrOfResults(maxNrOfResults);

		} catch (LearningProblemUnsupportedException e1) {
			e1.printStackTrace();
		}

		try {
			la.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public boolean learnAsynchronously() {
		if (learningInProgress) {
			return false;
		}
		initLearningProblem();
		initLearningAlgorithm();
		learningInProgress = true;
		progressMonitor.setProgressLength(maxExecutionTimeInSeconds);
		String learnType = "";
		if (learningType == LearningType.EQUIVALENT) {
			learnType = "equivalent";
		} else {
			learnType = "superclass";
		}
		progressMonitor.setProgressMessage("Learning " + learnType + " expressions");

		Thread currentLearningThread = new Thread(new LearningRunner(), "Learning Thread");
		currentLearningThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable throwable) {

			}
		});
		currentLearningThread.start();
		return true;
	}
	
	public boolean startLearning() {
		if (learningInProgress) {
			return false;
		}
		initLearningProblem();
		initLearningAlgorithm();
		learningInProgress = true;
		progressMonitor.setProgressLength(maxExecutionTimeInSeconds);
		String learnType = "";
		if (learningType == LearningType.EQUIVALENT) {
			learnType = "equivalent";
		} else {
			learnType = "superclass";
		}
		progressMonitor.setProgressMessage("Learning " + learnType + " expressions");

		try {
			la.start();
		} finally {
			learningInProgress = false;
			fireLearningFinished();
			progressMonitor.setProgressMessage("Done");
		}
		return true;
	}

	public boolean stopLearning() {
		if (!learningInProgress) {
			return false;
		}
		la.stop();
		learningInProgress = false;
		progressMonitor.setProgressLength(0);

		return true;
	}
	
	public boolean isLearning(){
		return la.isRunning();
	}

	@SuppressWarnings("unchecked")
	public synchronized List<EvaluatedDescriptionClass> getCurrentlyLearnedDescriptions() {
		List<EvaluatedDescriptionClass> result;
		if (la != null) {
			result = Collections.unmodifiableList((List<EvaluatedDescriptionClass>) la
					.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true));
		} else {
			result = Collections.emptyList();
		}
		return result;
	}
	
	public boolean isManualLearningMode() {
		return learningMode.equals(LearningMode.MANUAL);
	}

	public List<EvaluatedDescriptionClass> getNewDescriptions() {
		return newDescriptions;
	}

	public void setCurrentClass2Describe(NamedClass nc) {
		currentClass2Describe = nc;
	}

	public NamedClass getCurrentClass2Describe() {
		return currentClass2Describe;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	public void setMinInstanceCount(int minInstanceCount){
		this.minInstanceCount = minInstanceCount;
	}
	
	public int getMinInstanceCount(){
		return minInstanceCount;
	}

	public void setNewDescriptions(List<List<EvaluatedDescriptionClass>> descriptions) {
		newDescriptions.clear();
		newDescriptions.addAll(descriptions.get(0));
		newDescriptions.addAll(descriptions.get(1));
		equivalentDescriptions = descriptions.get(0);
		superDescriptions = descriptions.get(1);
		currentDescriptionIndex = 0;
		fireNewDescriptionsAdded(newDescriptions);
		setNextDescription();
	}

	public void addEquivalentDescriptions(List<EvaluatedDescriptionClass> descriptions) {
		equivalentDescriptions = descriptions;
	}

	public void addSuperDescriptions(List<EvaluatedDescriptionClass> descriptions) {
		superDescriptions = descriptions;
	}

	public boolean isEquivalentDescription(EvaluatedDescriptionClass desc) {
		return equivalentDescriptions.contains(desc);
	}

	public boolean isSuperDescription(EvaluatedDescriptionClass desc) {
		return superDescriptions.contains(desc);
	}

	public int getCurrentDescriptionIndex() {
		return currentDescriptionIndex;
	}

	public void setCurrentDescriptionIndex(int currentDescriptionIndex) {
		this.currentDescriptionIndex = currentDescriptionIndex;
	}

	public void setNextDescription() {
		OREManager.getInstance().setNewClassDescription(newDescriptions.get(currentDescriptionIndex));
		fireNewDescriptionSelected(currentDescriptionIndex);
		currentDescriptionIndex++;
		if (currentDescriptionIndex >= newDescriptions.size()) {
			fireNoDescriptionsLeft();
		}
	}

	public boolean addListener(LearningManagerListener listener) {
		return listeners.add(listener);
	}

	public boolean removeListener(LearningManagerListener listener) {
		return listeners.remove(listener);
	}

	public void fireNewDescriptionsAdded(List<EvaluatedDescriptionClass> descriptions) {
		for (LearningManagerListener listener : listeners) {
			listener.newDescriptionsAdded(descriptions);
		}
	}

	public void fireNoDescriptionsLeft() {
		for (LearningManagerListener listener : listeners) {
			listener.noDescriptionsLeft();
		}
	}

	public void fireNewDescriptionSelected(int index) {
		for (LearningManagerListener listener : listeners) {
			listener.newDescriptionSelected(index);
		}
	}

	private class LearningRunner implements Runnable {

		@Override
		public void run() {
			try {
				la.start();
			} finally {
				learningInProgress = false;
				fireLearningFinished();
				progressMonitor.setProgressMessage("Done");
			}
		}
	}

	public void fireLearningFinished() {
//		for(LearningManagerListener l : listeners){
//		}
	}

}
