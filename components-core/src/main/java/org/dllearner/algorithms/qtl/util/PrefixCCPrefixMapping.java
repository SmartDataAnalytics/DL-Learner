package org.dllearner.algorithms.qtl.util;

import java.util.Map.Entry;

import org.dllearner.utilities.PrefixCCMap;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A prefix mapping based on http://prefix.cc/
 * 
 * @author Lorenz Buehmann
 *
 */
public class PrefixCCPrefixMapping {

	public static final PrefixMapping Full = PrefixMapping.Factory.create();

	static {
		PrefixCCMap prefixCCMap = PrefixCCMap.getInstance();

		for (Entry<String, String> entry : prefixCCMap.entrySet()) {
			String prefix = entry.getKey();
			String uri = entry.getValue();

			Full.setNsPrefix(prefix, uri);
		}
		Full.lock();
	}

}
