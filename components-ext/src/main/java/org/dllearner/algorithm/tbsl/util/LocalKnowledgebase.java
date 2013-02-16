package org.dllearner.algorithm.tbsl.util;

import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;

import com.hp.hpl.jena.rdf.model.Model;

public class LocalKnowledgebase extends Knowledgebase{
	
	private Model model;
	
	public LocalKnowledgebase(Model model, String label, String description, Index resourceIndex, Index propertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
		super(label, description, resourceIndex, propertyIndex, classIndex, mappingIndex);
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}

}
