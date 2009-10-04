package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public class LearningManager {
	
	private static LearningManager instance;
	
	private List<LearningManagerListener> listeners;
	
	public static final int AUTO_LEARN_MODE = 0;
    public static final int MANUAL_LEARN_MODE = 1;
    
    private int learnMode = 0;
    
    private List<EvaluatedDescriptionClass> newDescriptions;
    
    private int currentDescriptionIndex = 0;
	
	public static LearningManager getInstance(){
		if(instance == null){
			instance = new LearningManager();
		}
		return instance;
	}
	
	public LearningManager(){
		listeners = new ArrayList<LearningManagerListener>();
	}
	
	public void setLearningMode(int learningMode){
		this.learnMode = learningMode;
	}
	
	public int getLearningMode(){
		return learnMode;
	}

	public List<EvaluatedDescriptionClass> getNewDescriptions() {
		return newDescriptions;
	}

	public void setNewDescriptions(List<EvaluatedDescriptionClass> newDescriptions) {
		this.newDescriptions = newDescriptions;
		currentDescriptionIndex = 0;
		fireNewDescriptionsAdded(newDescriptions);
		setNextDescription();
	}

	public int getCurrentDescriptionIndex() {
		return currentDescriptionIndex;
	}

	public void setCurrentDescriptionIndex(int currentDescriptionIndex) {
		this.currentDescriptionIndex = currentDescriptionIndex;
	}
	
	public void setNextDescription(){
		OREManager.getInstance().setNewClassDescription(newDescriptions.get(currentDescriptionIndex));
		fireNewDescriptionSelected(currentDescriptionIndex);
		currentDescriptionIndex++;
		if(currentDescriptionIndex >= newDescriptions.size()){
			fireNoDescriptionsLeft();
		}
	}

	public boolean addListener(LearningManagerListener listener) {
		return listeners.add(listener);
	}

	public boolean removeListener(LearningManagerListener listener) {
		return listeners.remove(listener);
	}
	
	public void fireNewDescriptionsAdded(List<EvaluatedDescriptionClass> descriptions){
		for(LearningManagerListener listener : listeners){
			listener.newDescriptionsAdded(descriptions);
		}
	}
	
	public void fireNoDescriptionsLeft(){
		for(LearningManagerListener listener : listeners){
			listener.noDescriptionsLeft();
		}
	}
	
	public void fireNewDescriptionSelected(int index){
		for(LearningManagerListener listener : listeners){
			listener.newDescriptionSelected(index);
		}
	}

}
