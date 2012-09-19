/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.utilities;

import java.util.HashMap;

/**
 * Can be used as base for a prefix map.
 * 
 * TODO: We might implement a version of this class, which is synchronised with prefix.cc.
 * 
 * @author Jens Lehmann
 *
 */
public class CommonPrefixMap extends HashMap<String,String> {

	private static final long serialVersionUID = 5434065917532534702L;

	public CommonPrefixMap() {
		put("dbpedia","http://dbpedia.org/resource/");
		put("dbp","http://dbpedia.org/property/");
		put("dbo","http://dbpedia.org/ontology/");
		put("yago","http://dbpedia.org/class/yago/");
		put("gml","http://www.opengis.net/gml/");
	}
	
}
