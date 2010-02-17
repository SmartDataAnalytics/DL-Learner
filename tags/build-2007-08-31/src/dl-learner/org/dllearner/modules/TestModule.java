package org.dllearner.modules;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.ConfigurationOption;
import org.dllearner.Main;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;

public class TestModule extends AbstractPreprocessingModule {

	public void preprocess(KB kb,
			Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfigurationOption> confOptions, List<List<String>> functionCalls,
			String baseDir, boolean useQueryMode) {
		
		Main.getConfMgr().addStringOption("bla", new String[] {"test"});
		System.out.println("Modul-Hauptmethode");

	}
/*
	public Map<String, Integer[]> registerIntOptions() {
		Map<String, Integer[]> intOptions = new HashMap<String, Integer[]>();
		intOptions.put("testOption", new Integer[] {1,1000});
		return intOptions;
	}
*/
	public String getModuleName() {
		return "sparql";
	}
}
