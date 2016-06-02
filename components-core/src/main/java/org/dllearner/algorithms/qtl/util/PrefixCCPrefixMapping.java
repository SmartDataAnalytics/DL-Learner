/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Map.Entry;

import org.dllearner.utilities.PrefixCCMap;

import org.apache.jena.shared.PrefixMapping;

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
