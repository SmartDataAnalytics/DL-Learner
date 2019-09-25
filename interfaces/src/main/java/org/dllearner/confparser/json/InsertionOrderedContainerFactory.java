package org.dllearner.confparser.json;

import org.json.simple.parser.ContainerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Simon Bin on 16-8-22.
 */
public class InsertionOrderedContainerFactory implements ContainerFactory {
	@Override
	public Map createObjectContainer() {
		return new LinkedHashMap();
	}

	@Override
	public List creatArrayContainer() {
		return new LinkedList();
	}
}
