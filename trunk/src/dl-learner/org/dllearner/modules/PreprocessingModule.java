package org.dllearner.modules;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.KB;

public interface PreprocessingModule {

	public void preprocess(KB kb, Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfFileOption> confOptions, List<List<String>> functionCalls,
			String baseDir, boolean useQueryMode);
	
	public String getModuleName();
	
	/*
	public Map<String, Integer[]> registerIntOptions();
	public Map<String, Double[]> registerDoubleOptions();
	public Map<String, String[]> registerStringOptions();
	public List<String> registerSetOptions();
	*/
}
