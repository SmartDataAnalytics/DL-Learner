package org.dllearner.algorithm.tbsl.util;

import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;

public abstract class Knowledgebase {

	private String label;
	private String description;

	private Index resourceIndex;
	private Index propertyIndex;
	private Index classIndex;
	
	private MappingBasedIndex mappingIndex;

	public Knowledgebase(String label, String description,
			Index resourceIndex, Index propertyIndex, Index classIndex) {
		this(label, description, resourceIndex, propertyIndex, classIndex, null);
	}
	
	public Knowledgebase(String label, String description,
			Index resourceIndex, Index propertyIndex, Index classIndex, MappingBasedIndex mappingIndex) {
		this.label = label;
		this.description = description;
		this.resourceIndex = resourceIndex;
		this.propertyIndex = propertyIndex;
		this.classIndex = classIndex;
		this.mappingIndex = mappingIndex;
	}
	
	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public Index getResourceIndex() {
		return resourceIndex;
	}

	public Index getPropertyIndex() {
		return propertyIndex;
	}

	public Index getClassIndex() {
		return classIndex;
	}
	
	public MappingBasedIndex getMappingIndex() {
		return mappingIndex;
	}

	@Override
	public String toString() {
		return label;
	}

}
