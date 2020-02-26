package org.dllearner.cli.parcel.modeling;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.parcel.reducer.ParCELReducer;

/**
 * Represents the information of a partial definition in one fold of validation, including:
 * <ul>
 * 	<li>training information, see {@link PhaseInfor}</li>
 * 	<li>testing information, see {@link PhaseInfor}</li>
 * 	<li>prediction model score</li>
 * 	<li>actual model score</li>
 * </ul>	
 * 
 * @author An C. Tran
 *
 */
public class FoldInfor {
	private int fold;
	private PhaseInfor training;
	private PhaseInfor testing;
	
	//score of the prediction model
	private double predScore = Double.MIN_VALUE;
	private double actualScore = Double.MIN_VALUE;
	
	//chosen by the reducer(s)
	private Set<ParCELReducer> selectedByReducers = new HashSet<ParCELReducer>();
	
	
	/**
	 * Constructor
	 * @param fold
	 * @param training
	 * @param testing
	 */
	public FoldInfor(int fold, PhaseInfor training, PhaseInfor testing) {
		this.fold = fold;
		this.training = training;
		this.testing = testing;			
	}
			
	public String toString() {
		return "fold " + fold + 
				": training=" + training.toString() +
				", testing=" + testing.toString(); 
	}
	
	public String getSelectedBy() {
		String result = "{";
		
		for (ParCELReducer reducer : selectedByReducers)
			result += reducer.getClass().getSimpleName() + ",";
		
		if (selectedByReducers.size() > 0)
			result = result.substring(0, result.length()-1);
		
		return result + "}";
	}

	public int getFold() {
		return fold;
	}

	public void setFold(int fold) {
		this.fold = fold;
	}

	public PhaseInfor getTraining() {
		return training;
	}

	public void setTraining(PhaseInfor training) {
		this.training = training;
	}

	public PhaseInfor getTesting() {
		return testing;
	}

	public void setTesting(PhaseInfor testing) {
		this.testing = testing;
	}

	public double getPredScore() {
		return predScore;
	}

	public void setPredScore(double predScore) {
		this.predScore = predScore;
	}

	public double getActualScore() {
		return actualScore;
	}

	public void setActualScore(double actualScore) {
		this.actualScore = actualScore;
	}

	public Set<ParCELReducer> getSelectedByReducers() {
		return selectedByReducers;
	}

	public void setSelectedByReducers(Set<ParCELReducer> selectedByReducers) {
		this.selectedByReducers = selectedByReducers;
	}
	
	
	
	
}