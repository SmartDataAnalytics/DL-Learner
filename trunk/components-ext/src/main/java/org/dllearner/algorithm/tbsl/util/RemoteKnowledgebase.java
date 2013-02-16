package org.dllearner.algorithm.tbsl.util;

import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class RemoteKnowledgebase extends Knowledgebase{
	
	private SparqlEndpoint endpoint;

	public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index propertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
		super(label, description, resourceIndex, propertyIndex, classIndex, mappingIndex);
		this.endpoint = endpoint;
	}

	public SparqlEndpoint getEndpoint() {
		return endpoint;
	}
	

}
