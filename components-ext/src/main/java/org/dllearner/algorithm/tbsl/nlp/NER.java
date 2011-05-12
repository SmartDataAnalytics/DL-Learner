package org.dllearner.algorithm.tbsl.nlp;

import java.util.List;

public interface NER {
	
	List<String> getNamedEntitites(String sentence);

}
