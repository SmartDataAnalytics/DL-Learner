package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public class LearningManager {
	
	private static LearningManager instance;
	
	private List<LearningManagerListener> listeners;
	
	public static final int AUTO_LEARN_MODE = 0;
    public static final int MANUAL_LEARN_MODE = 1;
    
    private int learnMode = 0;
    
    private NamedClass currentClass2Describe;
    
    private List<EvaluatedDescriptionClass> newDescriptions;
    
    private List<EvaluatedDescriptionClass> equivalentDescriptions;
    private List<EvaluatedDescriptionClass> superDescriptions;
    
    private int currentDescriptionIndex = 0;
	
	public static synchronized LearningManager getInstance(){
		if(instance == null){
			instance = new LearningManager();
		}
		return instance;
	}
	
	public LearningManager(){
		listeners = new ArrayList<LearningManagerListener>();
		newDescriptions = new ArrayList<EvaluatedDescriptionClass>();
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
	
	public void setCurrentClass2Describe(NamedClass nc){
		currentClass2Describe = nc;
	}
	
	public NamedClass getCurrentClass2Describe(){
		return currentClass2Describe;
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
	
	public void addEquivalentDescriptions(List<EvaluatedDescriptionClass> descriptions){
		equivalentDescriptions = descriptions;
	}
	
	public void addSuperDescriptions(List<EvaluatedDescriptionClass> descriptions){
		superDescriptions = descriptions;
	}
	
	public boolean isEquivalentDescription(EvaluatedDescriptionClass desc){
		return equivalentDescriptions.contains(desc);
	}
	
	public boolean isSuperDescription(EvaluatedDescriptionClass desc){
		return superDescriptions.contains(desc);
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
