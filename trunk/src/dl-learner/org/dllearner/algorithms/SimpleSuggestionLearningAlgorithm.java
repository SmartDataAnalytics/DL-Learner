package org.dllearner.algorithms;

import java.util.Set;
import java.util.List;
import java.util.*;

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.Score;
import org.dllearner.core.owl.*;

public class SimpleSuggestionLearningAlgorithm extends LearningAlgorithm implements Runnable{

private Score solutionScore;	
private boolean stop = false;
private Description bestSollution;
private Set<Description> simpleSuggestions;
private LearningProblem learningProblem;

	public SimpleSuggestionLearningAlgorithm()
	{
		//this.learningProblem = learningProblem;
	}
	
	public Description getBestSolution()
	{
		return bestSollution;
	}
	
	
	public void stop()
	{
		stop = true;
	}
	
	public void start()
	{
		
	}
	
	public <T> void applyConfigEntry(ConfigEntry<T> entry)
	{
		
	}
	
	public void init()
	{
		
	}
	
	public Score getSolutionScore()
	{
		return solutionScore;
	}
	
	public void run()
	{
		
	}
	
	public Set<Description> getSimpleSuggestions(ReasoningService rs,Set<Individual> indi) {
		// EXISTS property.TOP
		// ESISTS hasChild
		// EXISTS hasChild.male
		simpleSuggestions= new HashSet<Description>();
		List<ObjectProperty> test=rs.getAtomicRolesList();
		while(test.iterator().hasNext())
		{
		test.iterator().next();
		Description d1 = new ObjectSomeRestriction(test.iterator().next(), new Thing());
		test.remove(rs.getAtomicRolesList().iterator().next());
		simpleSuggestions.add(d1);
		}
		return simpleSuggestions;
	}
}
