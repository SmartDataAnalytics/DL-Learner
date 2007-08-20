package org.dllearner.modules;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.ConfigurationOption;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;

public interface PreprocessingModule {

	public void preprocess(KB kb, Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfigurationOption> confOptions, List<List<String>> functionCalls,
			String baseDir, boolean useQueryMode);
	
	public String getModuleName();
	
	/*
	public Map<String, Integer[]> registerIntOptions();
	public Map<String, Double[]> registerDoubleOptions();
	public Map<String, String[]> registerStringOptions();
	public List<String> registerSetOptions();
	*/
}
